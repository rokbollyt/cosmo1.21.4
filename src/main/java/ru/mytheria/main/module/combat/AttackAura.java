package ru.mytheria.main.module.combat;

import lombok.Getter;
import lombok.experimental.NonFinal;
import meteordevelopment.orbit.EventHandler;
import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.network.packet.c2s.play.PlayerMoveC2SPacket;
import net.minecraft.text.Text;
import net.minecraft.util.Hand;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec2f;
import net.minecraft.util.math.Vec3d;
import net.minecraft.util.hit.HitResult;
import net.minecraft.util.hit.EntityHitResult; // !!! ДОБАВЛЕН ИМПОРТ
import net.minecraft.world.RaycastContext;

import ru.mytheria.api.events.impl.TickEvent;
import ru.mytheria.api.module.Category;
import ru.mytheria.api.module.Module;
import ru.mytheria.api.module.settings.impl.BooleanSetting;
import ru.mytheria.api.module.settings.impl.ModeListSetting;
import ru.mytheria.api.module.settings.impl.ModeSetting;
import ru.mytheria.api.module.settings.impl.SliderSetting;
// Предполагается, что эти классы существуют:
import ru.mytheria.main.module.combat.aura.util.TargetEntitySelector;
import ru.mytheria.main.module.combat.aura.util.SensUtils;

import java.util.HashSet;
import java.util.Set;
import java.util.Random;

@Getter
public class AttackAura extends Module {
    // Предполагается, что TargetEntitySelector существует в вашем коде
    private final TargetEntitySelector targetSelector = new TargetEntitySelector();

    @NonFinal
    private LivingEntity target = null;
    @NonFinal
    private LivingEntity lastTarget = null;

    @NonFinal
    private Vec2f rotateVector = null;

    // === Humanized Rotation Variables ===
    private final Random random = new Random();
    private Vec3d randomOffset = new Vec3d(0, 0, 0);
    private long lastRandomizationTime = 0;

    private float recoilYaw = 0;
    private float recoilPitch = 0;
    private boolean isAttackingTick = false;

    private int rotationDelay = 0;
    private long lastAttackTime = 0;
    private final long minAttackDelay = 400; // Минимальная задержка между ударами в мс

    // Settings
    private final ModeSetting rotate = new ModeSetting(Text.of("Ротация"), null, () -> true)
            .set("Humanized", "Legit", "Snap", "None")
            .setDefault("Humanized");

    private final SliderSetting distance = new SliderSetting(Text.of("Дистанция"), null, () -> true)
            .set(2f, 6f, 0.1f)
            .set(3.5f);

    private final SliderSetting preAimDistance = new SliderSetting(Text.of("Пред-наводка"), null, () -> true)
            .set(1.0f, 10f, 0.1f)
            .set(5.0f);

    private final SliderSetting maxAngle = new SliderSetting(Text.of("Макс. Угол Наводки"),
            Text.of("Макс. угол отклонения от центра экрана для старта наводки"), () -> true)
            .set(15f, 180f, 1f)
            .set(45f);

    private final ModeListSetting targetTypeSetting = new ModeListSetting(Text.of("Таргет"), null, () -> true)
            .set("Игроки", "Голые игроки", "В броне", "Невидимые игроки",
                    "Мобы", "Животные", "Друзья", "Стойка для брони", "Жители", "Голем");

    private final BooleanSetting onlyCrits = new BooleanSetting(Text.of("Только криты"),
            Text.of("Атакует только когда можно нанести критический удар (в прыжке)"), () -> true)
            .set(false);

    public AttackAura() {
        super(Text.of("Aura"), Category.COMBAT);
        this.addSettings(rotate, distance, preAimDistance, maxAngle, targetTypeSetting, onlyCrits);
        targetTypeSetting.get("Игроки").set(true);
        targetTypeSetting.get("Мобы").set(true);
    }

    @Override
    public void activate() {
        super.activate();
        target = null;
        lastTarget = null;
        if (mc.player != null) {
            rotateVector = new Vec2f(mc.player.getYaw(), mc.player.getPitch());
        }
        isAttackingTick = false;
        recoilYaw = 0;
        recoilPitch = 0;
        rotationDelay = 0;
        lastAttackTime = 0;
    }

    @Override
    public void deactivate() {
        targetSelector.releaseTarget();
        target = null;
        lastTarget = null;
        rotateVector = null;
        super.deactivate();
    }

    @EventHandler
    public void onTickEvent(TickEvent event) {
        if (!this.isEnabled() || mc.player == null || mc.world == null) return;

        target = updateTarget();

        if (target == null) {
            rotateVector = new Vec2f(mc.player.getYaw(), mc.player.getPitch());
            return;
        }

        if (rotateVector == null) {
            rotateVector = new Vec2f(mc.player.getYaw(), mc.player.getPitch());
        }

        if (rotate.getValue().equals("Humanized") || rotate.getValue().equals("Snap")) {
            updateRotation();
        }

        // Атакуем, если можем и прошло достаточно времени с последнего удара
        if (System.currentTimeMillis() - lastAttackTime >= minAttackDelay && canAttackTarget()) {
            attackTarget();
        }
    }

    private void updateRotation() {
        if (target == null) return;

        float currentYaw = rotateVector.x;
        float currentPitch = rotateVector.y;
        float newYaw = currentYaw, newPitch = currentPitch;

        // 1. Рандомизация точки попадания (DristoTime logic)
        if (System.currentTimeMillis() - lastRandomizationTime > 100) {
            float width = target.getWidth() * 0.7f;
            float height = target.getHeight() * 0.8f;

            randomOffset = new Vec3d(
                    (random.nextDouble() - 0.5) * width * 0.6,
                    (random.nextDouble() - 0.5) * height * 0.5 + (target.getEyeHeight(target.getPose()) * 0.4),
                    (random.nextDouble() - 0.5) * width * 0.6
            );
            lastRandomizationTime = System.currentTimeMillis();
        }

        // 2. Считаем целевой вектор
        Vec3d targetPos = target.getPos().add(randomOffset);
        Vec3d eyePos = mc.player.getEyePos();
        Vec3d vec = targetPos.subtract(eyePos);

        double dist = vec.length();
        float targetYaw = (float) (Math.toDegrees(Math.atan2(vec.z, vec.x)) - 90.0);
        float targetPitch = (float) (-Math.toDegrees(Math.atan2(vec.y, dist)));

        // --- УСИЛЕННЫЙ HUMANIZED / DRISTOTIME ЛОГИКА ---
        if (rotate.getValue().equals("Humanized")) {

            float yawDeltaAbs = Math.abs(MathHelper.wrapDegrees(targetYaw - currentYaw));
            float pitchDeltaAbs = Math.abs(MathHelper.wrapDegrees(targetPitch - currentPitch));
            float totalDelta = yawDeltaAbs + pitchDeltaAbs;

            // 4. FOV Check
            if (totalDelta > maxAngle.getValue()) {
                if (target != lastTarget) rotationDelay = 3;
                newYaw = currentYaw;
                newPitch = currentPitch;
            } else {

                // 3. Логика отдачи (Recoil/Drift)
                if (isAttackingTick) {
                    float recoilForce = 8.0f + random.nextFloat() * 6.0f;
                    recoilPitch -= (float) (5.0 + Math.random() * recoilForce);
                    recoilYaw += (float) ((random.nextFloat() - 0.5) * recoilForce * 4.0);
                    isAttackingTick = false;
                    rotationDelay = 2 + random.nextInt(3);
                }

                recoilYaw *= 0.80f;
                recoilPitch *= 0.80f;

                targetYaw += recoilYaw;
                targetPitch += recoilPitch;

                // FIX: Добавляем постоянный Ambient Noise (DristoTime style)
                targetYaw += (random.nextFloat() - 0.5f) * 0.6f;
                targetPitch += (random.nextFloat() - 0.5f) * 0.6f;

                float yawDelta = MathHelper.wrapDegrees(targetYaw - currentYaw);
                float pitchDelta = MathHelper.wrapDegrees(targetPitch - currentPitch);

                // LERP Factor based on distance (Ease Out)
                float baseLerp = 0.5f + (random.nextFloat() * 0.3f);

                // Более агрессивное ускорение, чтобы не "бить через раз"
                float speedMultiplier = (float) Math.min(1.0, Math.max(yawDeltaAbs, pitchDeltaAbs) / 15.0);
                float lerpFactor = baseLerp * (0.6f + speedMultiplier * 0.4f);

                if (rotationDelay > 0) {
                    lerpFactor *= 0.1f;
                    rotationDelay--;
                }

                newYaw = MathHelper.lerp(lerpFactor, currentYaw, currentYaw + yawDelta);
                newPitch = MathHelper.lerp(lerpFactor, currentPitch, currentPitch + pitchDelta);

                // GCD Fix
                float sensitivity = mc.options.getMouseSensitivity().getValue().floatValue();
                // Предполагается, что SensUtils.getGCDValue() возвращает правильный GCD
                float gcd = SensUtils.getGCDValue();

                float yawChange = newYaw - currentYaw;
                float pitchChange = newPitch - currentPitch;

                yawChange -= yawChange % gcd;
                pitchChange -= pitchChange % gcd;

                newYaw = currentYaw + yawChange;
                newPitch = currentPitch + pitchChange;
            }
        } else if (rotate.getValue().equals("Snap")) {
            // Snap logic (можно дописать, если нужно)
            float yawDelta = MathHelper.wrapDegrees(targetYaw - currentYaw);
            float pitchDelta = MathHelper.wrapDegrees(targetPitch - currentPitch);

            float snapSpeed = 180.0f + (random.nextFloat() * 20.0f);

            newYaw = currentYaw + MathHelper.clamp(yawDelta, -snapSpeed, snapSpeed);
            newPitch = currentPitch + MathHelper.clamp(pitchDelta, -snapSpeed, snapSpeed);
        }

        // 5. АКТИВНОЕ ОПУСКАНИЕ ПИТЧА ДЛЯ КРИТОВ
        if (onlyCrits.getValue() && !mc.player.isOnGround() && mc.player.fallDistance > 0.0f) {
            float critPitchTarget = 89.9f;
            float critSpeed = 0.6f + random.nextFloat() * 0.2f;
            newPitch = MathHelper.lerp(critSpeed, newPitch, critPitchTarget);
        }

        newPitch = MathHelper.clamp(newPitch, -90.0F, 90.0F);

        // 6. ФИЛЬТРАЦИЯ ПАКЕТОВ

        float deltaYaw = MathHelper.wrapDegrees(newYaw - currentYaw);
        float deltaPitch = MathHelper.wrapDegrees(newPitch - currentPitch);

        float rotationChangeThreshold = 0.05f;

        boolean shouldSendPacket = Math.abs(deltaYaw) > rotationChangeThreshold ||
                Math.abs(deltaPitch) > rotationChangeThreshold ||
                (onlyCrits.getValue() && !mc.player.isOnGround());

        if (rotationDelay > 0) {
            shouldSendPacket = false;
        }

        if (shouldSendPacket) {
            rotateVector = new Vec2f(newYaw, newPitch);

            mc.player.setBodyYaw(newYaw);
            mc.player.setHeadYaw(newYaw);

            sendRotationPacket(newYaw, newPitch);
        }
    }

    private void sendRotationPacket(float yaw, float pitch) {
        if (mc.player != null && mc.player.networkHandler != null) {
            mc.player.networkHandler.sendPacket(
                    new PlayerMoveC2SPacket.LookAndOnGround(
                            yaw,
                            pitch,
                            mc.player.isOnGround(),
                            false
                    )
            );
        }
    }

    /**
     * Проверяет, готов ли игрок к атаке, соблюдает ли кулдаун и условия крита.
     */
    private boolean canAttackTarget() {
        if (target == null || mc.player == null) return false;

        double distanceToTarget = mc.player.distanceTo(target);
        if (distanceToTarget > distance.getValue()) return false;

        float cooldown = mc.player.getAttackCooldownProgress(0.0f);
        if (cooldown < 1.0f) return false;

        // Hitbox Check (проверка, что цель находится в прицеле)
        Vec3d start = mc.player.getEyePos();
        Vec3d end = start.add(mc.player.getRotationVec(1.0f).multiply(distance.getValue()));

        RaycastContext context = new RaycastContext(start, end, RaycastContext.ShapeType.COLLIDER, RaycastContext.FluidHandling.NONE, mc.player);
        HitResult hit = mc.world.raycast(context);

        if (hit.getType() == HitResult.Type.ENTITY) {
            if (hit instanceof EntityHitResult entityHit) {
                if (entityHit.getEntity() != target) {
                    // Мы попали в другую сущность, а не в нашу цель.
                    return false;
                }
            }
        } else if (hit.getType() == HitResult.Type.MISS) {
            // Если промахнулись совсем, значит, цель не в центре прицела
            return false;
        } else if (hit.getType() == HitResult.Type.BLOCK) {
            // Если попали в блок, цель за блоком
            return false;
        }

        // Логика для "Только Криты"
        if (onlyCrits.getValue()) {
            boolean isFallingOrJumping = !mc.player.isOnGround() && mc.player.fallDistance > 0.0f;
            boolean notInLiquid = !mc.player.isTouchingWater() && !mc.player.isInLava();
            boolean movementCheck = !mc.player.isClimbing() && !mc.player.hasVehicle();

            // Проверка, что наш Питч достаточно низкий для крита (от 65.0f)
            boolean pitchIsLow = rotateVector.y >= 65.0f;

            if (!isFallingOrJumping || !notInLiquid || !movementCheck || !pitchIsLow) {
                return false;
            }
        }

        return true;
    }

    /**
     * Выполняет атаку по цели и устанавливает флаг отдачи.
     */
    private void attackTarget() {
        if (mc.interactionManager == null || mc.player == null || target == null) return;

        if (!target.isAlive() || mc.player.distanceTo(target) > distance.getValue()) {
            return;
        }

        mc.interactionManager.attackEntity(mc.player, target);
        mc.player.swingHand(Hand.MAIN_HAND);

        isAttackingTick = true;
        lastAttackTime = System.currentTimeMillis();
    }

    /**
     * Обновляет текущую цель, используя TargetEntitySelector.
     */
    private LivingEntity updateTarget() {
        Set<TargetEntitySelector.TargetType> targetTypes = new HashSet<>();
        // Заполнение targetTypes
        for (String selected : targetTypeSetting.getSelected()) {
            for (TargetEntitySelector.TargetType type : TargetEntitySelector.TargetType.values()) {
                // Предполагается, что TargetType имеет getDisplayName()
                if (type.getDisplayName().equals(selected)) {
                    targetTypes.add(type);
                    break;
                }
            }
        }

        LivingEntity newTarget = targetSelector.updateTarget(targetTypes, mc.world.getEntities(), preAimDistance.getValue());
        if (newTarget != lastTarget) {
            lastTarget = newTarget;
            lastRandomizationTime = 0;
            rotationDelay = 3; // Имитация задержки реакции на новую цель
        }
        return newTarget;
    }
}