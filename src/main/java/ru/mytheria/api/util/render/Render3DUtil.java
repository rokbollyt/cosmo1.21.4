package ru.mytheria.api.util.render;

import com.mojang.blaze3d.platform.GlStateManager;
import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gl.ShaderProgramKeys;
import net.minecraft.client.render.*;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.util.Identifier;
import org.joml.Matrix4f;
import org.joml.Vector4i;
import org.lwjgl.opengl.GL11;

import java.util.*;

public final class Render3DUtil {

    private static final MinecraftClient mc = MinecraftClient.getInstance();
    private static final Tessellator TESSELLATOR = Tessellator.getInstance();

    private static final List<Texture> TEXTURES = new ArrayList<>();
    private static final List<Texture> TEXTURES_DEPTH = new ArrayList<>();

    private Render3DUtil() {}

    /* ==========================
       PUBLIC API
       ========================== */

    public static void drawTexture(MatrixStack.Entry entry,
                                   Identifier texture,
                                   float x, float y,
                                   float w, float h,
                                   Vector4i color,
                                   boolean depth) {

        Texture t = new Texture(entry, texture, x, y, w, h, color);
        if (depth) TEXTURES_DEPTH.add(t);
        else TEXTURES.add(t);
    }

    /* ==========================
       CALL FROM WORLD RENDER
       ========================== */

    public static void render() {
        renderBatch(TEXTURES, false);
        renderBatch(TEXTURES_DEPTH, true);
    }

    /* ==========================
       INTERNAL
       ========================== */

    private static void renderBatch(List<Texture> batch, boolean depth) {
        if (batch.isEmpty()) return;

        RenderSystem.enableBlend();
        RenderSystem.blendFunc(
                GlStateManager.SrcFactor.SRC_ALPHA,
                GlStateManager.DstFactor.ONE_MINUS_CONSTANT_ALPHA
        );

        if (depth) {
            RenderSystem.enableDepthTest();
            RenderSystem.depthMask(false);
        } else {
            RenderSystem.disableDepthTest();
        }

        RenderSystem.setShader(ShaderProgramKeys.POSITION_TEX_COLOR);

        Set<Identifier> ids = new LinkedHashSet<>();
        for (Texture t : batch) ids.add(t.id);

        for (Identifier id : ids) {
            RenderSystem.setShaderTexture(0, id);

            BufferBuilder buffer = TESSELLATOR.begin(
                    VertexFormat.DrawMode.QUADS,
                    VertexFormats.POSITION_TEXTURE_COLOR
            );

            for (Texture t : batch) {
                if (!t.id.equals(id)) continue;
                quad(buffer, t);
            }

            BufferRenderer.drawWithGlobalProgram(buffer.end());
        }

        if (depth) {
            RenderSystem.depthMask(true);
            RenderSystem.disableDepthTest();
        }

        RenderSystem.disableBlend();
        batch.clear();
    }

    private static void quad(BufferBuilder b, Texture t) {
        Matrix4f m = t.entry.getPositionMatrix();

        b.vertex(m, t.x, t.y + t.h, 0)
                .texture(0, 0)
                .color(t.color.x);

        b.vertex(m, t.x + t.w, t.y + t.h, 0)
                .texture(1, 0)
                .color(t.color.y);

        b.vertex(m, t.x + t.w, t.y, 0)
                .texture(1, 1)
                .color(t.color.z);

        b.vertex(m, t.x, t.y, 0)
                .texture(0, 1)
                .color(t.color.w);
    }

    /* ==========================
       DATA
       ========================== */

    private record Texture(
            MatrixStack.Entry entry,
            Identifier id,
            float x, float y,
            float w, float h,
            Vector4i color
    ) {}
}
