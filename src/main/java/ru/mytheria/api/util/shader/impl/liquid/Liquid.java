package ru.mytheria.api.util.shader.impl.liquid;

import ru.mytheria.api.util.shader.common.build.AbstractBuilder;
import ru.mytheria.api.util.shader.common.states.ColorState;
import ru.mytheria.api.util.shader.common.states.RadiusState;
import ru.mytheria.api.util.shader.common.states.SizeState;
import ru.mytheria.api.util.shader.impl.glass.GlassShader;


public final class Liquid extends AbstractBuilder<LiquidShader> {

    private SizeState size;
    private RadiusState radius;
    private ColorState color;
    private float smoothness;
    private float glassDirection;
    private float glassQuality;
    private float glassSize;
    private boolean createFrameBuffer;

    public Liquid size(SizeState size) {
        this.size = size;
        return this;
    }

    public Liquid radius(RadiusState radius) {
        this.radius = radius;
        return this;
    }

    public Liquid color(ColorState color) {
        this.color = color;
        return this;
    }

    public Liquid smoothness(float smoothness) {
        this.smoothness = smoothness;
        return this;
    }

    public Liquid glassDirection(float glassDirection) {
        this.glassDirection = glassDirection;
        return this;
    }

    public Liquid glassQuality(float glassQuality) {
        this.glassQuality = glassQuality;
        return this;
    }

    public Liquid glassSize(float glassSize) {
        this.glassSize = glassSize;
        return this;
    }

    public Liquid useFramebuffer(boolean useFramebuffer) {
        this.createFrameBuffer = useFramebuffer;
        return this;
    }

    @Override
    protected LiquidShader _build() {
        return new LiquidShader(
                this.size,
                this.radius,
                this.color,
                this.smoothness,
                this.glassDirection,
                this.glassQuality,
                this.glassSize,
                this.createFrameBuffer
        );
    }

    @Override
    protected void reset() {
        this.size = SizeState.NONE;
        this.radius = RadiusState.NO_ROUND;
        this.color = ColorState.TRANSPARENT;
        this.smoothness = 1.0f;
        this.glassDirection = 8.0f;
        this.glassQuality = 16.0f;
        this.glassSize = 4.0f;
        this.createFrameBuffer = true;
    }
}
