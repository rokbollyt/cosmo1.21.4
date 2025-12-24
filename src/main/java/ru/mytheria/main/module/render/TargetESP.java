package ru.mytheria.main.module.render;


import meteordevelopment.orbit.EventHandler;
import net.minecraft.block.Block;
import net.minecraft.client.render.OverlayTexture;
import net.minecraft.client.render.RenderLayer;
import net.minecraft.client.render.VertexConsumer;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.entity.Entity;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.MathHelper;
import org.joml.Matrix3f;
import org.joml.Matrix4f;
import ru.mytheria.api.events.impl.EventRender3D;
import ru.mytheria.api.module.Category;
import ru.mytheria.api.module.Module;

public class TargetESP extends Module {
    private static final int CRYSTALS = 6;
    private static final float RADIUS = 1.0f;

    public TargetESP() {
        super(Text.of("TargetESP"), Category.RENDER);
    }

    @EventHandler
    private void onRender3D(EventRender3D event) {
        Entity target = mc.targetedEntity;
        if (target == null) return;

        MatrixStack matrices = event.getMatrixStack();
        VertexConsumerProvider.Immediate consumers = event.getVertexConsumers();
        float tickDelta = event.getPartialTicks();

        double x = MathHelper.lerp(tickDelta, target.prevX, target.getX()) - event.getCamera().getPos().x;
        double y = MathHelper.lerp(tickDelta, target.prevY, target.getY()) - event.getCamera().getPos().y + target.getHeight() * 0.5;
        double z = MathHelper.lerp(tickDelta, target.prevZ, target.getZ()) - event.getCamera().getPos().z;

        matrices.push();
        matrices.translate(x, y, z);

        VertexConsumer buffer = consumers.getBuffer(
                RenderLayer.getEntityTranslucent(
                        Identifier.of("minecraft", "textures/entity/end_crystal/end_crystal.png")
                )
        );

        for (int i = 0; i < CRYSTALS; i++) {
            float angle = (float) (2 * Math.PI * i / CRYSTALS);
            float ox = MathHelper.cos(angle) * RADIUS;
            float oz = MathHelper.sin(angle) * RADIUS;
            float oy = MathHelper.sin((System.currentTimeMillis() % 2000L) / 2000f * MathHelper.TAU + i) * 0.2f;

            matrices.push();
            matrices.translate(ox, oy, oz);
            renderCrystal(matrices, buffer, 0xFF00FF);
            matrices.pop();
        }

        matrices.pop();
        consumers.draw();
    }


    private void renderCrystal(MatrixStack matrices, VertexConsumer buffer, int color) {
        Matrix4f mat = matrices.peek().getPositionMatrix();

        float r = ((color >> 16) & 0xFF) / 255f;
        float g = ((color >> 8) & 0xFF) / 255f;
        float b = (color & 0xFF) / 255f;

        float s = 0.4f;

        float[][] v = {
                {0, s, 0},
                {-s, 0, -s},
                { s, 0, -s},
                { s, 0,  s},
                {-s, 0,  s}
        };

        int[][] f = {
                {0,1,2},
                {0,2,3},
                {0,3,4},
                {0,4,1}
        };

        for (int[] face : f) {
            for (int i : face) {
                buffer.vertex(mat, v[i][0], v[i][1], v[i][2])
                        .color(r, g, b, 1f)
                        .overlay(OverlayTexture.DEFAULT_UV)
                        .light(0xF000F0)
                        .normal(0, 1, 0);
            }
        }
    }


    private Entity getTarget() {
        return mc.targetedEntity;
    }

}
