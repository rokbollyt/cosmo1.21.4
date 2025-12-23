package ru.mytheria.api.util.render;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.render.RenderTickCounter;
import net.minecraft.util.Identifier;
import org.jetbrains.annotations.Nullable;
import ru.mytheria.api.util.shader.common.states.*;
import ru.mytheria.api.util.shader.impl.blur.Blur;
import ru.mytheria.api.util.shader.impl.blur.BlurShader;
import ru.mytheria.api.util.shader.impl.border.Border;
import ru.mytheria.api.util.shader.impl.border.BorderShader;
import ru.mytheria.api.util.shader.impl.glass.Glass;
import ru.mytheria.api.util.shader.impl.glass.GlassShader;
import ru.mytheria.api.util.shader.impl.liquid.Liquid;
import ru.mytheria.api.util.shader.impl.liquid.LiquidShader;
import ru.mytheria.api.util.shader.impl.rect.RectShader;
import ru.mytheria.api.util.shader.impl.rect.Rectangle;
import ru.mytheria.api.util.shader.impl.texture.Texture;
import ru.mytheria.api.util.shader.impl.texture.TextureShader;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.texture.AbstractTexture;
import net.minecraft.client.texture.NativeImage;
import net.minecraft.client.texture.NativeImageBackedTexture;
import net.minecraft.client.util.math.MatrixStack;
import org.joml.Matrix4f;

import java.awt.*;
import java.awt.image.BufferedImage;

import static ru.mytheria.api.clientannotation.QuickImport.mc;

public class RenderEngine {

    public static void drawTexture( MatrixStack stack, float x, float y, float width, float height, float radius, AbstractTexture texture, Color color ) {
        TextureShader textureShader = new Texture()
                .size(new SizeState(width, height))
                .radius(new RadiusState(radius))
                .texture(1f, 1f, 1f, 1f, texture)
                .color(new ColorState(color))
                .build();
        textureShader.render(stack.peek().getPositionMatrix(), x, y);
    }

    public static void drawTexture( MatrixStack stack, float x, float y, float width, float height, float radius, float u, float v, float textWidth, float texHeight, Identifier texture, Color color ) {
        TextureShader built = new Texture()
                .size(new SizeState(width, height))
                .radius(new RadiusState(radius))
                .texture(u, v, textWidth, texHeight, mc.getTextureManager().getTexture(texture))
                .color(new ColorState(color))
                .build();
        built.render(stack.peek().getPositionMatrix(), x, y);
    }

    public static void drawRectangle( MatrixStack matrices, float x, float y, float width, float height, ColorState color, RadiusState radius, float... extraParams) {
        Matrix4f matrix = matrices.peek().getPositionMatrix();

        float smoothness = get(extraParams, 0, 1.0f);
        RectShader rect = new Rectangle()
                .size(new SizeState(width, height))
                .radius(radius)
                .color(color)
                .smoothness(smoothness)
                .build();
        rect.render(matrix, x, y);
    }

    public static void drawGlassRectangle( MatrixStack matrices, float x, float y, float width, float height, ColorState color, RadiusState radius, float... extraParams) {
        Matrix4f matrix = matrices.peek().getPositionMatrix();

        float smoothness = get(extraParams, 0, 1.0f);
        float glassOffset = get(extraParams, 1, 1.0f);
        boolean useFramebuffer = getBool(extraParams, 2, true);
        GlassShader glass = new Glass()
                .size(new SizeState(width, height))
                .radius(radius)
                .color(color)
                .smoothness(smoothness)
                .glassOffset(glassOffset)
                .useFramebuffer(useFramebuffer)
                .build();
        glass.render(matrix, x, y);
    }

    public static void drawBlurRectangle( MatrixStack matrices, float x, float y, float width, float height, ColorState color, RadiusState radius, float... extraParams) {
        Matrix4f matrix = matrices.peek().getPositionMatrix();

        float smoothness = get(extraParams, 0, 1.0f);
        float blurRadius = get(extraParams, 1, 8.0f);
        BlurShader blur = new Blur()
                .size(new SizeState(width, height))
                .radius(radius)
                .color(color)
                .smoothness(smoothness)
                .blurRadius(blurRadius)
                .build();
        blur.render(matrix, x, y);
    }

    public static void drawLiquid(MatrixStack matrices, float x, float y, float width, float height,
                                  ColorState color, RadiusState radius, float... extraParams) {
        Matrix4f matrix = matrices.peek().getPositionMatrix();

        float smoothness     = get(extraParams, 0, 1.0f);
        float glassDirection = get(extraParams, 1, 1.0f);
        float glassQuality   = get(extraParams, 2, 1.0f);
        float glassSize      = get(extraParams, 3, 1.0f);

        LiquidShader liquid = new Liquid()
                .size(new SizeState(width, height))
                .radius(radius)
                .color(color)
                .smoothness(smoothness)
                .glassDirection(glassDirection)
                .glassQuality(glassQuality)
                .glassSize(glassSize)
                .build();

        liquid.render(matrix, x, y);
    }


    public static void drawBorder( MatrixStack matrices, float x, float y, float width, float height, ColorState color, RadiusState radius, float... extraParams) {
        Matrix4f matrix = matrices.peek().getPositionMatrix();

        float thickness = get(extraParams, 0, 1.0f);
        float internalSmooth = get(extraParams, 1, 1.0f);
        float externalSmooth = get(extraParams, 2, 1.0f);
        BorderShader border = new Border()
                .size(new SizeState(width, height))
                .radius(radius)
                .color(color)
                .thickness(thickness)
                .smoothness(internalSmooth, externalSmooth)
                .build();
        border.render(matrix, x, y);
    }

    public static AbstractTexture convert(BufferedImage image) {
        if (image == null) {
            System.out.println("RenderEngine.convert: Received null image, returning null texture.");
            return null;
        }

        int width = image.getWidth();
        int height = image.getHeight();
        NativeImage img = new NativeImage(width, height, false);
        for (int y = 0; y < height; y++) {
            for (int x = 0; x < width; x++) {
                img.setColorArgb(x, y, image.getRGB(x, y));
            }
        }

        return new NativeImageBackedTexture(img);
    }

    private static float get(float[] arr, int index, float def) {
        return index < arr.length ? arr[index] : def;
    }

    private static boolean getBool(float[] arr, int index, boolean def) {
        return index < arr.length ? arr[index] > 0.5f : def;
    }

    public static void startScissor( DrawContext context, float x, float y, float width, float height ) {
        context.enableScissor((int) x, (int) y, (int) (x + width), (int) (y + height));
    }

    public static void stopScissor( DrawContext context ) {
        context.disableScissor();
    }

    public static float getTickDelta() {
        MinecraftClient mc = MinecraftClient.getInstance();
        RenderTickCounter rtc = mc.getRenderTickCounter();
        return rtc.getTickDelta(false);
    }

}
