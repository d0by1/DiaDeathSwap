package eu.diaworlds.deathswap.utils.ticker;

import java.util.UUID;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;

public abstract class Ticked implements ITicked {

    private final String id;
    private final AtomicLong interval;
    private final AtomicInteger time;
    private Direction direction;

    public Ticked(long interval) {
        this(UUID.randomUUID().toString(), interval);
    }

    public Ticked(String id, long interval) {
        this.id = id;
        this.interval = new AtomicLong(interval);
        this.time = new AtomicInteger(0);
        this.direction = Direction.INCREMENT;
        this.register();
    }

    public abstract void onTick();

    @Override
    public void tick() {
        if (direction.equals(Direction.INCREMENT)) {
            time.incrementAndGet();
        } else {
            time.decrementAndGet();
        }
        this.onTick();
    }

    @Override
    public String getId() {
        return id;
    }

    @Override
    public long getInterval() {
        return interval.get();
    }

    @Override
    public int getTime() {
        return time.get();
    }

    @Override
    public void setTime(int time) {
        this.time.set(time);
    }

    public Direction getDirection() {
        return direction;
    }

    public void setDirection(Direction direction) {
        this.direction = direction;
    }

    public enum Direction {
        DECREMENT, INCREMENT
    }

}
