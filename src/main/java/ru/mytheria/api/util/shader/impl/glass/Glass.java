package ru.mytheria.api.util.shader.impl.glass;

import ru.mytheria.api.util.shader.common.build.AbstractBuilder;
import ru.mytheria.api.util.shader.common.states.ColorState;
import ru.mytheria.api.util.shader.common.states.RadiusState;
import ru.mytheria.api.util.shader.common.states.SizeState;

public final class Glass extends AbstractBuilder<GlassShader> {

    private SizeState size;
    private float glassOffset;
    private RadiusState radius;
    private ColorState color;
    private float smoothness;
    private boolean createFrameBuffer;

    public Glass size(SizeState size) {
        this.size = size;
        return this;
    }

    public Glass glassOffset(float glassOffset) {
        this.glassOffset = glassOffset;
        return this;
    }

    public Glass radius(RadiusState radius) {
        this.radius = radius;
        return this;
    }

    public Glass color(ColorState color) {
        this.color = color;
        return this;
    }

    public Glass smoothness(float smoothness) {
        this.smoothness = smoothness;
        return this;
    }

    public Glass useFramebuffer(boolean useFramebuffer) {
        this.createFrameBuffer = useFramebuffer;
        return this;
    }

    @Override
    protected GlassShader _build() {
        return new GlassShader(
                this.size,
                this.glassOffset,
                this.radius,
                this.color,
                this.smoothness,
                this.createFrameBuffer
        );
    }

    @Override
    protected void reset() {
        this.size = SizeState.NONE;
        this.glassOffset = 1.0f;
        this.radius = RadiusState.NO_ROUND;
        this.color = ColorState.TRANSPARENT;
        this.smoothness = 1.0f;
        this.createFrameBuffer = true;
    }
}
