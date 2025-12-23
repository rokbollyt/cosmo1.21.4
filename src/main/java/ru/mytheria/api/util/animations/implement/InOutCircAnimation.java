package ru.mytheria.api.util.animations.implement;

import ru.mytheria.api.util.animations.Animation;

public class InOutCircAnimation extends Animation {

    @Override
    public double calculation(double value) {
        double x = value / ms;

        return x < 0.5
                ? (1 - Math.sqrt(1 - Math.pow(2 * x, 2))) / 2
                : (Math.sqrt(1 - Math.pow(-2 * x + 2, 2)) + 1) / 2;
    }
}
