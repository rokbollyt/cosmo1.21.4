package ru.mytheria.main.module.combat.aura.rotation.server.funtime;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.experimental.FieldDefaults;
import net.minecraft.entity.Entity;
import net.minecraft.util.math.Vec3d;
import ru.mytheria.api.clientannotation.QuickImport;
import ru.mytheria.main.module.combat.aura.angle.Angle;
import ru.mytheria.main.module.combat.aura.angle.AngleMode;
import ru.mytheria.main.module.combat.aura.angle.implementation.LinearSmoothAngle;
import ru.mytheria.main.module.combat.aura.rotation.Rotation;
import ru.mytheria.main.module.combat.aura.rotation.RotationController;

import java.util.Random;

@Getter
@FieldDefaults(level = AccessLevel.PRIVATE)
public class FuntimeRotation extends RotationController implements QuickImport {

    final Random random = new Random();
    boolean isInHitbox = false;
    boolean isAccelerating = false;
    float currentSpeedMultiplier = 1.0f;
    long accelerationStartTime = 0;
    int accelerationDelay = 0;
    float lastYaw = 0;
    float lastPitch = 0;

    public FuntimeRotation() {
        super(new LinearSmoothAngle(), true, true, true);
    }

    public FuntimeRotation(AngleMode angleSmooth, boolean changeView, boolean moveCorrection, boolean freeCorrection) {
        super(angleSmooth, changeView, moveCorrection, freeCorrection);
    }

    public Rotation createFuntimeRotation(Entity target, Angle currentAngle, boolean isAttacking) {
        if (target == null || mc.player == null) return null;

        Vec3d targetPos = target.getPos().add(0, target.getEyeHeight(target.getPose()) * 0.9, 0);
        Vec3d eyePos = mc.player.getEyePos();
        Vec3d vec = targetPos.subtract(eyePos);

        // Расчет углов к цели
        float yawToTarget = (float) Math.toDegrees(Math.atan2(vec.z, vec.x)) - 90;
        float pitchToTarget = (float) -Math.toDegrees(Math.atan2(vec.y, Math.sqrt(vec.x * vec.x + vec.z * vec.z)));

        yawToTarget = wrapDegrees(yawToTarget);

        // Обновление состояния хитбокса
        boolean wasInHitbox = isInHitbox;
        isInHitbox = isCursorOnHitbox(yawToTarget, pitchToTarget, currentAngle, target);

        if (isInHitbox != wasInHitbox) {
            if (isInHitbox) {
                currentSpeedMultiplier = 0.2f;
                isAccelerating = false;
            } else {
                isAccelerating = true;
                accelerationStartTime = System.currentTimeMillis();
                accelerationDelay = 100 + random.nextInt(21);
            }
        }

        // Ускорение
        if (isAccelerating && System.currentTimeMillis() - accelerationStartTime >= accelerationDelay) {
            float accelerationProgress = Math.min((System.currentTimeMillis() - accelerationStartTime - accelerationDelay) / 200f, 1f);
            currentSpeedMultiplier = 0.2f + accelerationProgress * 0.8f;
            if (accelerationProgress >= 1f) {
                isAccelerating = false;
                currentSpeedMultiplier = 1.0f;
            }
        }

        float yaw;
        float pitch;

        float baseYawSpeed = 80.0f * 1.5f * currentSpeedMultiplier;
        float basePitchSpeed = 35.0f * 1.5f * currentSpeedMultiplier;

        float yawDelta = wrapDegrees(yawToTarget - currentAngle.getYaw());
        float pitchDelta = wrapDegrees(pitchToTarget - currentAngle.getPitch());

        // Если атакуем выбранную цель
        if (isAttacking) {
            float attackSpeedMultiplier = 1.0f * currentSpeedMultiplier;
            yaw = currentAngle.getYaw() + yawDelta * attackSpeedMultiplier;
            pitch = clamp(currentAngle.getPitch() + pitchDelta * attackSpeedMultiplier, -89.0F, 89.0F);
        } else {
            float dynamicYawSpeed = calculateDynamicSpeed(Math.abs(yawDelta), baseYawSpeed);
            float dynamicPitchSpeed = calculateDynamicSpeed(Math.abs(pitchDelta), basePitchSpeed);

            float yawSpeed = Math.min(Math.max(Math.abs(yawDelta), 1.0f), dynamicYawSpeed);
            float pitchSpeed = Math.min(Math.max(Math.abs(pitchDelta), 1.0f), dynamicPitchSpeed);

            float accelerationFactor = calculateAccelerationFactor(Math.abs(yawDelta));
            yawSpeed *= accelerationFactor;
            pitchSpeed *= accelerationFactor;

            yaw = currentAngle.getYaw() + (yawDelta > 0 ? yawSpeed : -yawSpeed);
            pitch = clamp(currentAngle.getPitch() + (pitchDelta > 0 ? pitchSpeed : -pitchSpeed), -89.0F, 89.0F);
        }

        // Круговое движение
        float time = mc.player.age * 0.8f;
        float circleAmplitude = 1.8f + (float) Math.sin(time * 0.4f) * 2.2f;
        yaw += (float) Math.sin(time * 1.3f) * circleAmplitude * 0.9f;
        pitch += (float) Math.cos(time * 1.1f) * circleAmplitude * 0.7f;

        // Хаос
        float chaosFactor = isInHitbox ? 0.1f : 0.3f;
        if (mc.player.age % 2 == 0) {
            yaw += (random.nextFloat() - 0.5f) * chaosFactor;
            pitch += (random.nextFloat() - 0.5f) * chaosFactor * 0.6f;
        }

        // Плавные колебания
        float smoothSway = (float) Math.sin(time * 2.1f) * 0.008f;
        float smoothBob = (float) Math.cos(time * 1.8f) * 0.015f;
        yaw += smoothSway;
        pitch += smoothBob;

        // GCD коррекция
        float gcd = getGCDValue();
        float gcdRandomizer = 1.85f + random.nextFloat() * 0.45f;
        yaw -= (yaw - currentAngle.getYaw()) % (gcd * gcdRandomizer);
        pitch -= (pitch - currentAngle.getPitch()) % (gcd * gcdRandomizer);

        // Ограничение максимального изменения
        float maxYawChange = 32.0f + (float) Math.sin(time * 0.7f) * 6.0f;
        float maxPitchChange = 25.0f + (float) Math.cos(time * 0.8f) * 5.0f;

        yaw = currentAngle.getYaw() + clamp(yaw - currentAngle.getYaw(), -maxYawChange, maxYawChange);
        pitch = clamp(currentAngle.getPitch() + clamp(pitch - currentAngle.getPitch(), -maxPitchChange, maxPitchChange), -89.0F, 89.0F);

        lastYaw = yaw;
        lastPitch = pitch;

        // Создание нового Angle
        Angle newAngle = new Angle(yaw, pitch);

        // Создание Rotation плана
        return createRotationPlan(newAngle, targetPos, target);
    }

    // Вспомогательные методы
    private float wrapDegrees(float angle) {
        angle %= 360.0f;
        if (angle >= 180.0f) angle -= 360.0f;
        if (angle < -180.0f) angle += 360.0f;
        return angle;
    }

    private float clamp(float value, float min, float max) {
        return Math.max(min, Math.min(max, value));
    }

    private boolean isCursorOnHitbox(float yawToTarget, float pitchToTarget, Angle currentAngle, Entity target) {
        float yawDiff = Math.abs(wrapDegrees(yawToTarget - currentAngle.getYaw()));
        float pitchDiff = Math.abs(wrapDegrees(pitchToTarget - currentAngle.getPitch()));

        // Порог попадания в хитбокс
        float hitboxThreshold = 3.0f;
        return yawDiff <= hitboxThreshold && pitchDiff <= hitboxThreshold;
    }

    private float getGCDValue() {
        // Исправление: приведение double к float
        double sensitivity = mc.options.getMouseSensitivity().getValue();
        return (float) (0.15 * (sensitivity * sensitivity * sensitivity) + 0.35);
    }

    private float calculateDynamicSpeed(float angleDelta, float maxSpeed) {
        if (angleDelta > 45f) {
            return maxSpeed * 0.6f;
        } else if (angleDelta > 20f) {
            return maxSpeed * 0.8f;
        } else if (angleDelta > 5f) {
            return maxSpeed * 1.2f;
        } else {
            return maxSpeed * 0.9f;
        }
    }

    private float calculateAccelerationFactor(float angleDelta) {
        float progress = Math.min(angleDelta / 90f, 1.0f);

        if (progress > 0.7f) {
            return 0.3f + (progress - 0.7f) / 0.3f * 0.7f;
        } else if (progress > 0.3f) {
            return 1.0f;
        } else {
            return 0.3f + progress / 0.3f * 0.7f;
        }
    }
}