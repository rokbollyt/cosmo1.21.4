package ru.mytheria.main.module.render;

import meteordevelopment.orbit.EventHandler;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.render.Camera;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.entity.LivingEntity;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.Vec3d;
import net.minecraft.util.math.RotationAxis;
import org.joml.Vector4i;
import ru.mytheria.api.events.impl.EventRender3D;
import ru.mytheria.api.module.Category;
import ru.mytheria.api.module.Module;
import ru.mytheria.api.util.render.Render3DUtil;

public class TargetESP extends Module {

    private static final MinecraftClient mc = MinecraftClient.getInstance();

    // путь: resources/assets/mytheria/textures/bloom.png
    private static final Identifier BLOOM =
            Identifier.of("mytheria", "textures/bloom.png");

    private float anim = 0f;

    public TargetESP() {
        super(Text.of("TargetESP"), Category.RENDER);
    }

    @EventHandler
    public void onRender3D(EventRender3D e) {

        // 🔥 правильная проверка под твою базу
        if (!getEnabled()) return;

        if (mc.player == null || mc.world == null) return;

        LivingEntity target = getTarget();
        if (target == null) return;

        anim += e.getPartialTicks() * 0.05f;

        drawGhostTrail(e, target);
    }


    private void drawGhostTrail(EventRender3D e, LivingEntity target) {
        Camera cam = e.getCamera();
        MatrixStack matrices = e.getMatrixStack();

        // интерполяция позиции
        Vec3d targetPos = new Vec3d(
                target.prevX + (target.getX() - target.prevX) * e.getPartialTicks(),
                target.prevY + (target.getY() - target.prevY) * e.getPartialTicks(),
                target.prevZ + (target.getZ() - target.prevZ) * e.getPartialTicks()
        );

        // уровень груди
        targetPos = targetPos.add(0, target.getHeight() * 0.55, 0);

        Vec3d camPos = cam.getPos();
        Vec3d base = targetPos.subtract(camPos);

        int ghostsPerRing = 12;
        int rings = 3;

        float radius = target.getWidth() * 1.1f;
        float delay = 0.22f;

        for (int ring = 0; ring < rings; ring++) {

            // фазовый сдвиг между кольцами
            float ringPhase = (float) (Math.PI * 2 / rings) * ring;
            float ringYOffset = (ring - 1) * 0.25f; // вверх / центр / вниз

            for (int i = 0; i < ghostsPerRing; i++) {

                float time = anim - i * delay + ringPhase;

                double x = Math.cos(time) * radius;
                double z = Math.sin(time) * radius;

                // вертикальная волна
                double y = Math.sin(time * 1.4f) * 0.35 + ringYOffset;

                float alpha = 1f - (i / (float) ghostsPerRing);
                float size = 0.38f * alpha;

                matrices.push();
                matrices.translate(
                        base.x + x,
                        base.y + y,
                        base.z + z
                );

                // billboard
                matrices.multiply(
                        net.minecraft.util.math.RotationAxis.POSITIVE_Y.rotationDegrees(-cam.getYaw()));
                matrices.multiply(
                        net.minecraft.util.math.RotationAxis.POSITIVE_X.rotationDegrees(cam.getPitch()));

                int a = (int) (255 * alpha);
                int color = (a << 24) | 0xFFFFFF;

                Render3DUtil.drawTexture(
                        matrices.peek(),
                        BLOOM,
                        -size / 2f,
                        -size / 2f,
                        size,
                        size,
                        new org.joml.Vector4i(color),
                        false
                );

                matrices.pop();
            }
        }
    }


    private LivingEntity getTarget() {
        if (!(mc.targetedEntity instanceof LivingEntity le)) return null;
        if (le == mc.player) return null;
        return le;
    }
}
