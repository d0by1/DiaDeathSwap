package eu.diaworlds.deathswap.tick;

import eu.diaworlds.deathswap.DeathSwap;
import lombok.Getter;
import lombok.Setter;

import java.util.UUID;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;

@Getter
@Setter
public abstract class TickedObject implements Ticked {

    private final AtomicLong interval;
    private final AtomicInteger time;
    private final String id;
    private Direction direction;

    public TickedObject() {
        this(UUID.randomUUID().toString(), 1000);
    }

    public TickedObject(String id) {
        this(id, 1000);
    }

    public TickedObject(String id, int interval) {
        this.id = id;
        this.interval = new AtomicLong(interval);
        this.time = new AtomicInteger(0);
        this.direction = Direction.INCREMENT;
        DeathSwap.instance.getTicker().register(this);
    }

    public void unregister() {
        DeathSwap.instance.getTicker().unregister(getId());
    }

    @Override
    public void tick() {
        if (isIncrement()) {
            time.incrementAndGet();
        } else {
            time.decrementAndGet();
        }
        this.onTick();
    }

    public abstract void onTick();

    @Override
    public long getInterval() {
        return interval.get();
    }

    public void setInterval(long ms) {
        interval.set(ms);
    }

    private boolean isIncrement() {
        return direction.equals(Direction.INCREMENT);
    }

    public enum Direction {
        DECREMENT, INCREMENT
    }

}
