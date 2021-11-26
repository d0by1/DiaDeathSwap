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
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

public class PlayerLibrary implements Listener {

    private final Map<UUID, DeadPlayer> playerMap = new ConcurrentHashMap<>();

    public PlayerLibrary() {
        DeathSwap.instance.registerListener(this);
    }

    public void destroy() {
        for (DeadPlayer player : playerMap.values()) {
            player.onQuit();
        }
    }

    public DeadPlayer get(Player player) {
        return playerMap.get(player.getUniqueId());
    }

    public void join(Player player) {
        if (!playerMap.containsKey(player.getUniqueId())) {
            DeadPlayer deadPlayer = new DeadPlayer(player);
            playerMap.put(player.getUniqueId(), deadPlayer);
            deadPlayer.onJoin();
        }
    }

    public void quit(Player player) {
        if (playerMap.containsKey(player.getUniqueId())) {
            playerMap.get(player.getUniqueId()).onQuit();
            playerMap.remove(player.getUniqueId());
        }
    }

    @EventHandler
    public void onJoin(PlayerJoinEvent e) {
        if (!DeathSwap.instance.isReady()) {
            DeathSwap.instance.kick(e.getPlayer(), Config.parse(Config.ARENA_NO_ARENA_KICK));
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
