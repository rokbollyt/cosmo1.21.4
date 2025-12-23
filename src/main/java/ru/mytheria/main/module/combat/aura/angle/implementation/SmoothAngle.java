package ru.mytheria.main.module.combat.aura.angle.implementation;

import net.minecraft.entity.Entity;
import net.minecraft.util.math.Vec3d;
import ru.mytheria.main.module.combat.aura.angle.Angle;
import ru.mytheria.main.module.combat.aura.angle.AngleDeclaration;
import ru.mytheria.main.module.combat.aura.angle.AngleHandler;

public class SmoothAngle extends AngleDeclaration {

    public SmoothAngle() {
        super("Smooth");
    }

    @Override
    public Angle limitAngleChange( Angle currentAngle, Angle targetAngle, Vec3d vec3d, Entity entity ) {
        return null;
    }

    @Override
    public Vec3d randomValue() {
        return null;
    }
}
