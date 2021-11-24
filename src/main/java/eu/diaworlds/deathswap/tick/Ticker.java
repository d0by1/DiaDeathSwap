package eu.diaworlds.deathswap.tick;

import eu.diaworlds.deathswap.executor.BurstExecutor;
import eu.diaworlds.deathswap.executor.MultiBurst;
import eu.diaworlds.deathswap.utils.S;
import eu.diaworlds.deathswap.utils.collection.DList;

import java.util.concurrent.atomic.AtomicInteger;

public class Ticker {

    private final DList<Ticked> ticklist;
    private final DList<Ticked> newTicks;
    private final DList<String> removeTicks;
    private volatile boolean ticking;

    public Ticker() {
        this.ticklist = new DList<>(2048);
        this.newTicks = new DList<>(128);
        this.removeTicks = new DList<>(128);
        this.ticking = false;
        S.ar(() -> {
            if (!ticking) {
                tick();
            }
        }, 0);
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
        AtomicInteger tc = new AtomicInteger(0);
        BurstExecutor e = MultiBurst.burst.burst(ticklist.size());
        for(int i = 0; i < ticklist.size(); i++) {
            int ii = i;
            e.queue(() -> {
                Ticked t = ticklist.get(ii);

                if(t.shouldTick()) {
                    tc.incrementAndGet();
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
                tc.incrementAndGet();
                ticklist.add(newTicks.popRandom());
            }
        }

        synchronized(removeTicks) {
            while(removeTicks.hasElements()) {
                tc.incrementAndGet();
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
        tc.get();
    }

}
