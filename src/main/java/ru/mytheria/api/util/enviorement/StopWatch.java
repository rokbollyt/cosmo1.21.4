package ru.mytheria.api.util.enviorement;

public class StopWatch {

    private long lastMS;
    private long delay;

    public StopWatch() {
        reset();
    }

    public void setDelay(long delay) {
        this.delay = delay;
    }

    public void reset() {
        lastMS = System.currentTimeMillis();
    }

    public void setLastMS(long lastMS) {
        this.lastMS = lastMS;
    }

    public boolean hasTimeElapsed() {
        return (System.currentTimeMillis() - lastMS) >= delay;
    }

    public long getElapsedTime() {
        return System.currentTimeMillis() - lastMS;
    }
}
