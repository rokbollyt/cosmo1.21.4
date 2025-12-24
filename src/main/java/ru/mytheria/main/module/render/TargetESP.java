package ru.mytheria.main.module.render;

import meteordevelopment.orbit.EventHandler;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.render.Camera;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.entity.LivingEntity;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.RotationAxis;
import net.minecraft.util.math.Vec3d;
import org.joml.Vector4i;
import ru.mytheria.api.events.impl.EventRender3D;
import ru.mytheria.api.module.Category;
import ru.mytheria.api.module.Module;
import ru.mytheria.api.util.render.Render3DUtil;

public class TargetESP extends Module {

    private static final MinecraftClient mc = MinecraftClient.getInstance();

    private static final Identifier BLOOM =
            Identifier.of("mytheria", "textures/bloom.png");

    private float anim;

    public TargetESP() {
        super(Text.of("TargetESP"), Category.RENDER);
    }

    @EventHandler
    public void onRender3D(EventRender3D e) {
        if (mc.player == null || mc.world == null) return;

        LivingEntity target = getTarget();
        if (target == null) return;

        anim += e.getPartialTicks() * 0.08f; // бесконечная анимация

        drawGhosts(e, target);
    }

    private void drawGhosts(EventRender3D e, LivingEntity target) {
        Camera cam = e.getCamera();
        MatrixStack matrices = e.getMatrixStack();

        // ✅ ПРАВИЛЬНАЯ ИНТЕРПОЛЯЦИЯ
        Vec3d targetPos = new Vec3d(
                target.prevX + (target.getX() - target.prevX) * e.getPartialTicks(),
                target.prevY + (target.getY() - target.prevY) * e.getPartialTicks(),
                target.prevZ + (target.getZ() - target.prevZ) * e.getPartialTicks()
        );

        // 🎯 уровень груди (как в Nexis)
        targetPos = targetPos.add(0, target.getHeight() * 0.55, 0);

        Vec3d camPos = cam.getPos();
        Vec3d base = targetPos.subtract(camPos);

        float radius = target.getWidth() * 1.8f;
        int count = 12;

        for (int i = 0; i < count; i++) {

            double angle = anim + (Math.PI * 2 / count) * i;

            double x = Math.cos(angle) * radius;
            double z = Math.sin(angle) * radius;

            // 🌊 вертикальная волна (Nexis)
            double y = Math.sin(anim * 1.6 + i) * 0.25;

            float size = 0.35f + 0.12f * (float) Math.sin(anim + i);

            matrices.push();
            matrices.translate(
                    base.x + x,
                    base.y + y,
                    base.z + z
            );

            // billboard к камере
            matrices.multiply(RotationAxis.POSITIVE_Y.rotationDegrees(-cam.getYaw()));
            matrices.multiply(RotationAxis.POSITIVE_X.rotationDegrees(cam.getPitch()));

            Render3DUtil.drawTexture(
                    matrices.peek(),
                    BLOOM,
                    -size / 2,
                    -size / 2,
                    size,
                    size,
                    new Vector4i(0xFFFFFFFF),
                    false
            );

            matrices.pop();
        }
    }

    private LivingEntity getTarget() {
        if (mc.targetedEntity instanceof LivingEntity le && le != mc.player) {
            return le;
        }
        return null;
    }
}
