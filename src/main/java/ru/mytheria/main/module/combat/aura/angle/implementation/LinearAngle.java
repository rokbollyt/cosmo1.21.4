package ru.mytheria.main.module.combat.aura.angle.implementation;

import net.minecraft.entity.Entity;
import net.minecraft.util.math.Vec3d;
import ru.mytheria.main.module.combat.aura.angle.Angle;
import ru.mytheria.main.module.combat.aura.angle.AngleDeclaration;
import ru.mytheria.main.module.combat.aura.angle.AngleHandler;

public class LinearAngle extends AngleDeclaration {

    public LinearAngle() {
        super("Linear");
    }

    @Override
    public Angle limitAngleChange(Angle currentAngle, Angle targetAngle, Vec3d vec3d, Entity entity) {
        Angle angleDelta = AngleHandler.calculateDelta(currentAngle, targetAngle);
        float yawDelta = angleDelta.getYaw();
        float pitchDelta = angleDelta.getPitch();

        float rotationDifference = (float) Math.hypot(Math.abs(yawDelta), Math.abs(pitchDelta));

        float straightLineYaw = Math.abs(yawDelta / rotationDifference) * 180.0F;
        float straightLinePitch = Math.abs(pitchDelta / rotationDifference) * 180.0F;

        return new Angle(
                currentAngle.getYaw() + Math.min(Math.max(yawDelta, -straightLineYaw), straightLineYaw),
                currentAngle.getPitch() + Math.min(Math.max(pitchDelta, -straightLinePitch), straightLinePitch)
        );
    }

    @Override
    public Vec3d randomValue() {
        return new Vec3d(0, 0, 0);
    }
}
