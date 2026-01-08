package fun.cosmo.api.events.impl;

import fun.cosmo.api.events.Event;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import fun.cosmo.api.module.Module;

public class ModuleEvent {
    @Getter
    @RequiredArgsConstructor
    public static class ToggleEvent extends Event {
        private final Module moduleLayer;
    }
}
