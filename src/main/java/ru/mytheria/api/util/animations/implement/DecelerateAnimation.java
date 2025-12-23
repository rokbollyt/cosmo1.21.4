package ru.mytheria.api.util.animations.implement;

import ru.mytheria.api.util.animations.Animation;

public class DecelerateAnimation extends Animation {

    @Override
    public double calculation(double value) {
        double x = value / ms;
        return 1 - (x - 1) * (x - 1);
    }
}
