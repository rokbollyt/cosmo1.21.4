package ru.mytheria.api.util.animations;


import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;
import ru.mytheria.api.util.math.MathCounter;

@Getter
@Setter
@Accessors(chain = true)
public abstract class Animation implements AnimationCalculation {
    private final MathCounter counter = new MathCounter();
    protected int ms;
    protected double value;

    protected Direction direction = Direction.FORWARDS;

    public void reset() {
        counter.resetCounter();
    }

    public boolean isDone() {
        return counter.isReached(ms);
    }

    public boolean isFinished(Direction direction) {
        return this.direction == direction && isDone();
    }

    public void setDirection(Direction direction) {
        if (this.direction != direction) {
            this.direction = direction;
            adjustTimer();
        }
    }

    private void adjustTimer() {
        counter.setTime(System.currentTimeMillis() - ((long) ms - Math.min(ms, counter.getTime())) );
    }

    public Double getOutput() {
        double time = (1 - calculation(counter.getTime())) * value;

        return direction == Direction.FORWARDS
                ? endValue()
                : isDone() ? 0.0 : time;
    }

    private double endValue() {
        return isDone()
                ? value
                : calculation(counter.getTime()) * value;
    }
}
