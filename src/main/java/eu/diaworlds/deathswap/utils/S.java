package eu.diaworlds.deathswap.utils;

import eu.diaworlds.deathswap.DeathSwap;
import lombok.experimental.UtilityClass;
import org.bukkit.Bukkit;

@UtilityClass
public class S {

    public static long ms() {
        return System.currentTimeMillis();
    }

    public static void r(Runnable runnable) {
        Bukkit.getScheduler().runTask(DeathSwap.instance, runnable);
    }

    public static void ar(Runnable runnable, long interval) {
        Bukkit.getScheduler().runTaskTimerAsynchronously(DeathSwap.instance, runnable, 0, interval);
    }

}
