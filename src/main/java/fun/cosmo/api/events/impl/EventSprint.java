package fun.cosmo.api.events.impl;

import fun.cosmo.api.events.Event;

public class EventSprint extends Event {

    private boolean sprinting;

    public EventSprint(boolean sprinting) {
        this.sprinting = sprinting;
    }

    public boolean isSprinting() { return sprinting; }
    public void setSprinting(boolean sprinting) { this.sprinting = sprinting; }
}
