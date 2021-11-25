package eu.diaworlds.deathswap.tick;

public interface Ticked {

    String getId();

    void tick();

    long getInterval();

    default boolean shouldTick(long ticks) {
        return ticks % getInterval() == 0;
    }

}
