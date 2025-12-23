package ru.mytheria.api.events.impl;

import ru.mytheria.api.events.Event;

public class EventLEntityRender extends Event {

    private boolean shouldRender = true;

    private float yaw;
    private float pitch;
    private float prevYaw;
    private float prevPitch;
    private float bodyYaw;
    private float prevBodyYaw;

    public boolean shouldRender() {
        return shouldRender;
    }

    public void setShouldRender(boolean shouldRender) {
        this.shouldRender = shouldRender;
    }

    public float getYaw() {
        return yaw;
    }

    public void setYaw(float yaw) {
        this.yaw = yaw;
    }

    public float getPitch() {
        return pitch;
    }

    public void setPitch(float pitch) {
        this.pitch = pitch;
    }

    public float getPrevYaw() {
        return prevYaw;
    }

    public void setPrevYaw(float prevYaw) {
        this.prevYaw = prevYaw;
    }

    public float getPrevPitch() {
        return prevPitch;
    }

    public void setPrevPitch(float prevPitch) {
        this.prevPitch = prevPitch;
    }

    public float getBodyYaw() {
        return bodyYaw;
    }

    public void setBodyYaw(float bodyYaw) {
        this.bodyYaw = bodyYaw;
    }

    public float getPrevBodyYaw() {
        return prevBodyYaw;
    }

    public void setPrevBodyYaw(float prevBodyYaw) {
        this.prevBodyYaw = prevBodyYaw;
    }
}
