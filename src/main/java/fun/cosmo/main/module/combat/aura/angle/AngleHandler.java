package fun.cosmo.main.module.combat.aura.angle;

import net.minecraft.client.MinecraftClient;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec2f;
import net.minecraft.util.math.Vec3d;

import static java.lang.Math.hypot;
import static java.lang.Math.toDegrees;
import static net.minecraft.util.math.MathHelper.wrapDegrees;

public class AngleHandler {

    private static final MinecraftClient mc = MinecraftClient.getInstance();

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
     * Вычисляет Yaw и Pitch, необходимые для прицеливания в точку.
     * Эквивалентно fromVec3d, но использует разницу между позицией игрока и целью.
     * @param target Целевая точка Vec3d.
     * @return Угол (Angle) с рассчитанными Yaw и Pitch.
     */
    public static Angle calculateAngle(Vec3d target) {
        if (mc.player == null) return new Angle(0, 0);

        Vec3d eyes = mc.player.getEyePos();
        Vec3d vector = target.subtract(eyes); // Вектор от игрока до цели

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
    public static float getDelta(float start, float end) {
        // MathHelper.wrapDegrees (или wrapDegrees, который вы импортируете)
        // выполняет эту самую операцию: (angle % 360 + 540) % 360 - 180
        return MathHelper.wrapDegrees(end - start);
    }
}