package ru.mytheria.api.util.math;



import net.minecraft.client.util.math.Vector2f;
import net.minecraft.entity.Entity;
import net.minecraft.entity.projectile.ProjectileUtil;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.hit.EntityHitResult;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.RaycastContext;
import net.minecraft.world.World;
import ru.mytheria.api.clientannotation.QuickImport;

import java.lang.Math;
import java.util.function.Predicate;

public class MathVector implements QuickImport {

    public static float rotationDifference(Entity entity) {
        if (mc.player == null || entity == null) return 0;

        double x = interpolate(entity.prevX, entity.getPos().x) - interpolate(mc.player.prevX, mc.player.getPos().x);
        double z = interpolate(entity.prevZ, entity.getPos().z) - interpolate(mc.player.prevZ, mc.player.getPos().z);
        return (float) -(Math.atan2(x, z) * (180 / Math.PI));
    }

    public static Vec3d lerpPosition(Entity entity) {
        float tickDelta = mc.getRenderTickCounter().getTickDelta(true);
        return new Vec3d(
            entity.prevX + (entity.getX() - entity.prevX) * tickDelta,
            entity.prevY + (entity.getY() - entity.prevY) * tickDelta,
            entity.prevZ + (entity.getZ() - entity.prevZ) * tickDelta
        );
    }

    public static double interpolate(double d, double d2) {
        return d + (d2 - d) * (double) mc.getRenderTickCounter().getTickDelta(true);
    }

    public static float distanceTo(Vec3d from, Vec3d to) {
        float f = (float) (from.getX() - to.getX());
        float g = (float) (from.getY() - to.getY());
        float h = (float) (from.getZ() - to.getZ());
        return MathHelper.sqrt(f * f + g * g + h * h);
    }

  /*  public static Vector calculateRotationDelta(Vector from, Vector to) {
        return to.subtract(from).wrapDegrees();
    }

    public static Vector2f calculateRotationDelta(Vector2f from, Vector2f to) {
        return new Vector2f(MathHelper.wrapDegrees(to.getX() - from.getX()), MathHelper.wrapDegrees(to.getY() - from.getY()));
    }

    public static Vector calculateRotation(Vec3d vec3d) {
        return new Vector(
                (float) Math.toDegrees(Math.atan2(vec3d.z, vec3d.x)) - 90,
                (float) Math.toDegrees(-Math.atan2(vec3d.y, Math.hypot(vec3d.x, vec3d.z)))
        ).wrapDegrees();
    }*/

    public static Vec3d calculatePositionDelta(Vec3d from, Vec3d to) {
        return to.subtract(from);
    }

   /* public static BlockHitResult raycast(double range, Vector vector, boolean includeFluids) {
        Entity entity = mc.cameraEntity;

        if (entity == null) {
            return null;
        }

        Vec3d start = entity.getCameraPosVec(1.0F);
        Vec3d rotationVec = vector.toVector();
        Vec3d end = start.add(rotationVec.x * range, rotationVec.y * range, rotationVec.z * range);

        World world = mc.world;
        if (world == null) {
            return null;
        }

        RaycastContext.FluidHandling fluidHandling = includeFluids ? RaycastContext.FluidHandling.ANY : RaycastContext.FluidHandling.NONE;
        RaycastContext context = new RaycastContext(start, end, RaycastContext.ShapeType.OUTLINE, fluidHandling, entity);

        return world.raycast(context);
    }


    public static EntityHitResult raycastEntity(double range, Vector vector, Predicate<Entity> filter) {
        Entity entity = mc.cameraEntity;
        if (entity == null) return null;

        Vec3d cameraVec = entity.getCameraPosVec(1.0F);
        Vec3d rotationVec = vector.toVector();

        Vec3d vec3d3 = cameraVec.add(rotationVec.x * range, rotationVec.y * range, rotationVec.z * range);
        Box box = entity.getBoundingBox().stretch(rotationVec.multiply(range)).expand(1.0, 1.0, 1.0);

        return ProjectileUtil.raycast(
                entity,
                cameraVec,
                vec3d3,
                box,
                (e) -> !e.isSpectator() && filter.test(e),
                range * range
        );
    }*/

}
