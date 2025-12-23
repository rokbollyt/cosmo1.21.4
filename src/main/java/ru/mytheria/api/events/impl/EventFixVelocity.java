package ru.mytheria.api.events.impl;

import ru.mytheria.api.events.Event;

public class EventFixVelocity extends Event {

    private float velocity;
    private float movementInput;
    private float speed;

    public EventFixVelocity(float velocity, float movementInput, float speed) {
        this.velocity = velocity;
        this.movementInput = movementInput;
        this.speed = speed;
    }

    public float getVelocity() {
        return velocity;
    }

    public void setVelocity(float velocity) {
        this.velocity = velocity;
    }

    public float getMovementInput() {
        return movementInput;
    }

    public float getSpeed() {
        return speed;
    }
}
