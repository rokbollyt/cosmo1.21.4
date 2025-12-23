package ru.mytheria.main.module.combat.aura.points;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.experimental.FieldDefaults;
import lombok.experimental.NonFinal;
import net.minecraft.entity.Entity;
import net.minecraft.util.hit.HitResult;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.RaycastContext;
import ru.mytheria.api.clientannotation.QuickImport;
import ru.mytheria.main.module.combat.aura.angle.Angle;
import ru.mytheria.main.module.combat.aura.angle.AngleHandler;


import java.security.SecureRandom;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Random;

@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@Getter
public class SmartPointHandler implements QuickImport {
    private Random random = new SecureRandom();
    @NonFinal
    private Vec3d lastTarget = Vec3d.ZERO;
    private double centerAimChance = 0.15;

    public Vec3d computeVector( Entity entity, float maxDistance, Angle initialAngle, Vec3d velocity) {
        if (entity == null) return Vec3d.ZERO;

        Vec3d eyePos = mc.player.getEyePos();
        Vec3d targetPos;

        if (random.nextDouble() < centerAimChance) {
            targetPos = entity.getBoundingBox().getCenter();
            if (isWithinDistance(eyePos, targetPos, maxDistance) && isPointVisible(eyePos, targetPos)) {
                lastTarget = targetPos;
                return targetPos;
            }
        }

        List<Vec3d> candidatePoints = generateVisiblePoints(entity, maxDistance);
        Vec3d bestVector = findBestVector(candidatePoints, initialAngle);

        targetPos = bestVector != null ? bestVector : entity.getEyePos();
        lastTarget = targetPos;
        return targetPos;
    }

    private List<Vec3d> generateVisiblePoints(Entity entity, float maxDistance) {
        Vec3d eyePos = mc.player.getEyePos();
        Box box = entity.getBoundingBox().expand(-0.1);
        List<Vec3d> visiblePoints = new ArrayList<>();

        double step = 0.15;
        for (double x = box.minX; x <= box.maxX; x += step) {
            for (double y = box.minY; y <= box.maxY; y += step) {
                for (double z = box.minZ; z <= box.maxZ; z += step) {
                    Vec3d point = new Vec3d(x, y, z);
                    if (isWithinDistance(eyePos, point, maxDistance) && isPointVisible(eyePos, point)) {
                        visiblePoints.add(point);
                    }
                }
            }
        }

        return visiblePoints;
    }

    private boolean isWithinDistance(Vec3d startPoint, Vec3d endPoint, float maxDistance) {
        return startPoint.distanceTo(endPoint) < maxDistance;
    }

    private Vec3d findBestVector(List<Vec3d> candidatePoints, Angle initialAngle) {
        Vec3d playerEyePos = mc.player.getEyePos();

        return candidatePoints.stream()
                .min(Comparator.comparing(point -> calculateRotationDifference(playerEyePos, point, initialAngle)))
                .orElse(null);
    }

    private double calculateRotationDifference(Vec3d startPoint, Vec3d endPoint, Angle initialAngle) {
        Angle targetAngle = AngleHandler.fromVec3d(endPoint.subtract(startPoint));
        Angle delta = AngleHandler.calculateDelta(initialAngle, targetAngle);
        return Math.hypot(delta.getYaw(), delta.getPitch());
    }

    private boolean isPointVisible(Vec3d from, Vec3d to) {
        HitResult result = mc.world.raycast(new RaycastContext(
                from, to,
                RaycastContext.ShapeType.OUTLINE,
                RaycastContext.FluidHandling.NONE,
                mc.player
        ));
        return result.getType() == HitResult.Type.MISS;
    }
}