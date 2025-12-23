package ru.mytheria.api.events.impl;

import ru.mytheria.api.events.Event;

public class EventMoveInput extends Event {

    private float yaw;
    private boolean needFix;

    public float getYaw() { return yaw; }
    public void setYaw(float yaw) { this.yaw = yaw; }

    public boolean isNeedFix() { return needFix; }
    public void setNeedFix(boolean needFix) { this.needFix = needFix; }
}
