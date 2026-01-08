package fun.cosmo.api.events.impl;

import meteordevelopment.orbit.EventHandler;

public class ToggleEvent {
    @EventHandler
    public void onToggle(ModuleEvent.ToggleEvent event) {
        event.getModuleLayer().toggleEnabled();
    }
}
