package ru.mytheria.api.util.animations;

public interface AnimationCalculation {
    default double calculation(double value){
        return 0;
    }
}
