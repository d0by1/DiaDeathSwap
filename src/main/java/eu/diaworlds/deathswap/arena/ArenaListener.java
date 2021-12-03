package eu.diaworlds.deathswap.arena;

import eu.diaworlds.deathswap.Config;
import eu.diaworlds.deathswap.DeathSwap;
import eu.diaworlds.deathswap.utils.Common;
import eu.diaworlds.deathswap.utils.LP;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.HandlerList;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.player.AsyncPlayerChatEvent;
import org.bukkit.event.player.PlayerMoveEvent;

public class ArenaListener implements Listener {

    private final Arena parent;

    public ArenaListener(Arena parent) {
        this.parent = parent;
        DeathSwap.instance.registerListener(this);
    }

    public void unregister() {
        HandlerList.unregisterAll(this);
    }

    /*
     *  Event Handlers
     */

    @EventHandler
    public void onMove(PlayerMoveEvent e) {
        Player player = e.getPlayer();
        if (parent.getPlayerList().contains(player.getUniqueId()) && parent.getState().isInGame()) {
            Location to = e.getTo();
            if (to != null && !parent.getRegion().isInside(to.getBlockX(), to.getBlockZ())) {
                e.setTo(e.getFrom());
            }
        }
    }

    @EventHandler
    public void onDeath(PlayerDeathEvent e) {
        Player player = e.getEntity();
        if (parent.getPlayerList().contains(player.getUniqueId()) && parent.getState().isInGame()) {
            parent.bc(String.format(Config.parse(Config.GAME_PLAYER_DEATH), player.getName()));
            parent.getState().setWinner(parent.getOtherPlayer(player).getUniqueId());
            parent.getState().setPhase(ArenaPhase.ENDING);
            e.setDeathMessage(null);
        }
    }

    @EventHandler
    public void onChat(AsyncPlayerChatEvent e) {
        Player player = e.getPlayer();
        if (parent.getPlayerList().contains(player.getUniqueId())) {
            String message = Common.colorize(Config.CHAT_FORMAT
                    .replace("{name}", player.getName())
                    .replace("{prefix}", LP.getPrefix(player))
                    .replace("{suffix}", LP.getSuffix(player))
                    .replace("{username-color}", LP.getUsernameColor(player))
                    .replace("{message-color}", LP.getMessageColor(player))
                    .replace("{message}", e.getMessage())
            );
            parent.bc(message);
            e.setMessage("");
            e.setFormat("");
            e.setCancelled(true);
        }
    }

}
