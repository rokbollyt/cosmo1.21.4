package ru.mytheria.main.module.combat.aura.angle;

import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec2f;
import net.minecraft.util.math.Vec3d;

import static java.lang.Math.hypot;
import static java.lang.Math.toDegrees;
import static net.minecraft.util.math.MathHelper.wrapDegrees;

public class AngleHandler {

    /**
     * @param vector2f - 2D вектор для создания угла
     * @return Angle - угол из 2D вектора
     * @method fromVec2f
     */
    public static Angle fromVec2f( Vec2f vector2f ) {
        return new Angle(vector2f.y, vector2f.x);
    }

    /**
     * @param vector - 3D вектор для создания угла
     * @return Angle - угол из 3D вектора
     * @method fromVec3d
     */
    public static Angle fromVec3d( Vec3d vector ) {
        return new Angle(
                (float) wrapDegrees(toDegrees(Math.atan2(vector.z, vector.x)) - 90),
                (float) wrapDegrees(toDegrees(-Math.atan2(vector.y, hypot(vector.x, vector.z))))
        );
    }

    /**
     * @param start - начальный угол
     * @param end   - конечный угол
     * @return Angle - разница между углами
     * @method calculateDelta
     **/
    public static Angle calculateDelta( Angle start, Angle end ) {
        float deltaYaw = MathHelper.wrapDegrees(end.getYaw() - start.getYaw());
        float deltaPitch = MathHelper.wrapDegrees(end.getPitch() - start.getPitch());
        return new Angle(deltaYaw, deltaPitch);
    }
}
