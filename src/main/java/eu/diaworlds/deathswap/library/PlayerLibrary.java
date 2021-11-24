package eu.diaworlds.deathswap.library;

import eu.diaworlds.deathswap.Config;
import eu.diaworlds.deathswap.DeathSwap;
import eu.diaworlds.deathswap.player.DeadPlayer;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class PlayerLibrary implements Listener {

    private final Map<Player, DeadPlayer> playerMap = new ConcurrentHashMap<>();

    public PlayerLibrary() {
        DeathSwap.instance.registerListener(this);
    }

    public void destroy() {
        for (DeadPlayer player : playerMap.values()) {
            player.onQuit();
        }
    }

    public DeadPlayer get(Player player) {
        return playerMap.get(player);
    }

    public void join(Player player) {
        if (!playerMap.containsKey(player)) {
            DeadPlayer deadPlayer = new DeadPlayer(player);
            playerMap.put(player, deadPlayer);
            deadPlayer.onJoin();
        }
    }

    public void quit(Player player) {
        if (playerMap.containsKey(player)) {
            playerMap.get(player).onQuit();
            playerMap.remove(player);
        }
    }

    @EventHandler
    public void onJoin(PlayerJoinEvent e) {
        if (!DeathSwap.instance.isReady()) {
            DeathSwap.instance.kick(e.getPlayer(), Config.parse(Config.NO_ARENA_KICK));
            return;
        }
        e.setJoinMessage(null);
        join(e.getPlayer());
    }

    @EventHandler
    public void onQuit(PlayerQuitEvent e) {
        e.setQuitMessage(null);
        quit(e.getPlayer());
    }

}
