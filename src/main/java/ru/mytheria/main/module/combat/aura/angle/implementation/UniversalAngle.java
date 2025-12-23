package ru.mytheria.main.module.combat.aura.angle.implementation;

import net.minecraft.entity.Entity;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;
import ru.mytheria.main.module.combat.aura.angle.Angle;
import ru.mytheria.main.module.combat.aura.angle.AngleDeclaration;
import ru.mytheria.main.module.combat.aura.angle.AngleHandler;

import java.util.Objects;

public class UniversalAngle extends AngleDeclaration {

    public UniversalAngle() {
        super("Universal");
    }

    @Override
    public Angle limitAngleChange( Angle currentAngle, Angle targetAngle, Vec3d vec3d, Entity entity ) {
        return null;
    }

    @Override
    public Vec3d randomValue() {
        return null;
    }

    public static class FuntimeAngle extends AngleDeclaration {

        public FuntimeAngle() {
            super("Funtime");
        }

        @Override
        public Angle limitAngleChange(Angle currentAngle, Angle targetAngle, Vec3d vec3d, Entity entity) {
            Angle angleDelta = AngleHandler.calculateDelta(currentAngle, targetAngle);

            float yawDelta = angleDelta.getYaw();
            float pitchDelta = angleDelta.getPitch();

            float rotationDifference = (float) Math.hypot(Math.abs(yawDelta), Math.abs(pitchDelta));

            float distanceFactor = Math.max(Math.min(Objects.requireNonNull(mc.player).distanceTo(entity) / 2, 1.0F), 0.1F);

            float lineYaw = (Math.abs(yawDelta / rotationDifference) * 90) * distanceFactor;
            float linePitch = (Math.abs(pitchDelta / rotationDifference) * 90) * distanceFactor;

            float moveYaw = MathHelper.clamp(yawDelta, -lineYaw, lineYaw);
            float movePitch = MathHelper.clamp(pitchDelta, -linePitch, linePitch);

            Angle moveAngle = new Angle(currentAngle.getYaw(), currentAngle.getPitch());
            moveAngle.setYaw((float) MathHelper.lerp(0.7F + ru.mytheria.api.util.math.Math.getRandom(0, 1) * 0.7F, currentAngle.getYaw(),
                    currentAngle.getYaw() + moveYaw));
            moveAngle.setPitch((float) MathHelper.lerp(0.7F + ru.mytheria.api.util.math.Math.getRandom(0, 1) * 0.7F, currentAngle.getPitch(),
                    currentAngle.getPitch() + movePitch));

            return new Angle(moveAngle.getYaw(), moveAngle.getPitch());
        }

        @Override
        public Vec3d randomValue() {
            return new Vec3d(0.06F, 0.02F, 0.06F);
        }
    }
}
