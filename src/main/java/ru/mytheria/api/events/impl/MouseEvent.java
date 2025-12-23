package ru.mytheria.api.events.impl;

import lombok.AllArgsConstructor;
import lombok.Getter;
import ru.mytheria.api.events.Event;

@AllArgsConstructor @Getter
public class MouseEvent extends Event {
    private int button, action;
}