package ru.mytheria.main.module.combat.aura.rotation;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import net.minecraft.entity.Entity;
import net.minecraft.util.math.Vec3d;
import ru.mytheria.api.clientannotation.QuickImport;
import ru.mytheria.main.module.combat.aura.angle.Angle;
import ru.mytheria.main.module.combat.aura.angle.AngleHandler;
import ru.mytheria.main.module.combat.aura.angle.AngleMode;

@Getter
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class Rotation implements QuickImport {
    Angle angle;
    Vec3d vec3d;
    Entity entity;
    AngleMode angleSmooth;
    int ticksUntilReset;
    float resetThreshold;
    boolean changeLook, moveCorrection, freeCorrection;

    public Angle nextRotation(Angle fromAngle, boolean isResetting) {
        if (isResetting) {
            return angleSmooth.limitAngleChange(fromAngle, AngleHandler.fromVec2f(mc.player.getRotationClient()));
        }
        return angleSmooth.limitAngleChange(fromAngle, angle, vec3d, entity);
    }

}