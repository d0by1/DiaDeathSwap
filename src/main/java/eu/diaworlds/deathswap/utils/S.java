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

    @SuppressWarnings("deprecation")
    public static int ar(Runnable runnable, int interval) {
        return Bukkit.getScheduler().scheduleAsyncRepeatingTask(DeathSwap.instance, runnable, 0, interval);
    }

}
