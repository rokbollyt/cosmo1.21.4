package fun.cosmo.api.events.impl;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.FieldDefaults;
import fun.cosmo.api.events.Event;

@Getter
@Setter
@FieldDefaults(level = AccessLevel.PRIVATE)
public class SwingDurationEvent extends Event {
    private float animation;
    private boolean cancelled;

    public SwingDurationEvent(float animation) {
        this.animation = animation;
        this.cancelled = false;
    }

    public boolean isCancelled() {
        return cancelled;
    }

    public void cancel() {
        this.cancelled = true;
    }

    public void uncancel() {
        this.cancelled = false;
    }
}