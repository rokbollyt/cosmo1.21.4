package fun.cosmo.api.events.impl;

import lombok.AllArgsConstructor;
import lombok.Getter;
import fun.cosmo.api.events.Event;

@AllArgsConstructor @Getter
public class MouseEvent extends Event {
    private int button, action;
}