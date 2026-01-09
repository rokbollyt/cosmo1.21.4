package fun.cosmo.main.module.combat.aura.points;

import fun.cosmo.main.module.combat.aura.angle.Angle;
import fun.cosmo.main.module.combat.aura.angle.AngleHandler;
import net.minecraft.entity.Entity;
import net.minecraft.util.hit.HitResult;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.RaycastContext;
import fun.cosmo.api.clientannotation.QuickImport;

import java.security.SecureRandom;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Random;

public class SmartPointHandler implements QuickImport {

    private final Random random = new SecureRandom();

    public Vec3d getBestPoint(Entity entity, float partialTicks) {
        if (entity == null) return Vec3d.ZERO;

        Vec3d eyePos = mc.player.getEyePos();
        Box box = entity.getBoundingBox().expand(0.1);

        List<Vec3d> candidatePoints = new ArrayList<>();

        // Основные точки: голова, тело, ноги, углы бокса
        double headY = box.maxY - 0.1;
        double bodyY = (box.maxY + box.minY) / 2.0;
        double feetY = box.minY + 0.1;

        candidatePoints.add(new Vec3d(entity.getX(), headY, entity.getZ()));
        candidatePoints.add(new Vec3d(entity.getX(), bodyY, entity.getZ()));
        candidatePoints.add(new Vec3d(entity.getX(), feetY, entity.getZ()));

        // Углы бокса на разных высотах
        for (double y : new double[]{headY, bodyY, feetY}) {
            candidatePoints.add(new Vec3d(box.minX, y, box.minZ));
            candidatePoints.add(new Vec3d(box.maxX, y, box.minZ));
            candidatePoints.add(new Vec3d(box.minX, y, box.maxZ));
            candidatePoints.add(new Vec3d(box.maxX, y, box.maxZ));
        }

        // Фильтруем только видимые точки (raytrace)
        List<Vec3d> visiblePoints = new ArrayList<>();
        for (Vec3d point : candidatePoints) {
            if (isPointVisible(eyePos, point)) {
                visiblePoints.add(point);
            }
        }

        if (visiblePoints.isEmpty()) {
            // Если ничего не видно — берём центр головы
            return new Vec3d(entity.getX(), box.maxY - 0.15, entity.getZ());
        }

        // Выбираем точку с минимальным угловым отклонением от текущего взгляда
        Angle currentAngle = AngleHandler.fromVec3d(mc.player.getRotationVec(1.0f));

        return visiblePoints.stream()
                .min(Comparator.comparingDouble(point ->
                        calculateAngleDifference(eyePos, point, currentAngle)))
                .orElse(visiblePoints.get(0));
    }

    private double calculateAngleDifference(Vec3d from, Vec3d to, Angle current) {
        Vec3d direction = to.subtract(from).normalize();
        Angle target = AngleHandler.fromVec3d(direction);
        Angle delta = AngleHandler.calculateDelta(current, target);
        return Math.sqrt(delta.getYaw() * delta.getYaw() + delta.getPitch() * delta.getPitch());
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