package ru.mytheria.api.util.fonts.main;

import net.minecraft.client.util.math.MatrixStack;
import org.joml.Matrix4f;
import ru.mytheria.api.util.fonts.TextRenderer;

public class StyledFont {
    private final MsdfFont font;
    private final float size;

    public StyledFont(String fontDataJsonName, String fontAtlasName, float size) {
        this.font = MsdfFont.builder()
                .data(fontDataJsonName)
                .atlas(fontAtlasName)
                .build();
        this.size = size;
    }

    public void drawString(String text, float x, float y, int color) {
        TextRenderer renderer = new TextRenderer(font, text, size, 0.05f, color, 0.5f, 0.0f, 0, 0.0f);
        Matrix4f matrix = new MatrixStack().peek().getPositionMatrix();
        renderer.render(matrix, x, y, 0);
    }

    public float getWidth(String text) {
        return font.getWidth(text, size);
    }
}