package ru.mytheria.main.module.combat;

import lombok.Getter;
import lombok.experimental.NonFinal;
import meteordevelopment.orbit.EventHandler;
import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.network.packet.c2s.play.PlayerMoveC2SPacket;
// ИСПРАВЛЕНИЕ: Пакет возвращен на net.minecraft.network.packet.c2s.play, что является стандартом для Fabric 1.21.4
import net.minecraft.network.packet.c2s.play.EntityActionC2SPacket;
import net.minecraft.text.Text;
import net.minecraft.util.Hand;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec2f;
import net.minecraft.util.math.Vec3d;

import ru.mytheria.api.events.impl.TickEvent;
import ru.mytheria.api.events.impl.EventRender3D;
import ru.mytheria.api.module.Category;
import ru.mytheria.api.module.Module;
import ru.mytheria.api.module.settings.impl.BooleanSetting;
import ru.mytheria.api.module.settings.impl.ModeListSetting;
import ru.mytheria.api.module.settings.impl.ModeSetting;
import ru.mytheria.api.module.settings.impl.SliderSetting;

import java.security.SecureRandom;
import java.util.Random;

// Класс для хранения углов (Turns в оригинале)
record Turns(float yaw, float pitch) {}

// Класс для расчета разницы углов (MathAngle.calculateDelta в оригинале)
class MathAngle {
    public static Turns calculateDelta(Vec2f current, Vec2f target) {
        float yawDelta = MathHelper.wrapDegrees(target.x - current.x);
        float pitchDelta = target.y - current.y;
        return new Turns(yawDelta, pitchDelta);
    }
}

@Getter
public class AttackAura extends Module {
    private static final net.minecraft.client.MinecraftClient mc = net.minecraft.client.MinecraftClient.getInstance();
    private static final SecureRandom secureRandom = new SecureRandom();

    @NonFinal
    private LivingEntity target = null;
    @NonFinal
    private LivingEntity lastTarget = null;

    @NonFinal
    private Vec2f rotateVector = new Vec2f(secureRandom.nextFloat() * 360f, 0f);
    @NonFinal
    private Vec2f snapTargetRotation = new Vec2f(0f, 0f);
    private final Random random = new Random();
    private Vec3d randomOffset = new Vec3d(0, 0, 0);
    private long lastRandomizationTime = 0;
    private static final long RANDOMIZATION_INTERVAL = 150;

    // --- Grim AC Bypass Logic V29 ---
    private int attackSequenceDelay = 0;
    private static final int ATTACK_SEQUENCE_LENGTH = 1; // T0=Rotation (Snap), T1=Interact+Swing

    private int movePacketCooldown = 0;
    private static final int MIN_MOVE_PACKET_DELAY = 6;
    private static final int MAX_MOVE_PACKET_DELAY = 12;

    // Настройки
    // >>> МОДИФИКАЦИЯ: Добавлен режим "FunTime Focused" <<<
    private final ModeSetting rotate = new ModeSetting(Text.of("Ротация"), null, () -> true).set("Silent", "None", "FunTime Focused").setDefault("FunTime Focused");

    private final SliderSetting distance = new SliderSetting(Text.of("Дистанция"), null, () -> true).set(2f, 6f, 0.1f).set(6.0f);
    private final SliderSetting snapSpeed = new SliderSetting(Text.of("Скорость рывка (Snap Multiplier)"), null, () -> true)
            .set(0.0f, 1.0f, 0.05f).set(0.5f);

    // >>> НОВЫЕ НАСТРОЙКИ ДЛЯ FunTime Focused <<<
    private final SliderSetting focusSpeed = new SliderSetting(Text.of("Скорость Фокуса"), null, () -> rotate.getValue().equals("FunTime Focused"))
            .set(0.1f, 1.0f, 0.01f).set(0.85f);
    private final SliderSetting jitterStrength = new SliderSetting(Text.of("Сила Тряски"), null, () -> rotate.getValue().equals("FunTime Focused"))
            .set(0.0f, 0.5f, 0.01f).set(0.05f);
    // >>> КОНЕЦ НОВЫХ НАСТРОЕК <<<

    private final ModeSetting sprintMode = new ModeSetting(Text.of("Обход Спринта"), null, () -> true).set("None", "Grim", "Legit").setDefault("Grim");
    private final BooleanSetting breakShield = new BooleanSetting(Text.of("Ломать щит"), null, () -> true).set(true);
    private final BooleanSetting unblockShield = new BooleanSetting(Text.of("Отжимать щит"), null, () -> false).set(false);
    private final BooleanSetting onlyCrits = new BooleanSetting(Text.of("Только криты"), null, () -> false).set(false);


    public AttackAura() {
        super(Text.of("Aura"), Category.COMBAT);
        // >>> МОДИФИКАЦИЯ: Добавлены focusSpeed и jitterStrength <<<
        this.addSettings(rotate, distance, snapSpeed, focusSpeed, jitterStrength, sprintMode, breakShield, unblockShield, onlyCrits);
    }

    @Override
    public void activate() {
        super.activate();
        target = null;
        lastTarget = null;
        attackSequenceDelay = 0;
        movePacketCooldown = 0;
        if (mc.player != null) {
            rotateVector = new Vec2f(mc.player.getYaw(), mc.player.getPitch());
        }
    }

    @Override
    public void deactivate() {
        target = null;
        lastTarget = null;
        super.deactivate();
    }

    // =========================================================================
    // >>>>> MOUSE SENSITIVITY UTILS (GCD) <<<<<
    // =========================================================================

    private float getGCDValue() {
        if (mc.options.getMouseSensitivity() == null) return 0.001f;

        // Формула из MakimaAngle
        float sens = (float) (mc.options.getMouseSensitivity().getValue() * 0.6 + 0.2);
        float t = sens * sens * sens * 8.0f;
        return t * 0.15f;
    }

    private float randomLerp(float min, float max) {
        return MathHelper.lerp(secureRandom.nextFloat(), min, max);
    }

    // =========================================================================
    // >>>>> 1. ROTATION LOGIC ON RENDER (Main Handler) <<<<<
    // =========================================================================

    @EventHandler
    public void onRender3D(EventRender3D event) {
        if (!this.isEnabled() || mc.player == null || mc.world == null || rotate.getValue().equals("None")) return;

        target = updateTarget();

        if (target != null) {
            Vec2f idealTargetRot = calculateIdealRotation(target);

            if (rotate.getValue().equals("FunTime Focused")) {
                applyFunTimeFocusedRotation(idealTargetRot, target);
            } else { // "Silent" (Makima/LG Hybrid)
                applyMakimaRotation(idealTargetRot, target);
            }

            if (attackSequenceDelay == ATTACK_SEQUENCE_LENGTH) {
                snapTargetRotation = rotateVector;
            }
        } else {
            applyIdleJitter();
        }
    }

    // >>>>> НОВАЯ ЛОГИКА: FunTime Focused Rotation <<<<<
    private void applyFunTimeFocusedRotation(Vec2f idealRotation, LivingEntity entity) {
        if (mc.player == null) return;

        Turns angleDelta = MathAngle.calculateDelta(rotateVector, idealRotation);
        float yawDelta = angleDelta.yaw();
        float pitchDelta = angleDelta.pitch();

        float rotationDifference = (float) Math.hypot(Math.abs(yawDelta), Math.abs(pitchDelta));
        boolean isLookingAtTarget = rotationDifference < 10.0f; // Условие для джиттера

        // --- 1. DYNAMIC SPEED (FunTime Focused: Yaw=1.0, Pitch=0.5 priority) ---
        float baseSpeed = focusSpeed.getValue().floatValue();
        float dynamicYawSpeed = baseSpeed * 1.0f; // Приоритет Yaw
        float dynamicPitchSpeed = baseSpeed * 0.5f; // Пониженный Pitch

        float targetYawDelta = yawDelta * dynamicYawSpeed;
        float targetPitchDelta = pitchDelta * dynamicPitchSpeed;

        // --- 2. MINIMIZED JITTER ---
        if (isLookingAtTarget) {
            float jitter = jitterStrength.getValue().floatValue();
            targetYawDelta += (float) secureRandom.nextGaussian() * jitter;
            targetPitchDelta += (float) secureRandom.nextGaussian() * jitter * 0.5f; // Pitch Jitter ниже
        }

        // --- 3. GCD CORRECTION ---
        float gcd = getGCDValue();
        targetYawDelta -= (targetYawDelta % gcd);
        targetPitchDelta -= (targetPitchDelta % gcd);

        // --- 4. APPLY ---
        float fixYaw = rotateVector.x + targetYawDelta;
        float fixPitch = rotateVector.y + targetPitchDelta;
        fixPitch = MathHelper.clamp(fixPitch, -90.0F, 90.0F);

        rotateVector = new Vec2f(MathHelper.wrapDegrees(fixYaw), fixPitch);
    }

    // >>>>> СТАРЫЙ МЕТОД (переименован для новой логики) <<<<<
    private void applyMakimaRotation(Vec2f idealRotation, LivingEntity entity) {
        if (mc.player == null) return;

        Turns angleDelta = MathAngle.calculateDelta(rotateVector, idealRotation);
        float yawDelta = angleDelta.yaw();
        float pitchDelta = angleDelta.pitch();

        float rotationDifference = (float) Math.hypot(Math.abs(yawDelta), Math.abs(pitchDelta));
        boolean isLookingAtTarget = rotationDifference < 30.0f;
        boolean canAttack = mc.player.getAttackCooldownProgress(0) >= 1.0f;
        float dist = (entity != null) ? (float) mc.player.distanceTo(entity) : 3.0f;

        // --- 1. DYNAMIC SPEED (Makima/LG Hybrid) ---
        float speed;
        if (!isLookingAtTarget) {
            speed = randomLerp(0.95F, 1.0F);
        } else {
            float accuracyFactor = MathHelper.clamp(rotationDifference / 20.0f, 0.2f, 1.0f);
            float baseSpeed = canAttack ? randomLerp(0.9F, 0.98F) : randomLerp(0.5F, 0.7F);
            speed = baseSpeed * accuracyFactor;

            if (dist > 0 && dist < 0.66F) {
                float closeRangeSpeed = MathHelper.clamp(dist / 1.5F * 0.35F, 0.1F, 0.6F);
                speed = canAttack ? 0.85f : Math.min(speed, closeRangeSpeed);
            }
        }

        // --- 2. SMOOTHING (Makima Logic) ---
        float div = (rotationDifference < 0.0001f) ? 0.0001f : rotationDifference;
        float lineYaw = (Math.abs(yawDelta / div) * 180F);
        float linePitch = (Math.abs(pitchDelta / div) * 180F);

        float targetYawDelta = MathHelper.clamp(yawDelta, -lineYaw, lineYaw) * speed;
        float targetPitchDelta = MathHelper.clamp(pitchDelta, -linePitch, linePitch) * speed;

        // --- 3. JITTER/SHAKE (Makima Gaussian Jitter) ---
        if (isLookingAtTarget) {
            float distFactor = MathHelper.clamp(dist / 4.0f, 0.3f, 1.0f);
            float gaussianYaw = (float) secureRandom.nextGaussian();
            float gaussianPitch = (float) secureRandom.nextGaussian();
            float movementStress = MathHelper.clamp(rotationDifference / 10.0f, 0.5f, 1.5f);

            float shakeStrengthYaw = 3.5f * distFactor * movementStress;
            float shakeStrengthPitch = 2.0f * distFactor * movementStress;

            targetYawDelta += gaussianYaw * shakeStrengthYaw;
            targetPitchDelta += gaussianPitch * shakeStrengthPitch;
        }

        // --- 4. GCD CORRECTION ---
        float gcd = getGCDValue();
        targetYawDelta -= (targetYawDelta % gcd);
        targetPitchDelta -= (targetPitchDelta % gcd);

        // --- 5. APPLY ---
        float fixYaw = rotateVector.x + targetYawDelta;
        float fixPitch = rotateVector.y + targetPitchDelta;
        fixPitch = MathHelper.clamp(fixPitch, -90.0F, 90.0F);

        rotateVector = new Vec2f(MathHelper.wrapDegrees(fixYaw), fixPitch);
    }
    // >>>>> КОНЕЦ СТАРОГО МЕТОДА <<<<<


    private void applyIdleJitter() {
        if (mc.player == null) return;

        float baseSpeed = 0.35F;
        double time = System.currentTimeMillis() / 50D;

        float jitterYaw = (float) (randomLerp(18, 27) * Math.sin(time));
        float jitterPitch = (float) (randomLerp(15, 22) * Math.sin(System.currentTimeMillis() / 13D));

        float moveYaw = MathHelper.lerp(baseSpeed, rotateVector.x, rotateVector.x + jitterYaw);
        float movePitch = MathHelper.lerp(baseSpeed, rotateVector.y, rotateVector.y + jitterPitch);

        rotateVector = new Vec2f(MathHelper.wrapDegrees(moveYaw), MathHelper.clamp(movePitch, -90.0F, 90.0F));
    }

    private Vec2f calculateIdealRotation(LivingEntity entity) {
        if (mc.player == null) return rotateVector;

        Vec3d targetVelocity = entity.getVelocity();
        double predictionFactor = 0.5 + random.nextDouble() * 0.5;
        Vec3d predictedPos = entity.getPos().add(targetVelocity.multiply(predictionFactor));

        if (System.currentTimeMillis() - lastRandomizationTime > RANDOMIZATION_INTERVAL) {
            float width = entity.getWidth() * 0.3f;
            float height = entity.getHeight() * 0.75f;

            randomOffset = new Vec3d(
                    (secureRandom.nextGaussian() * 0.3) * width,
                    (0.6 + secureRandom.nextFloat() * 0.4) * height,
                    (secureRandom.nextGaussian() * 0.3) * width
            );
            lastRandomizationTime = System.currentTimeMillis();
        }

        Vec3d targetPos = predictedPos.add(randomOffset);
        Vec3d eyePos = mc.player.getEyePos();
        Vec3d vec = targetPos.subtract(eyePos);
        double dist = vec.length();

        float finalYaw = (float) (Math.toDegrees(Math.atan2(vec.z, vec.x)) - 90.0);
        float finalPitch = (float) (-Math.toDegrees(Math.atan2(vec.y, dist)));

        finalYaw = MathHelper.wrapDegrees(finalYaw);

        return new Vec2f(finalYaw, MathHelper.clamp(finalPitch, -90.0F, 90.0F));
    }

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


    // =========================================================================
    // >>>>> 2. LOGIC ON TICK (20 Hz) - ATTACK AND BYPASS LOGIC <<<<<
    // =========================================================================

    @EventHandler
    public void onTickEvent(TickEvent event) {
        if (!this.isEnabled() || mc.player == null || mc.world == null || rotate.getValue().equals("None")) return;

        if (movePacketCooldown > 0) movePacketCooldown--;

        // --- 1. СЕКВЕНЦИЯ АТАКИ (2 ТИКА: R -> I+S) ---
        if (attackSequenceDelay > 0) {

            // T0 (attackSequenceDelay == 1): Smooth Snap Rotation
            if (attackSequenceDelay == ATTACK_SEQUENCE_LENGTH) {

                float snapMultiplier = snapSpeed.getValue().floatValue();

                float interpolatedYaw = rotateVector.x + MathHelper.wrapDegrees(snapTargetRotation.x - rotateVector.x) * snapMultiplier;
                float interpolatedPitch = rotateVector.y + (snapTargetRotation.y - rotateVector.y) * snapMultiplier;

                sendRotationPacket(interpolatedYaw, interpolatedPitch);
            }

            // T1 (attackSequenceDelay == 0): Interact Entity + Swing
            else if (attackSequenceDelay == ATTACK_SEQUENCE_LENGTH - 1) {
                if (target != null && mc.player.getAttackCooldownProgress(0) >= 1.0f) {
                    updateAttack();
                }
            }

            attackSequenceDelay--;

        } else {
            // --- 2. НОРМАЛЬНАЯ ОТПРАВКА (Makima/LG ротация или FunTime) ---
            sendRotationPacket(rotateVector.x, rotateVector.y);
        }

        // --- 3. ЛОГИКА ИНИЦИАЦИИ АТАКИ ---
        boolean isCooldownReady = mc.player.getAttackCooldownProgress(0) >= 1.0f;
        float currentDistance = (target != null) ? mc.player.distanceTo(target) : 0f;

        boolean canCrit = !mc.player.isOnGround() && !mc.player.isRiding();
        boolean critCheckPassed = !onlyCrits.getValue() || canCrit;

        boolean canAttack = target != null && isCooldownReady && currentDistance <= distance.getValue() && critCheckPassed;

        if (canAttack && attackSequenceDelay == 0) {
            if (movePacketCooldown <= 0) {
                movePacketCooldown = MIN_MOVE_PACKET_DELAY + random.nextInt(MAX_MOVE_PACKET_DELAY - MIN_MOVE_PACKET_DELAY + 1);
                attackSequenceDelay = ATTACK_SEQUENCE_LENGTH;
            }
        }
    }

    private void updateAttack() {
        if (target == null || mc.player == null || mc.interactionManager == null) return;

        // 1. Отжать щит
        if (mc.player.isBlocking() && unblockShield.getValue()) {
            mc.interactionManager.onStoppedUsingItem(mc.player);
        }

        // 2. Атака и Свинг
        mc.interactionManager.attackEntity(mc.player, target);
        mc.player.swingHand(Hand.MAIN_HAND);

        // 3. Обход спринта (Grim/Legit)
        if (mc.player.isSprinting()) {
            String mode = sprintMode.getValue();
            if (mode.equals("Grim") || mode.equals("Legit")) {
                // Используем исправленный пакет
                mc.player.networkHandler.sendPacket(new EntityActionC2SPacket(mc.player, EntityActionC2SPacket.Action.STOP_SPRINTING));
            }
            if (mode.equals("Legit")) {
                mc.player.setSprinting(false);
            }
        }

        // 4. Ломать щит
        if (target instanceof PlayerEntity player && breakShield.getValue()) {
            // Здесь должна быть логика breakShieldPlayer(player);
        }
    }

    private LivingEntity updateTarget() {
        if (mc.player == null || mc.world == null) return null;

        LivingEntity currentClosest = null;
        double closestDistance = Double.MAX_VALUE;
        float searchRange = distance.getValue() * 1.5f;

        if (target != null) {
            float lockDistance = distance.getValue() + 0.5f;
            if (mc.player.distanceTo(target) <= lockDistance) {
                return target;
            }
        }

        for (net.minecraft.entity.Entity entity : mc.world.getEntities()) {
            if (entity instanceof LivingEntity livingEntity && livingEntity != mc.player) {
                if (livingEntity instanceof PlayerEntity playerEntity && (playerEntity.isSpectator() || playerEntity.getAbilities().creativeMode)) {
                    continue;
                }
                double dist = mc.player.distanceTo(entity);

                if (dist <= searchRange) {
                    if (dist < closestDistance) {
                        closestDistance = dist;
                        currentClosest = livingEntity;
                    }
                }
            }
        }

        if (currentClosest != lastTarget) {
            lastTarget = currentClosest;
            lastRandomizationTime = 0;
        }

        return currentClosest;
    }
}