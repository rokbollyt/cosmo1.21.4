package ru.mytheria.api.events.impl;


import ru.mytheria.api.events.Event;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;

@Getter
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class KeyEvent extends Event {

    long window;
    int key;
    int scancode;
    int action;
    int modifiers;

}
