package ru.mytheria.api.util.shader.impl.blur;

import ru.mytheria.api.util.shader.common.build.AbstractBuilder;
import ru.mytheria.api.util.shader.common.states.ColorState;
import ru.mytheria.api.util.shader.common.states.RadiusState;
import ru.mytheria.api.util.shader.common.states.SizeState;

public final class Blur extends AbstractBuilder<BlurShader> {

    private SizeState size;
    private RadiusState radius;
    private ColorState color;
    private float smoothness;
    private float blurRadius;

    public Blur size(SizeState size) {
        this.size = size;
        return this;
    }

    public Blur radius(RadiusState radius) {
        this.radius = radius;
        return this;
    }

    public Blur color(ColorState color) {
        this.color = color;
        return this;
    }

    public Blur smoothness(float smoothness) {
        this.smoothness = smoothness;
        return this;
    }

    public Blur blurRadius(float blurRadius) {
        this.blurRadius = blurRadius;
        return this;
    }

    @Override
    protected BlurShader _build() {
        return new BlurShader(
            this.size,
            this.radius,
            this.color,
            this.smoothness,
            this.blurRadius
        );
    }

    @Override
    protected void reset() {
        this.size = SizeState.NONE;
        this.radius = RadiusState.NO_ROUND;
        this.color = ColorState.WHITE;
        this.smoothness = 1.0f;
        this.blurRadius = 0.0f;
    }

}