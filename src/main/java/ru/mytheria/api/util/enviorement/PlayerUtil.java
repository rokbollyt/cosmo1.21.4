package ru.mytheria.api.util.enviorement;

import net.minecraft.entity.Entity;
import net.minecraft.entity.EquipmentSlot;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.projectile.ProjectileUtil;
import net.minecraft.item.Item;
import net.minecraft.item.Items;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.hit.EntityHitResult;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.RaycastContext;
import net.minecraft.world.World;

import java.util.Set;
import java.util.function.Predicate;

import static ru.mytheria.api.clientannotation.QuickImport.mc;

public class PlayerUtil {

    public static Entity getMouseOver(Entity target, float yaw, float pitch, double distance) {
        Entity entity = mc.getCameraEntity();
        if (entity == null || mc.world == null || target == null) {
            return null;
        }

        Box playerBox = entity.getBoundingBox();
        Box targetBox = target.getBoundingBox();
        Vec3d startVec = entity.getEyePos();
        Vec3d directionVec = getVectorForRotation(pitch, yaw);
        Vec3d endVec = startVec.add(
                directionVec.x * distance,
                directionVec.y * distance,
                directionVec.z * distance
        );

        if (playerBox.intersects(targetBox)) {
            EntityHitResult hitResult = raytraceEntity(distance, yaw, pitch, (e) -> e == target && !e.isSpectator() && e.canBeHitByProjectile());
            if (hitResult != null && hitResult.getEntity() == target) {
                return target;
            }
        }

        EntityHitResult entityHitResult = rayTraceEntities(
                entity,
                startVec,
                endVec,
                targetBox,
                (e) -> e == target && !e.isSpectator() && e.canBeHitByProjectile(),
                distance
        );

        if (entityHitResult != null && startVec.distanceTo(entityHitResult.getPos()) <= distance) {
            return entityHitResult.getEntity();
        }

        return null;
    }

    private static Vec3d getVectorForRotation(float pitch, float yaw) {
        float yawRad = (float) Math.toRadians(yaw);
        float pitchRad = (float) Math.toRadians(pitch);
        float cosPitch = (float) Math.cos(-pitchRad);
        return new Vec3d(
                -Math.sin(yawRad) * cosPitch,
                -Math.sin(pitchRad),
                Math.cos(yawRad) * cosPitch
        );
    }

    private static EntityHitResult rayTraceEntities(Entity source, Vec3d start, Vec3d end, Box boundingBox, java.util.function.Predicate<Entity> predicate, double maxDistance) {
        World world = source.getWorld();
        double closestDistance = maxDistance;
        Entity closestEntity = null;
        Vec3d closestHitPos = null;

        for (Entity entity : world.getEntitiesByClass(Entity.class, boundingBox, predicate)) {
            if (entity == source) continue;

            Box entityBox = entity.getBoundingBox();
            var hit = entityBox.raycast(start, end);

            if (hit.isPresent()) {
                Vec3d hitPos = hit.get();
                double distance = start.distanceTo(hitPos);

                if (distance < closestDistance) {
                    closestEntity = entity;
                    closestHitPos = hitPos;
                    closestDistance = distance;
                }
            }
        }

        if (closestEntity != null) {
            return new EntityHitResult(closestEntity, closestHitPos);
        }
        return null;
    }

    public static boolean isFallFlying() {
        return !mc.player.isOnGround()
                && mc.player.getEquippedStack(EquipmentSlot.CHEST).getItem() == Items.ELYTRA
                && mc.player.getVelocity().getY() > -0.5;
    }

    public static boolean isMoving() {
        return mc.player != null && mc.world != null && mc.player.input != null
                && (mc.player.input.movementForward != 0.0 || mc.player.input.movementSideways != 0.0 || mc.player.fallDistance != 0.0);
    }

    public static boolean isMovingForward() {
        return mc.player != null && mc.world != null && mc.player.input != null
                && mc.player.input.movementForward > 0.0;
    }

    public static boolean isMovingSideways() {
        return mc.player != null && mc.world != null && mc.player.input != null
                && mc.player.input.movementSideways != 0.0;
    }

    public static BlockHitResult raycast( double range, float yaw, float pitch, boolean includeFluids) {
        Entity entity = mc.getCameraEntity();
        if (entity == null || mc.world == null) {
            return null;
        }



        Vec3d start = entity.getCameraPosVec(1.0F);
        float pitchRad = pitch * 0.017453292F;
        float yawRad = -yaw * 0.017453292F;
        float cosPitch = (float) Math.cos(pitchRad);
        float sinPitch = (float) Math.sin(pitchRad);
        float cosYaw = (float) Math.cos(yawRad);
        float sinYaw = (float) Math.sin(yawRad);
        Vec3d rotationVec = new Vec3d(sinYaw * cosPitch, -sinPitch, cosYaw * cosPitch);
        Vec3d end = start.add(rotationVec.x * range, rotationVec.y * range, rotationVec.z * range);

        World world = mc.world;
        RaycastContext.FluidHandling fluidHandling = includeFluids ? RaycastContext.FluidHandling.ANY : RaycastContext.FluidHandling.NONE;
        RaycastContext context = new RaycastContext(start, end, RaycastContext.ShapeType.OUTLINE, fluidHandling, entity);

        return world.raycast(context);
    }

    public static EntityHitResult raytraceEntity( double range, float yaw, float pitch, Predicate<Entity> filter) {
        Entity entity = mc.getCameraEntity();
        if (entity == null || mc.world == null) {
            return null;
        }

        Vec3d cameraVec = entity.getCameraPosVec(1.0F);
        float pitchRad = pitch * 0.017453292F;
        float yawRad = -yaw * 0.017453292F;
        float cosPitch = (float) Math.cos(pitchRad);
        float sinPitch = (float) Math.sin(pitchRad);
        float cosYaw = (float) Math.cos(yawRad);
        float sinYaw = (float) Math.sin(yawRad);
        Vec3d rotationVec = new Vec3d(sinYaw * cosPitch, -sinPitch, cosYaw * cosPitch);
        Vec3d end = cameraVec.add(rotationVec.x * range, rotationVec.y * range, rotationVec.z * range);
        Box box = entity.getBoundingBox().stretch(rotationVec.multiply(range)).expand(1.0, 1.0, 1.0);

        return ProjectileUtil.raycast(
                entity,
                cameraVec,
                end,
                box,
                filter,
                range * range
        );
    }


    private static final Set<Item> THROWABLE_ITEMS = Set.of(
            Items.SNOWBALL,
            Items.EGG,
            Items.ENDER_PEARL,
            Items.SPLASH_POTION,
            Items.LINGERING_POTION,
            Items.TRIDENT,
            Items.EXPERIENCE_BOTTLE
    );

    public static boolean isUsingThrowable() {
        if (mc.player == null) return false;
        PlayerEntity player = mc.player;

        if (!player.isUsingItem()) return false;

        Item inHand = player.getActiveItem().getItem();
        return THROWABLE_ITEMS.contains(inHand);
    }

    public static boolean flyingOnElytra() {
        if (mc.player == null) return false;
        return isFallFlying();
    }

}
