package fun.cosmo.main.module.combat.aura.angle.implementation;

import net.minecraft.entity.Entity;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;
import fun.cosmo.main.module.combat.aura.angle.Angle;
import fun.cosmo.main.module.combat.aura.angle.AngleHandler;
import fun.cosmo.main.module.combat.aura.angle.AngleMode;

import java.util.Random;

public class FuntimeAngle extends AngleMode {

    private final Random random = new Random();

    public FuntimeAngle() {
        super("Funtime");
    }

    @Override
    public Angle limitAngleChange(Angle currentAngle, Angle targetAngle, Vec3d vec3d, Entity entity) {
        Angle delta = AngleHandler.calculateDelta(currentAngle, targetAngle);

        float yawDelta = delta.getYaw();
        float pitchDelta = delta.getPitch();

        float rotationDiff = (float) Math.sqrt(yawDelta * yawDelta + pitchDelta * pitchDelta);

        if (rotationDiff < 0.15f) {
            return targetAngle;
        }

        float lineYaw = (yawDelta / rotationDiff) * 180.0F;
        float linePitch = (pitchDelta / rotationDiff) * 180.0F;

        float clampedYaw = MathHelper.clamp(yawDelta, -Math.abs(lineYaw), Math.abs(lineYaw));
        float clampedPitch = MathHelper.clamp(pitchDelta, -Math.abs(linePitch), Math.abs(linePitch));

        // Классический Funtime lerp: 0.7 — 1.4
        float lerpFactor = 0.7f + random.nextFloat() * 0.7f;

        float newYaw = (float) MathHelper.lerp(lerpFactor, currentAngle.getYaw(), currentAngle.getYaw() + clampedYaw);
        float newPitch = (float) MathHelper.lerp(lerpFactor, currentAngle.getPitch(), currentAngle.getPitch() + clampedPitch);

        return new Angle(newYaw, MathHelper.clamp(newPitch, -90f, 90f));
    }

    @Override
    public Vec3d randomValue() {
        return new Vec3d(
                (random.nextDouble() - 0.5) * 0.12,
                (random.nextDouble() - 0.5) * 0.04,
                (random.nextDouble() - 0.5) * 0.12
        );
    }
}