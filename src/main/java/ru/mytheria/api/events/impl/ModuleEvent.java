package ru.mytheria.api.events.impl;

import ru.mytheria.api.events.Event;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import ru.mytheria.api.module.Module;

public class ModuleEvent {
    @Getter
    @RequiredArgsConstructor
    public static class ToggleEvent extends Event {
        private final Module moduleLayer;
    }
}
