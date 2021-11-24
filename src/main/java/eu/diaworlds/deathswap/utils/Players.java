package eu.diaworlds.deathswap.utils;

import eu.diaworlds.deathswap.DeathSwap;
import lombok.experimental.UtilityClass;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

@UtilityClass
public class Players {

    public static void hide(Player player) {
        if (Bukkit.getOnlinePlayers().size() < 2) return;
        for (Player onlinePlayer : Bukkit.getOnlinePlayers()) {
            player.hidePlayer(DeathSwap.instance, onlinePlayer);
            onlinePlayer.hidePlayer(DeathSwap.instance, player);
        }
    }

    public static void show(Player player) {
        if (Bukkit.getOnlinePlayers().size() < 2) return;
        for (Player onlinePlayer : Bukkit.getOnlinePlayers()) {
            player.showPlayer(DeathSwap.instance, onlinePlayer);
            onlinePlayer.showPlayer(DeathSwap.instance, player);
        }
    }

    public static void heal(Player player) {
        player.getActivePotionEffects().clear();
        player.setFireTicks(0);
        player.setHealth(20d);
        player.setFoodLevel(20);
    }

}
