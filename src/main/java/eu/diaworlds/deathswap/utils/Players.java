package eu.diaworlds.deathswap.utils;

import eu.diaworlds.deathswap.DeathSwap;
import lombok.experimental.UtilityClass;
import org.bukkit.Bukkit;
import org.bukkit.GameMode;
import org.bukkit.entity.Player;

@UtilityClass
public class Players {

    /**
     * Hide this player to all other players
     * and hide all other players to this player.
     *
     * @param player The player.
     */
    public static void hide(Player player) {
        if (Bukkit.getOnlinePlayers().size() < 2) return;
        for (Player onlinePlayer : Bukkit.getOnlinePlayers()) {
            player.hidePlayer(DeathSwap.instance, onlinePlayer);
            onlinePlayer.hidePlayer(DeathSwap.instance, player);
        }
    }

    /**
     * Show this player to all other players
     * and show all other players to this player.
     *
     * @param player The player.
     */
    public static void show(Player player) {
        if (Bukkit.getOnlinePlayers().size() < 2) return;
        for (Player onlinePlayer : Bukkit.getOnlinePlayers()) {
            player.showPlayer(DeathSwap.instance, onlinePlayer);
            onlinePlayer.showPlayer(DeathSwap.instance, player);
        }
    }

    /**
     * Simple method for resetting some player stats.
     * Resets players potion effects, inventory, fire ticks,
     * health, food level and game mode (to survival).
     *
     * @param player The player.
     */
    public static void clear(Player player) {
        player.getActivePotionEffects().clear();
        player.getInventory().clear();
        player.setFireTicks(0);
        player.setHealth(20d);
        player.setFoodLevel(20);
        player.setGameMode(GameMode.SURVIVAL);
    }

}
