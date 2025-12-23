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

    //TODO: эх шарик, я как и ты был на крашах, короче фиксануть надо, чет с вершинами не так
    @EventHandler
    private void onRender3D( EventRender3D event) {
        Entity target = getTarget();
        if (target == null) return;
        System.out.println("Render3D tick: " + System.currentTimeMillis());

        MatrixStack matrices = event.getMatrixStack();
        VertexConsumerProvider.Immediate vertexConsumers = event.getVertexConsumers();
        float tickDelta = event.getPartialTicks();

        double x = MathHelper.lerp(tickDelta, target.prevX, target.getX()) - event.getCamera().getPos().x;
        double y = MathHelper.lerp(tickDelta, target.prevY, target.getY()) - event.getCamera().getPos().y + target.getHeight() * 0.5;
        double z = MathHelper.lerp(tickDelta, target.prevZ, target.getZ()) - event.getCamera().getPos().z;

        matrices.push();
        matrices.translate(x, y, z);

        for (int i = 0; i < CRYSTALS; i++) {
            float angle = (float) (2 * Math.PI * i / CRYSTALS);
            float offsetX = (float) Math.cos(angle) * RADIUS;
            float offsetZ = (float) Math.sin(angle) * RADIUS;

            float floatY = (float) Math.sin((System.currentTimeMillis() % 2000L) / 2000.0 * 2 * Math.PI + i) * 0.2f;

            matrices.push();
            matrices.translate(offsetX, floatY, offsetZ);
            VertexConsumer buffer = vertexConsumers.getBuffer(RenderLayer.getEntitySolid(Identifier.of("minecraft", "textures/entity/end_crystal/end_crystal.png")));
            renderCrystal(matrices, buffer, x, y, z, 0xFF00FF);

            matrices.pop();
        }

        matrices.pop();
        vertexConsumers.draw();
    }

    private void renderCrystal(MatrixStack matrices, VertexConsumer buffer, double x, double y, double z, int color) {
        Matrix4f matrix = matrices.peek().getPositionMatrix();

        float r = ((color >> 16) & 0xFF) / 255f;
        float g = ((color >> 8) & 0xFF) / 255f;
        float b = (color & 0xFF) / 255f;
        float a = 1f;

        float size = 0.5f;

        float[][] vertices = {
                {0, size, 0},
                {-size, 0, -size},
                {size, 0, -size},
                {size, 0, size},
                {-size, 0, size}
        };

        int[][] faces = {
                {0,1,2},
                {0,2,3},
                {0,3,4},
                {0,4,1}
        };

        for (int i = 0; i < faces.length; i++) {
            float[] v0 = vertices[faces[i][0]];
            float[] v1 = vertices[faces[i][1]];
            float[] v2 = vertices[faces[i][2]];

            float nx = 0, ny = 1, nz = 0;

            buffer.vertex(matrix, (float)(x+v0[0]), (float)(y+v0[1]), (float)(z+v0[2]))
                    .color(r,g,b,a)
                    .texture(0f,0f)
                    .overlay(OverlayTexture.DEFAULT_UV)
                    .normal(nx, ny, nz);

            buffer.vertex(matrix, (float)(x+v1[0]), (float)(y+v1[1]), (float)(z+v1[2]))
                    .color(r,g,b,a)
                    .texture(1f,0f)
                    .overlay(OverlayTexture.DEFAULT_UV)
                    .normal(nx, ny, nz);

            buffer.vertex(matrix, (float)(x+v2[0]), (float)(y+v2[1]), (float)(z+v2[2]))
                    .color(r,g,b,a)
                    .texture(0.5f,1f)
                    .overlay(OverlayTexture.DEFAULT_UV)
                    .normal(nx, ny, nz);
        }

        float[] b0 = vertices[1];
        float[] b1 = vertices[2];
        float[] b2 = vertices[3];
        float[] b3 = vertices[4];

        float nx = 0, ny = -1, nz = 0;

        buffer.vertex(matrix, (float)(x+b0[0]), (float)(y+b0[1]), (float)(z+b0[2]))
                .color(r,g,b,a)
                .texture(0f,0f)
                .overlay(OverlayTexture.DEFAULT_UV)
                .normal(nx,ny,nz);

        buffer.vertex(matrix, (float)(x+b1[0]), (float)(y+b1[1]), (float)(z+b1[2]))
                .color(r,g,b,a)
                .texture(1f,0f)
                .overlay(OverlayTexture.DEFAULT_UV)
                .normal(nx,ny,nz);

        buffer.vertex(matrix, (float)(x+b2[0]), (float)(y+b2[1]), (float)(z+b2[2]))
                .color(r,g,b,a)
                .texture(1f,1f)
                .overlay(OverlayTexture.DEFAULT_UV)
                .normal(nx,ny,nz);

        buffer.vertex(matrix, (float)(x+b2[0]), (float)(y+b2[1]), (float)(z+b2[2]))
                .color(r,g,b,a)
                .texture(1f,1f)
                .overlay(OverlayTexture.DEFAULT_UV)
                .normal(nx,ny,nz);

        buffer.vertex(matrix, (float)(x+b3[0]), (float)(y+b3[1]), (float)(z+b3[2]))
                .color(r,g,b,a)
                .texture(0f,1f)
                .overlay(OverlayTexture.DEFAULT_UV)
                .normal(nx,ny,nz);

        buffer.vertex(matrix, (float)(x+b0[0]), (float)(y+b0[1]), (float)(z+b0[2]))
                .color(r,g,b,a)
                .texture(0f,0f)
                .overlay(OverlayTexture.DEFAULT_UV)
                .normal(nx,ny,nz);
    }

    private Entity getTarget() {
        return mc.targetedEntity;
    }

}
