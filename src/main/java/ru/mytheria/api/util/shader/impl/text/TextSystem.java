package ru.mytheria.api.util.shader.impl.text;

import java.awt.Color;

import ru.mytheria.api.util.fonts.main.MsdfFont;
import ru.mytheria.api.util.shader.common.build.AbstractBuilder;
import ru.mytheria.api.util.fonts.TextRenderer;

public final class TextSystem extends AbstractBuilder<TextRenderer> {

    private MsdfFont font;
    private String text;
    private float size;
    private float thickness;
    private int color;
    private float smoothness;
    private float spacing;
    private int outlineColor;
    private float outlineThickness;

    public TextSystem font(MsdfFont font) {
        this.font = font;
        return this;
    }

    public TextSystem text(String text) {
        this.text = text;
        return this;
    }

    public TextSystem size(float size) {
        this.size = size;
        return this;
    }

    public TextSystem thickness(float thickness) {
        this.thickness = thickness;
        return this;
    }

    public TextSystem color(Color color) {
        return this.color(color.getRGB());
    }

    public TextSystem color(int color) {
        this.color = color;
        return this;
    }

    public TextSystem smoothness(float smoothness) {
        this.smoothness = smoothness;
        return this;
    }

    public TextSystem spacing(float spacing) {
        this.spacing = spacing;
        return this;
    }

    public TextSystem outline(Color color, float thickness) {
        return this.outline(color.getRGB(), thickness);
    }

    public TextSystem outline(int color, float thickness) {
        this.outlineColor = color;
        this.outlineThickness = thickness;
        return this;
    }

    @Override
    protected TextRenderer _build() {
        return new TextRenderer(
                this.font,
                this.text,
                this.size,
                this.thickness,
                this.color,
                this.smoothness,
                this.spacing,
                this.outlineColor,
                this.outlineThickness
        );
    }

    @Override
    protected void reset() {
        this.font = null;
        this.text = "";
        this.size = 0.0f;
        this.thickness = 0.05f;
        this.color = -1;
        this.smoothness = 0.5f;
        this.spacing = 0.0f;
        this.outlineColor = 0;
        this.outlineThickness = 0.0f;
    }

    public float getStringWidth() {
        if (font == null || text == null) return 0f;
        return font.getWidth(text, size) + spacing * (text.length() - 1);
    }

    public float getStringHeight() {
        if (font == null) return 0f;
        return font.getMetrics().lineHeight() * size;
    }

    public static float getStringWidth(MsdfFont font, String text, float size, float spacing) {
        if (font == null || text == null) return 0f;
        return font.getWidth(text, size) + spacing * (text.length() - 1);
    }

    public float getWidth() {
        return getStringWidth();
    }
}
