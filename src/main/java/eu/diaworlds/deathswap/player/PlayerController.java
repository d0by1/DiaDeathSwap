package eu.diaworlds.deathswap.player;

import eu.diaworlds.deathswap.Config;
import eu.diaworlds.deathswap.DeathSwap;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.HandlerList;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

public class PlayerController implements Listener {

    private final Map<UUID, PlayerProfile> playerMap;

    /**
     * Default constructor.
     */
    public PlayerController() {
        this.playerMap = new ConcurrentHashMap<>();

        DeathSwap.instance.registerListener(this);
    }

    /**
     * Destroy PlayerManager and remove all profiles.
     */
    public void destroy() {
        HandlerList.unregisterAll(this);
        for (PlayerProfile player : playerMap.values()) {
            player.onQuit();
        }
        playerMap.clear();
    }

    /**
     * Get Profile of a player.
     *
     * @param uuid Players UUID.
     * @return Players profile.
     */
    public PlayerProfile get(UUID uuid) {
        return playerMap.get(uuid);
    }

    /**
     * Add a new player and create their profile.
     *
     * @param player The player.
     */
    public void join(Player player) {
        if (!playerMap.containsKey(player.getUniqueId())) {
            PlayerProfile playerProfile = new PlayerProfile(player);
            playerMap.put(player.getUniqueId(), playerProfile);
            playerProfile.onJoin();
        }
    }

    /**
     * Remove player and its profile.
     *
     * @param uuid Players UUID.
     */
    public void quit(UUID uuid) {
        if (playerMap.containsKey(uuid)) {
            playerMap.get(uuid).onQuit();
            playerMap.remove(uuid);
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
        quit(e.getPlayer().getUniqueId());
    }

}
