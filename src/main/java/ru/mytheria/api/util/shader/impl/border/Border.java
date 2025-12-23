package ru.mytheria.api.util.shader.impl.border;

import ru.mytheria.api.util.shader.common.build.AbstractBuilder;
import ru.mytheria.api.util.shader.common.states.ColorState;
import ru.mytheria.api.util.shader.common.states.RadiusState;
import ru.mytheria.api.util.shader.common.states.SizeState;

public final class Border extends AbstractBuilder<BorderShader> {

    private SizeState size;
    private RadiusState radius;
    private ColorState color;
    private float thickness;
    private float internalSmoothness, externalSmoothness;

    public Border size(SizeState size) {
        this.size = size;
        return this;
    }

    public Border radius(RadiusState radius) {
        this.radius = radius;
        return this;
    }

    public Border color(ColorState color) {
        this.color = color;
        return this;
    }

    public Border thickness(float thickness) {
        this.thickness = thickness;
        return this;
    }

    public Border smoothness(float internalSmoothness, float externalSmoothness) {
        this.internalSmoothness = internalSmoothness;
        this.externalSmoothness = externalSmoothness;
        return this;
    }

    @Override
    protected BorderShader _build() {
        return new BorderShader(
            this.size,
            this.radius,
            this.color,
            this.thickness,
            this.internalSmoothness, this.externalSmoothness
        );
    }

    @Override
    protected void reset() {
        this.size = SizeState.NONE;
        this.radius = RadiusState.NO_ROUND;
        this.color = ColorState.TRANSPARENT;
        this.thickness = 0.0f;
        this.internalSmoothness = 1.0f;
        this.externalSmoothness = 1.0f;
    }

}