package fun.cosmo.api.events.impl;



import fun.cosmo.api.events.Event;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;

public class ChatScreenEvent extends Event {

    @Getter
    @RequiredArgsConstructor
    @FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
    public static class MouseClicked extends ChatScreenEvent {
        double mouseX;
        double mouseY;
        int button;
    }

    @Getter
    @RequiredArgsConstructor
    @FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
    public static class MouseReleased extends ChatScreenEvent {
        double mouseX;
        double mouseY;
        int button;
    }

}
