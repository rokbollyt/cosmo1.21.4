package fun.cosmo.api.events.impl;

import fun.cosmo.api.events.Event;

public class EventTravelRotation extends Event {

    private float yaw;
    private float pitch;

    public EventTravelRotation(float yaw, float pitch) {
        this.yaw = yaw;
        this.pitch = pitch;
    }

    public float getYaw() { return yaw; }
    public void setYaw(float yaw) { this.yaw = yaw; }

    public float getPitch() { return pitch; }
    public void setPitch(float pitch) { this.pitch = pitch; }
}
