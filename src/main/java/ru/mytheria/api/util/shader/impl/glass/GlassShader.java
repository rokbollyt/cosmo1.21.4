package ru.mytheria.api.util.shader.impl.glass;

import com.google.common.base.Supplier;
import com.google.common.base.Suppliers;
import com.mojang.blaze3d.systems.RenderSystem;
import ru.mytheria.api.util.shader.common.event.IRenderer;
import ru.mytheria.api.util.shader.common.providers.ResourceProvider;
import ru.mytheria.api.util.shader.common.states.ColorState;
import ru.mytheria.api.util.shader.common.states.RadiusState;
import ru.mytheria.api.util.shader.common.states.SizeState;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gl.*;
import net.minecraft.client.render.*;
import org.joml.Matrix4f;

public record GlassShader(SizeState sizeState, float glassOffset, RadiusState radius, ColorState colorState, float smoothness, boolean createFrameBuffer) implements IRenderer {
    private static final ShaderProgramKey RECTANGLE_SHADER_KEY = new ShaderProgramKey(ResourceProvider.getShaderIdentifier("glass"),
            VertexFormats.POSITION_COLOR, Defines.EMPTY);

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

        float width = this.sizeState.width(), height = this.sizeState.height();
        ShaderProgram shader = RenderSystem.setShader(RECTANGLE_SHADER_KEY);

        shader.getUniform("Size").set(width, height);
        shader.getUniform("GlassOffset").set(glassOffset);
        shader.getUniform("Radius").set(radius.radius1(), radius.radius2(), radius.radius3(), radius.radius4());
        shader.getUniform("Smoothness").set(smoothness);

        BufferBuilder builder = Tessellator.getInstance().begin(VertexFormat.DrawMode.QUADS, VertexFormats.POSITION_COLOR);

        builder.vertex(matrix, x, y, z).color(this.colorState.color1());
        builder.vertex(matrix, x, y + height, z).color(this.colorState.color2());
        builder.vertex(matrix, x + width, y + height, z).color(this.colorState.color3());
        builder.vertex(matrix, x + width, y, z).color(this.colorState.color4());

        BufferRenderer.drawWithGlobalProgram(builder.end());

        RenderSystem.enableCull();
        RenderSystem.disableBlend();
    }
}
