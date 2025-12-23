package ru.mytheria.api.events.impl;

import ru.mytheria.api.events.Event;

public class EventRotationFix extends Event {

    private float yaw;
    private float pitch;

    public float getYaw() { return yaw; }
    public void setYaw(float yaw) { this.yaw = yaw; }

    public float getPitch() { return pitch; }
    public void setPitch(float pitch) { this.pitch = pitch; }

    public void cancel() { super.cancel(); }
    public void uncancel() { this.canceled = false; }
}
