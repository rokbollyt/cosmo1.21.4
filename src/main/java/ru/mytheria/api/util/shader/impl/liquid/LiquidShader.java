package ru.mytheria.api.util.shader.impl.liquid;


import com.google.common.base.Supplier;
import com.google.common.base.Suppliers;
import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gl.*;
import net.minecraft.client.render.*;
import org.joml.Matrix4f;
import ru.mytheria.api.util.shader.common.event.IRenderer;
import ru.mytheria.api.util.shader.common.providers.ResourceProvider;
import ru.mytheria.api.util.shader.common.states.ColorState;
import ru.mytheria.api.util.shader.common.states.RadiusState;
import ru.mytheria.api.util.shader.common.states.SizeState;

public record LiquidShader(SizeState sizeState,
                           RadiusState radius,
                           ColorState colorState,
                           float smoothness,
                           float glassDirection,
                           float glassQuality,
                           float glassSize,
                           boolean createFrameBuffer) implements IRenderer {

    private static final ShaderProgramKey LIQUID_GLASS_SHADER_KEY = new ShaderProgramKey(
            ResourceProvider.getShaderIdentifier("liquid"),
            VertexFormats.POSITION_COLOR,
            Defines.EMPTY
    );

    private static final Supplier<SimpleFramebuffer> TEMP_FBO_SUPPLIER = Suppliers
            .memoize(() -> new SimpleFramebuffer(1920, 1080, true));

    private static final Framebuffer MAIN_FBO = MinecraftClient.getInstance().getFramebuffer();

    @Override
    public void render(Matrix4f matrix, float x, float y, float z) {
        SimpleFramebuffer fbo = TEMP_FBO_SUPPLIER.get();

        if (createFrameBuffer) {
            if (fbo.textureWidth != MAIN_FBO.textureWidth || fbo.textureHeight != MAIN_FBO.textureHeight) {
                fbo.resize(MAIN_FBO.textureWidth, MAIN_FBO.textureHeight);
            }
        }

        RenderSystem.enableBlend();
        RenderSystem.defaultBlendFunc();
        RenderSystem.disableCull();

        if (createFrameBuffer) {
            fbo.beginWrite(false);
            MAIN_FBO.draw(fbo.textureWidth, fbo.textureHeight);
            MAIN_FBO.beginWrite(false);
        }

        RenderSystem.setShaderTexture(0, createFrameBuffer ? fbo.getColorAttachment() : MAIN_FBO.getColorAttachment());

        float width = this.sizeState.width();
        float height = this.sizeState.height();

        ShaderProgram shader = RenderSystem.setShader(LIQUID_GLASS_SHADER_KEY);

        shader.getUniform("Size").set(width, height);
        shader.getUniform("Radius").set(radius.radius1(), radius.radius2(), radius.radius3(), radius.radius4());
        shader.getUniform("Smoothness").set(smoothness);
        shader.getUniform("GlassDirection").set(glassDirection);
        shader.getUniform("GlassQuality").set(glassQuality);
        shader.getUniform("GlassSize").set(glassSize);

        BufferBuilder builder = Tessellator.getInstance().begin(VertexFormat.DrawMode.QUADS, VertexFormats.POSITION_COLOR);

        builder.vertex(matrix, x, y, z).color(this.colorState.color1());
        builder.vertex(matrix, x, y + height, z).color(this.colorState.color2());
        builder.vertex(matrix, x + width, y + height, z).color(this.colorState.color3());
        builder.vertex(matrix, x + width, y, z).color(this.colorState.color4());

        BufferRenderer.drawWithGlobalProgram(builder.end());

        RenderSystem.enableCull();
        RenderSystem.disableBlend();
    }

    private static float[] intToFloatColor(int colorInt) {
        float a = ((colorInt >> 24) & 0xFF) / 255f;
        float r = ((colorInt >> 16) & 0xFF) / 255f;
        float g = ((colorInt >> 8) & 0xFF) / 255f;
        float b = (colorInt & 0xFF) / 255f;
        return new float[]{r, g, b, a};
    }


}
