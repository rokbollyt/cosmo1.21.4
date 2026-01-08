package fun.cosmo.api.events.impl;

import lombok.Getter;
import lombok.Setter;
import meteordevelopment.orbit.ICancellable;

// Событие для контроля тумана
@Getter
@Setter
public class FogEvent implements ICancellable {
    private boolean cancelled = false;
    private float distance;
    private int color;

    public FogEvent(float distance, int color) {
        this.distance = distance;
        this.color = color;
    }

    @Override
    public boolean isCancelled() { return cancelled; }
    @Override
    public void setCancelled(boolean cancelled) { this.cancelled = cancelled; }
}