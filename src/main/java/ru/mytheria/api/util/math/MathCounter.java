package ru.mytheria.api.util.math;


import lombok.AccessLevel;
import lombok.Getter;
import lombok.experimental.FieldDefaults;

@Getter
@FieldDefaults(level = AccessLevel.PRIVATE)
public class MathCounter {

    public MathCounter() {
        this.lastMS = System.currentTimeMillis();
    }

    long lastMS;

    public static MathCounter create() {
        return new MathCounter();
    }

    public void resetCounter() {
        lastMS = System.currentTimeMillis();
    }

    public boolean isReached(long time) {
        return System.currentTimeMillis() - lastMS > time;
    }

    public void setTime(long time) {
        lastMS = time;
    }

    public void addTime(long time) {
        lastMS += time;
    }

    public long getTime() {
        return System.currentTimeMillis() - lastMS;
    }

    public boolean isRunning() {
        return System.currentTimeMillis() - lastMS <= 0;
    }

    public boolean hasTimeElapsed() {
        return lastMS < System.currentTimeMillis();
    }
}
