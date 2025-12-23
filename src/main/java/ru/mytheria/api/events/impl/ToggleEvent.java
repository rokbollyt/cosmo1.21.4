package ru.mytheria.api.events.impl;

import com.google.common.eventbus.Subscribe;
import meteordevelopment.orbit.EventHandler;

public class ToggleEvent {
    @EventHandler
    public void onToggle(ModuleEvent.ToggleEvent event) {
        event.getModuleLayer().toggleEnabled();
    }
}
