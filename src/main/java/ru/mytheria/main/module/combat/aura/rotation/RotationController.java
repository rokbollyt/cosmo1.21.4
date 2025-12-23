package ru.mytheria.main.module.combat.aura.rotation;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.experimental.FieldDefaults;
import net.minecraft.entity.Entity;
import net.minecraft.util.math.Vec3d;
import ru.mytheria.Mytheria;
import ru.mytheria.api.clientannotation.QuickImport;
import ru.mytheria.main.module.combat.aura.angle.Angle;
import ru.mytheria.main.module.combat.aura.angle.AngleMode;
import ru.mytheria.main.module.combat.aura.angle.implementation.LinearAngle;
import ru.mytheria.main.module.combat.aura.angle.implementation.LinearSmoothAngle;

@Getter
@FieldDefaults(level = AccessLevel.PRIVATE)
public class RotationController implements QuickImport {

    public static RotationController INSTANCE = new RotationController();

    public RotationController() {
        Mytheria.getInstance().getEventProvider().subscribe(this);
    }

    Angle currentAngle;

    Angle previousAngle;

    Angle serverAngle = Angle.DEFAULT;

    public static RotationController DEFAULT = new RotationController(new LinearSmoothAngle(),
            false, true, true);
    AngleMode angleSmooth;
    float resetThreshold = 2f;
    int ticksUntilReset = 5;
    boolean changeView, moveCorrection, freeCorrection;

    public RotationController(boolean changeView, boolean moveCorrection, boolean freeCorrection) {
        this(new LinearSmoothAngle(), changeView, moveCorrection, freeCorrection);
    }

    public RotationController(boolean changeView, boolean moveCorrection) {
        this(new LinearSmoothAngle(), changeView, moveCorrection, true);
    }

    public RotationController(boolean changeView) {
        this(new LinearSmoothAngle(), changeView, true, true);
    }

    public RotationController(AngleMode angleSmooth, boolean changeView, boolean moveCorrection, boolean freeCorrection) {
        this.angleSmooth = angleSmooth;
        this.changeView = changeView;
        this.moveCorrection = moveCorrection;
        this.freeCorrection = freeCorrection;
    }

    public Rotation createRotationPlan( Angle angle, Vec3d vec, Entity entity) {
        return new Rotation(angle, vec, entity, angleSmooth, ticksUntilReset, resetThreshold, changeView, moveCorrection, freeCorrection);
    }

    public Rotation createRotationPlan(Angle angle) {
        return new Rotation(angle, null, null, angleSmooth, ticksUntilReset, resetThreshold, changeView, moveCorrection, freeCorrection);
    }

    public Rotation createRotationPlan(Angle angle, Vec3d vec, Entity entity, boolean changeLook, boolean moveCorrection, boolean freeCorrection) {
        return new Rotation(angle, vec, entity, angleSmooth, ticksUntilReset, resetThreshold, changeLook, moveCorrection, freeCorrection);
    }

}
