package fun.cosmo.api.events.impl;


import fun.cosmo.api.events.Event;
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
