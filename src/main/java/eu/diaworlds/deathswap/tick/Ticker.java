package eu.diaworlds.deathswap.tick;

import eu.diaworlds.deathswap.executor.BurstExecutor;
import eu.diaworlds.deathswap.executor.MultiBurst;
import eu.diaworlds.deathswap.utils.S;
import eu.diaworlds.deathswap.utils.collection.DList;

import java.util.concurrent.atomic.AtomicLong;

public class Ticker {

    private final DList<Ticked> ticklist;
    private final DList<Ticked> newTicks;
    private final DList<String> removeTicks;
    private final AtomicLong ticks;
    private volatile boolean ticking;

    public Ticker() {
        this.ticklist = new DList<>(512);
        this.newTicks = new DList<>(64);
        this.removeTicks = new DList<>(64);
        this.ticks = new AtomicLong(0);
        this.ticking = false;

        S.ar(() -> {
            if (!ticking) {
                tick();
            }
        }, 1);
    }

    public void register(Ticked ticked) {
        synchronized (newTicks) {
            newTicks.add(ticked);
        }
    }

    public void unregister(String id) {
        synchronized (removeTicks) {
            removeTicks.add(id);
        }
    }

    private void tick() {
        ticking = true;
        BurstExecutor e = MultiBurst.burst.burst(ticklist.size());
        for(int i = 0; i < ticklist.size(); i++) {
            int ii = i;
            e.queue(() -> {
                Ticked t = ticklist.get(ii);

                if(t.shouldTick(ticks.get())) {
                    try {
                        t.tick();
                    } catch(Throwable exxx) {
                        exxx.printStackTrace();
                    }
                }
            });
        }

        e.complete();

        synchronized(newTicks) {
            while(newTicks.hasElements()) {
                ticklist.add(newTicks.popRandom());
            }
        }

        synchronized(removeTicks) {
            while(removeTicks.hasElements()) {
                String id = removeTicks.popRandom();
                for (int i = 0; i < ticklist.size(); i++) {
                    if(ticklist.get(i).getId().equals(id)) {
                        ticklist.remove(i);
                        break;
                    }
                }
            }
        }

        ticking = false;
        ticks.incrementAndGet();
    }

}
