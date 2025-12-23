package ru.mytheria.main.module.combat.aura.points;


import net.minecraft.entity.Entity;
import net.minecraft.util.hit.HitResult;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.RaycastContext;

import lombok.AccessLevel;
import lombok.experimental.FieldDefaults;
import ru.mytheria.api.clientannotation.QuickImport;
import ru.mytheria.main.module.combat.aura.angle.Angle;

import java.util.ArrayList;
import java.util.List;

@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class MultiPointHandler implements QuickImport {

    public Vec3d computeVector( Entity entity, float maxDistance, Angle initialAngle, Vec3d velocity) {
        if (entity == null) return Vec3d.ZERO;

        Vec3d eyePos = mc.player.getEyePos();
        Box box = entity.getBoundingBox();

        List<Vec3d> points = generateDensePoints(box, 0.2);

        return points.stream()
                .filter(point -> isPointVisible(eyePos, point))
                .min((p1, p2) -> Double.compare(
                        eyePos.squaredDistanceTo(p1),
                        eyePos.squaredDistanceTo(p2)
                ))
                .orElse(box.getCenter());
    }

    private List<Vec3d> generateDensePoints(Box box, double step) {
        List<Vec3d> points = new ArrayList<>();

        for (double t = 0; t <= 1; t += step) {
            double x = lerp(box.minX, box.maxX, t);
            double y = lerp(box.minY, box.maxY, t);
            double z = lerp(box.minZ, box.maxZ, t);

            points.add(new Vec3d(x, box.minY, box.minZ));
            points.add(new Vec3d(x, box.maxY, box.maxZ));
            points.add(new Vec3d(box.minX, y, box.minZ));
            points.add(new Vec3d(box.maxX, y, box.maxZ));
            points.add(new Vec3d(box.minX, box.minY, z));
            points.add(new Vec3d(box.maxX, box.maxY, z));

            points.add(new Vec3d(x, y, box.minZ));
            points.add(new Vec3d(x, y, box.maxZ));
        }

        return points;
    }

    private double lerp(double start, double end, double t) {
        return start + (end - start) * t;
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