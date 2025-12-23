package ru.mytheria.api.util.math;

import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;

import java.util.concurrent.ThreadLocalRandom;

import static ru.mytheria.api.clientannotation.QuickImport.mc;

public class Math extends org.joml.Math {

    public static float stick(float value, float nearest, float threshold) {
        return Math.abs(value - nearest) <= threshold ? nearest : value;
    }

    public static boolean isHover(double mouseX, double mouseY, float x, float y, float width, float height) {
        return mouseX >= x && mouseX <= x + width &&
                mouseY >= y && mouseY <= y + height;
    }

    public static void scale(MatrixStack stack, float x, float y, float scale, Runnable data) {
        stack.push();
        stack.translate(x, y, 0);
        stack.scale(scale, scale, 1);
        stack.translate(-x, -y, 0);
        data.run();
        stack.pop();
    }

    public static String lerp(float delta, String from, String to) {
        int step = (int) floor(delta * (from.length() + to.length()));

        return step < from.length()
                ? from.substring(0, max(0, from.length() - step))
                : to.substring(0, min(step - from.length(), to.length()));
    }

    public static Integer random(Integer from, Integer to) {
        return ThreadLocalRandom.current().nextInt(from, to);
    }

    public static Float random(Float from, Float to) {
        return ThreadLocalRandom.current().nextFloat(from, to);
    }

    public static float interpolateRotation(float prev, float now, float partialTicks) {
        float delta = wrapDegrees(now - prev);
        return prev + partialTicks * delta;
    }

    public static float wrapDegrees(float value) {
        value = value % 360.0F;
        if (value >= 180.0F) {
            value -= 360.0F;
        }
        if (value < -180.0F) {
            value += 360.0F;
        }
        return value;
    }

    public static int randomInt( int min, int max ) {
        return (int) (java.lang.Math.random() * (max - min) + min);
    }

    public static float randomFloat( float min, float max ) {
        return (float) (java.lang.Math.random() * (max - min) + min);
    }


    public static boolean inFov( Vec3d pos, int fov, float yaw ) {
        double deltaX = pos.getX() - mc.player.getX();
        double deltaZ = pos.getZ() - mc.player.getZ();
        float angle = (float) java.lang.Math.toDegrees(java.lang.Math.atan2(deltaZ, deltaX)) - 90;
        float yawDelta = MathHelper.wrapDegrees(angle - yaw);

        return java.lang.Math.abs(yawDelta) <= fov;
    }

    public static float getStep( float current, float target, float step ) {
        if (java.lang.Math.abs(target - current) <= step) return target;

        return current + java.lang.Math.signum(target - current) * step;
    }

    public static double computeGcd() {
        double f = mc.options.getMouseSensitivity().getValue() * 0.6 + 0.2;
        return f * f * f * 8.0 * 0.15;
    }

    public static double getRandom(double min, double max) {
        if (min == max) {
            return min;
        } else {
            if (min > max) {
                double d = min;
                min = max;
                max = d;
            }

            return ThreadLocalRandom.current().nextDouble(min, max);
        }
    }
}

