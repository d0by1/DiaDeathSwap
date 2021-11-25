package eu.diaworlds.deathswap.player;

import eu.diaworlds.deathswap.Config;
import eu.diaworlds.deathswap.DeathSwap;
import eu.diaworlds.deathswap.arena.Arena;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.FoodLevelChangeEvent;
import org.bukkit.event.player.PlayerDropItemEvent;
import org.bukkit.event.player.PlayerItemDamageEvent;
import org.bukkit.event.player.PlayerKickEvent;
import org.bukkit.event.player.PlayerTeleportEvent;

import java.util.Optional;

public class PlayerListener implements Listener {

    private boolean isNotInGame(Player player) {
        Optional<Arena> arena = DeathSwap.instance.getArenaLibrary().getArena(player);
        return !arena.map(Arena::isInGame).orElse(false);
    }

    @EventHandler
    public void onKick(PlayerKickEvent e) {
        e.setCancelled(true);
        DeathSwap.instance.kick(e.getPlayer(), Config.parse(Config.SERVER_RESTART_KICK));
    }

    @EventHandler
    public void onDamage(EntityDamageEvent e) {
        if (e.getEntity() instanceof Player && isNotInGame((Player) e.getEntity())) {
            e.setCancelled(true);
        }
    }

    @EventHandler
    public void onPvp(EntityDamageByEntityEvent e) {
        if (e.getDamager() instanceof Player && e.getEntity() instanceof Player) {
            e.setCancelled(true);
        }
    }

    @EventHandler
    public void on(BlockBreakEvent e) {
        if (isNotInGame(e.getPlayer()) || !e.getPlayer().getWorld().getName().equals("world")) e.setCancelled(true);
    }

    @EventHandler
    public void on(BlockPlaceEvent e) {
        if (isNotInGame(e.getPlayer()) || !e.getPlayer().getWorld().getName().equals("world")) e.setCancelled(true);
    }

    @EventHandler
    public void on(FoodLevelChangeEvent e) {
        if (isNotInGame((Player) e.getEntity())) e.setCancelled(true);
    }

    @EventHandler
    public void on(PlayerItemDamageEvent e) {
        if (isNotInGame(e.getPlayer())) e.setCancelled(true);
    }

    @EventHandler
    public void on(PlayerDropItemEvent e) {
        if (isNotInGame(e.getPlayer())) e.setCancelled(true);
    }

    @EventHandler
    public void on(PlayerTeleportEvent e) {
        if (e.getCause().equals(PlayerTeleportEvent.TeleportCause.PLUGIN) || e.getCause().equals(PlayerTeleportEvent.TeleportCause.COMMAND)) {
            return;
        }
        String toWorld = e.getTo().getWorld().getName();
        String fromWorld = e.getFrom().getWorld().getName();
        if (!toWorld.equals(fromWorld)) {
            e.setCancelled(true);
        }
    }

}
