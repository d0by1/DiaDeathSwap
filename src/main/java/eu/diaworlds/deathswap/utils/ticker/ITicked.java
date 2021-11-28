package eu.diaworlds.deathswap.utils.ticker;

import eu.diaworlds.deathswap.DeathSwap;

public interface ITicked {

    String getId();

    int getTime();

    void setTime(int time);

    void tick();

    long getInterval();

    default boolean shouldTick(long tick) {
        return tick % getInterval() == 0;
    }

    default void register() {
        DeathSwap.instance.getTicker().register(this);
    }

    default void unregister() {
        DeathSwap.instance.getTicker().unregister(getId());
    }

}
