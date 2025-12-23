package ru.mytheria.api.util.math;

import lombok.Setter;

public class Time {
    @Setter private long startTime = System.currentTimeMillis();

    public void reset() {
        startTime = System.currentTimeMillis();
    }

    public boolean passed(long time) {
        return System.currentTimeMillis() - startTime > time;
    }

    public long getElapsed() {
        return System.currentTimeMillis() - startTime;
    }
}