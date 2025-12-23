package ru.mytheria.api.util.player;

import net.minecraft.entity.Entity;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;

import java.util.regex.Pattern;

public class PlayerUtils {
    private static final Pattern NAME_REGEX = Pattern.compile("^[A-zА-я0-9_]{3,16}$");

    public static boolean isNameValid(String name) {
        return NAME_REGEX.matcher(name).matches();
    }

    public static Vec3d getLerpedPos(Entity e, float tickDelta) {
        if (e.isRemoved()) return e.getLerpedPos(tickDelta);

        double x = MathHelper.lerp(tickDelta, e.prevX, e.getX());
        double y = MathHelper.lerp(tickDelta, e.prevY, e.getY());
        double z = MathHelper.lerp(tickDelta, e.prevZ, e.getZ());
        return new Vec3d(x, y, z);
    }
}
