package ru.mytheria.api.util.shader.impl.texture;

import ru.mytheria.api.util.shader.common.build.AbstractBuilder;
import ru.mytheria.api.util.shader.common.states.ColorState;
import ru.mytheria.api.util.shader.common.states.RadiusState;
import ru.mytheria.api.util.shader.common.states.SizeState;
import net.minecraft.client.texture.AbstractTexture;

public final class Texture extends AbstractBuilder<TextureShader> {

    private SizeState size;
    private RadiusState radius;
    private ColorState color;
    private float smoothness;
    private float u, v;
    private float texWidth, texHeight;
    private int textureId;

    public Texture size(SizeState size) {
        this.size = size;
        return this;
    }

    public Texture radius(RadiusState radius) {
        this.radius = radius;
        return this;
    }

    public Texture color(ColorState color) {
        this.color = color;
        return this;
    }

    public Texture smoothness(float smoothness) {
        this.smoothness = smoothness;
        return this;
    }

    public Texture texture(float u, float v, float texWidth, float texHeight, AbstractTexture texture) {
        return texture(u, v, texWidth, texHeight, texture.getGlId());
    }

    public Texture texture(float u, float v, float texWidth, float texHeight, int textureId) {
        this.u = u;
        this.v = v;
        this.texWidth = texWidth;
        this.texHeight = texHeight;
        this.textureId = textureId;
        return this;
    }

    @Override
    protected TextureShader _build() {
        return new TextureShader(
            this.size,
            this.radius,
            this.color,
            this.smoothness,
            this.u, this.v,
            this.texWidth, this.texHeight,
            this.textureId
        );
    }

    @Override
    protected void reset() {
        this.size = SizeState.NONE;
        this.radius = RadiusState.NO_ROUND;
        this.color = ColorState.WHITE;
        this.smoothness = 1.0f;
        this.u = 0.0f;
        this.v = 0.0f;
        this.texWidth = 0.0f;
        this.texHeight = 0.0f;
        this.textureId = 0;
    }

}