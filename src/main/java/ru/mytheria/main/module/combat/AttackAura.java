package ru.mytheria.main.module.combat;

import lombok.Getter;
import lombok.experimental.NonFinal;
import meteordevelopment.orbit.EventHandler;
import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.network.packet.c2s.play.PlayerMoveC2SPacket;
import net.minecraft.text.Text;
import net.minecraft.util.Hand;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.hit.HitResult;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec2f;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.RaycastContext;

import ru.mytheria.api.events.impl.TickEvent;
import ru.mytheria.api.module.Category;
import ru.mytheria.api.module.Module;
import ru.mytheria.api.module.settings.impl.BooleanSetting;
import ru.mytheria.api.module.settings.impl.ModeListSetting;
import ru.mytheria.api.module.settings.impl.ModeSetting;
import ru.mytheria.api.module.settings.impl.SliderSetting;

import java.util.Random;

@Getter
public class AttackAura extends Module {
    private static final net.minecraft.client.MinecraftClient mc = net.minecraft.client.MinecraftClient.getInstance();

    private final Object targetSelector = new Object();

    @NonFinal
    private LivingEntity target = null;
    @NonFinal
    private LivingEntity lastTarget = null;

    @NonFinal
    private Vec2f rotateVector = new Vec2f(new Random().nextFloat() * 360f, 0f);
    @NonFinal
    private boolean snapbackLock = false;

    private final Random random = new Random();
    private Vec3d randomOffset = new Vec3d(0, 0, 0);
    private long lastRandomizationTime = 0;

    // --- Grim AC Bypass Logic ---
    private int rotationToAttackDelay = 0;
    // Rotation (T0) -> Nothing (T1) -> Interact (T2) -> Nothing (T3) -> Swing (T4)
    // УВЕЛИЧЕНА задержка для PacketOrderB/Post
    private static final int ROTA_ATTACK_DELAY_TICKS = 4;

    // Рандомизированный кулдаун для пакетов движения/ротации
    private int movePacketCooldown = 0;
    // Диапазон задержки: 6-12 тиков (Безопасный таймер для обхода TimerLimit)
    private static final int MIN_MOVE_PACKET_DELAY = 6;
    private static final int MAX_MOVE_PACKET_DELAY = 12;
    // ----------------------------

    // НАСТРОЙКИ МОДУЛЯ
    private final ModeSetting rotate = new ModeSetting(Text.of("Ротация"), null, () -> true)
            .set("Silent", "None")
            .setDefault("Silent");

    private final SliderSetting distance = new SliderSetting(Text.of("Дистанция"), null, () -> true)
            .set(2f, 6f, 0.1f)
            .set(6.0f);

    private final SliderSetting preAimDistance = new SliderSetting(Text.of("Пред-наводка"), null, () -> true)
            .set(1.0f, 10f, 0.1f)
            .set(5.0f);

    // Вернули ПЛАВНОСТЬ для обхода AimModulo360 и Simulation
    private final SliderSetting smoothness = new SliderSetting(Text.of("Плавность (Интерполяция)"),
            Text.of("Скорость наводки/интерполяции угла за тик. 1.0 = мгновенно."), () -> rotate.getValue().equals("Silent"))
            .set(0.01f, 1.0f, 0.01f)
            .set(0.25f); // Среднее, но заметное сглаживание

    private final ModeListSetting targetTypeSetting = new ModeListSetting(Text.of("Таргет"), null, () -> true)
            .set("Игроки", "Мобы", "Друзья");

    private final BooleanSetting onlyCrits = new BooleanSetting(Text.of("Только криты"),
            Text.of("Атакует только когда можно нанести критический удар"), () -> false)
            .set(false);

    public AttackAura() {
        super(Text.of("Aura"), Category.COMBAT);
        this.addSettings(rotate, distance, preAimDistance, smoothness, targetTypeSetting, onlyCrits);
        targetTypeSetting.get("Игроки").set(true);
        targetTypeSetting.get("Мобы").set(true);
    }

    @Override
    public void activate() {
        super.activate();
        target = null;
        lastTarget = null;
        snapbackLock = false;
        rotationToAttackDelay = 0;
        movePacketCooldown = 0;
    }

    @Override
    public void deactivate() {
        target = null;
        lastTarget = null;
        rotateVector = new Vec2f(new Random().nextFloat() * 360f, 0f);
        super.deactivate();
    }

    /**
     * Основной цикл. Включает логику обхода Grim AC.
     */
    @EventHandler
    public void onTickEvent(TickEvent event) {
        if (!this.isEnabled() || mc.player == null || mc.world == null) return;

        // 0. Обновление кулдаунов AC Bypass
        if (movePacketCooldown > 0) {
            movePacketCooldown--;
        }

        target = updateTarget();

        if (rotate.getValue().equals("Silent")) {

            // 1. Вычисляем новую ротацию (ПЛАВНУЮ с Jitter'ом)
            updateServerRotation();

            ClientPlayerEntity player = mc.player;

            // --- 2. ЭТАП АТАКИ (T+1, T+2, T+3, T+4) ---
            if (rotationToAttackDelay > 0) {
                rotationToAttackDelay--;

                // T+2: Interact Entity (ROTA_ATTACK_DELAY_TICKS - 2)
                if (rotationToAttackDelay == ROTA_ATTACK_DELAY_TICKS - 2) {
                    attackTargetInteractOnly();
                }

                // T+4: Swing (rotationToAttackDelay == 0)
                if (rotationToAttackDelay == 0) {
                    swingHand();
                }
            }

            // --- 1. ЛОГИКА ИНИЦИАЦИИ АТАКИ И РОТАЦИИ (Тик А - T0) ---

            boolean isCooldownReady = player.getAttackCooldownProgress(0) >= 1.0f;
            float currentDistance = (target != null) ? player.distanceTo(target) : 0f;

            boolean canCrit = !player.isOnGround() && !player.isRiding();
            boolean critCheckPassed = !onlyCrits.getValue() || canCrit;

            boolean canAttack = target != null && isCooldownReady && currentDistance <= distance.getValue() && critCheckPassed;

            if (canAttack && rotationToAttackDelay == 0) {
                if (movePacketCooldown <= 0) {
                    // 1. Отправляем ПЕРВЫЙ пакет: Rotation-Only (Тик А)
                    sendRotationPacket(rotateVector.x, rotateVector.y);

                    // 2. Устанавливаем РАНДОМИЗИРОВАННЫЙ кулдаун для следующего пакета движения
                    movePacketCooldown = MIN_MOVE_PACKET_DELAY + random.nextInt(MAX_MOVE_PACKET_DELAY - MIN_MOVE_PACKET_DELAY + 1);

                    // 3. Устанавливаем задержку в 4 тика (для Interact/Swing)
                    rotationToAttackDelay = ROTA_ATTACK_DELAY_TICKS;
                }
            }
        }
    }


    // --- ВСПОМОГАТЕЛЬНЫЕ МЕТОДЫ ---

    private boolean isVisible(LivingEntity target) {
        if (mc.world == null || mc.player == null) return false;

        Vec3d eyePos = mc.player.getEyePos();
        Vec3d targetPos = target.getPos().add(0, target.getHeight() * 0.8, 0);

        RaycastContext context = new RaycastContext(
                eyePos,
                targetPos,
                RaycastContext.ShapeType.COLLIDER,
                RaycastContext.FluidHandling.NONE,
                mc.player
        );

        BlockHitResult result = mc.world.raycast(context);

        return result.getType() == HitResult.Type.MISS;
    }

    /**
     * Цикл аима: Вычисляет ротацию и СГЛАЖИВАЕТ ее (Интерполяция/Smoothing).
     */
    private void updateServerRotation() {
        if (mc.player == null) return;

        if (rotateVector == null) {
            rotateVector = new Vec2f(random.nextFloat() * 360f, 0f);
        }

        float currentYaw = rotateVector.x;
        float currentPitch = rotateVector.y;

        float finalTargetYaw;
        float finalTargetPitch;

        // Jitter: Постоянное и медленное колебание
        float time = System.currentTimeMillis() / 1000f;
        float baseNoiseYaw = (float) Math.sin(time * 6) * 2.0f;
        float baseNoisePitch = (float) Math.cos(time * 5) * 1.5f;

        if (target == null) {
            // Idle Jitter: Если цели нет
            finalTargetYaw = currentYaw + (random.nextFloat() - 0.5f) * 0.5f;
            finalTargetPitch = currentPitch + (random.nextFloat() - 0.5f) * 0.3f;
        } else {
            // 1. Рандомизация точки прицеливания (Focus on Upper Body)
            if (System.currentTimeMillis() - lastRandomizationTime > 50) {
                float width = target.getWidth() * 0.3f;
                float height = target.getHeight() * 0.75f;

                randomOffset = new Vec3d(
                        (random.nextGaussian() * 0.2) * width,
                        (0.5 + random.nextFloat() * 0.3) * height,
                        (random.nextGaussian() * 0.2) * width
                );
                lastRandomizationTime = System.currentTimeMillis();
            }

            // 2. Вычисляем БЕЗОПАСНЫЙ прогноз (1.0 тика)
            Vec3d targetVelocity = target.getVelocity();
            double predictionFactor = 1.0;
            Vec3d predictedPos = target.getPos().add(targetVelocity.multiply(predictionFactor));

            Vec3d targetPos = predictedPos.add(randomOffset);
            Vec3d eyePos = mc.player.getEyePos();
            Vec3d vec = targetPos.subtract(eyePos);
            double dist = vec.length();

            float idealTargetYaw = (float) (Math.toDegrees(Math.atan2(vec.z, vec.x)) - 90.0);
            float idealTargetPitch = (float) (-Math.toDegrees(Math.atan2(vec.y, dist)));

            // 3. Добавляем ПЛАВНЫЙ Дрейф/Тремор к идеальным углам (Jitter)
            finalTargetYaw = idealTargetYaw + baseNoiseYaw;
            finalTargetPitch = idealTargetPitch + baseNoisePitch;

            // 4. ИНТЕРПОЛЯЦИЯ/СГЛАЖИВАНИЕ (Aim Smoothing)
            float smoothFactor = smoothness.getValue().floatValue();

            float deltaYaw = MathHelper.wrapDegrees(finalTargetYaw - currentYaw);
            currentYaw += deltaYaw * smoothFactor;

            float deltaPitch = finalTargetPitch - currentPitch;
            currentPitch += deltaPitch * smoothFactor;

            // Обновляем ротацию с учетом интерполяции
            finalTargetYaw = currentYaw;
            finalTargetPitch = currentPitch;
        }

        // 5. Финальный случайный шум и Clamp
        float finalYaw = finalTargetYaw + (random.nextFloat() - 0.5f) * 0.05f;
        float finalPitch = finalTargetPitch + (random.nextFloat() - 0.5f) * 0.05f;

        finalPitch = MathHelper.clamp(finalPitch, -90.0F, 90.0F);

        rotateVector = new Vec2f(finalYaw, finalPitch);
    }

    /**
     * Отправляет пакет ротации PlayerMoveC2SPacket.LookAndOnGround (Rotation-Only).
     */
    private void sendRotationPacket(float yaw, float pitch) {
        if (mc.player != null && mc.player.networkHandler != null) {
            boolean onGround = mc.player.isOnGround() && !mc.player.isRiding();

            mc.player.networkHandler.sendPacket(
                    new PlayerMoveC2SPacket.LookAndOnGround(
                            yaw,
                            pitch,
                            onGround,
                            true
                    )
            );
        }
    }

    /**
     * Отправляет InteractEntity (Attack).
     */
    private void attackTargetInteractOnly() {
        if (target == null || mc.player == null || mc.interactionManager == null) return;
        // УДАР (Interact Entity) - Тик T+2
        mc.interactionManager.attackEntity(mc.player, target);
    }

    /**
     * Отправляет Swing Packet (Animation).
     */
    private void swingHand() {
        if (mc.player == null) return;
        // SWING (Animation) - Тик T+4
        mc.player.swingHand(Hand.MAIN_HAND);
    }


    /**
     * Используем универсальный поиск ближайшей цели LivingEntity. (Без изменений)
     */
    private LivingEntity updateTarget() {
        if (mc.player == null || mc.world == null) return null;

        LivingEntity currentClosest = null;
        double closestDistance = Double.MAX_VALUE;
        float searchRange = preAimDistance.getValue();

        // 1. ЛОГИКА УДЕРЖАНИЯ ЦЕЛИ (TARGET LOCK)
        if (target != null) {
            float lockDistance = distance.getValue() + 0.5f;
            if (mc.player.distanceTo(target) <= lockDistance) {
                return target;
            }
        }

        // 2. ПОИСК НОВОЙ БЛИЖАЙШЕЙ ЦЕЛИ
        for (net.minecraft.entity.Entity entity : mc.world.getEntities()) {

            if (entity instanceof LivingEntity livingEntity && livingEntity != mc.player) {

                if (livingEntity instanceof PlayerEntity playerEntity) {
                    if (playerEntity.isSpectator() || playerEntity.getAbilities().creativeMode) {
                        continue;
                    }
                }

                double distance = mc.player.distanceTo(entity);

                if (distance <= searchRange) {
                    if (distance < closestDistance) {
                        closestDistance = distance;
                        currentClosest = livingEntity;
                    }
                }
            }
        }

        // 3. ОБРАБОТКА НОВОЙ ЦЕЛИ
        if (currentClosest != lastTarget) {
            lastTarget = currentClosest;
            lastRandomizationTime = 0;
        }

        return currentClosest;
    }
}