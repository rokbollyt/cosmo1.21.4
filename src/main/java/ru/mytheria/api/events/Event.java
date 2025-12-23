package ru.mytheria.api.events;

import lombok.Getter;

public class Event {

    @Getter
    protected boolean canceled = false;

    public void cancel() {
        this.canceled = true;
    }
}
//sd