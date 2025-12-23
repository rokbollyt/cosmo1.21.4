package ru.mytheria.api.util.shader.impl.rect;

import ru.mytheria.api.util.shader.common.build.AbstractBuilder;
import ru.mytheria.api.util.shader.common.states.ColorState;
import ru.mytheria.api.util.shader.common.states.RadiusState;
import ru.mytheria.api.util.shader.common.states.SizeState;

public final class Rectangle extends AbstractBuilder<RectShader> {

    private SizeState size;
    private RadiusState radius;
    private ColorState color;
    private float smoothness;

    public Rectangle size(SizeState size) {
        this.size = size;
        return this;
    }

    public Rectangle radius(RadiusState radius) {
        this.radius = radius;
        return this;
    }

    public Rectangle color(ColorState color) {
        this.color = color;
        return this;
    }

    public Rectangle smoothness(float smoothness) {
        this.smoothness = smoothness;
        return this;
    }

    @Override
    protected RectShader _build() {
        return new RectShader(
            this.size,
            this.radius,
            this.color,
            this.smoothness
        );
    }

    @Override
    protected void reset() {
        this.size = SizeState.NONE;
        this.radius = RadiusState.NO_ROUND;
        this.color = ColorState.TRANSPARENT;
        this.smoothness = 1.0f;
    }

}