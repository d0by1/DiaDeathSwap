package eu.diaworlds.deathswap.tick;

import eu.diaworlds.deathswap.utils.S;

public interface Ticked {

    String getId();

    void tick();

    long getLastTick();

    long getInterval();

    default boolean shouldTick() {
        return S.ms() - getLastTick() > getInterval();
    }

}
