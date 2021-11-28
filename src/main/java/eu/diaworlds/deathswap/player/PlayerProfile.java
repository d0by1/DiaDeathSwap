package eu.diaworlds.deathswap.player;

import eu.diaworlds.deathswap.Config;
import eu.diaworlds.deathswap.DeathSwap;
import eu.diaworlds.deathswap.arena.Arena;
import eu.diaworlds.deathswap.utils.Players;
import lombok.Getter;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

import java.util.Optional;
import java.util.UUID;

@Getter
public class PlayerProfile {

    private final UUID uuid;
    private Arena arena;

    public PlayerProfile(Player parent) {
        this.uuid = parent.getUniqueId();
    }

    public Player getPlayer() {
        return Bukkit.getPlayer(uuid);
    }

    /**
     * Handle player join.<br>
     *
     * (1) Send join info.<br>
     * (2) Teleport to spawn.<br>
     * (3) Hide other players.<br>
     * (4) Attempt to join arena.<br>
     */
    public void onJoin() {
        Player player = getPlayer();
        if (arena != null || player == null) return;
        for (String line : Config.JOIN_INFO) {
            player.sendMessage(Config.parse(line));
        }
        player.teleport(DeathSwap.instance.getSpawn());
        Players.hide(player);
        attemptIdealArenaJoin();
    }

    /**
     * Handle player quit.<br>
     *
     * (1) Quit the current arena. (If any)<br>
     * (2) Show other players. (Just to reset)<br>
     */
    public void onQuit() {
        Player player = getPlayer();
        if (arena != null) {
            arena.onQuit(player);
            arena = null;
        }
        Players.show(player);
    }

    /**
     * Attempt to join ideal arena.
     */
    public void attemptIdealArenaJoin() {
        Optional<Arena> optionalArena = DeathSwap.instance.getArenaController().getIdealArena();
        optionalArena.ifPresent(this::attemptArenaJoin);
    }

    /**
     * Attempt to join an arena.
     */
    public void attemptArenaJoin(Arena arena) {
        Player player = getPlayer();
        if (this.arena != null || player == null) {
            return;
        }

        if (!arena.onJoin(player)) {
            DeathSwap.instance.kick(player, Config.parse(Config.ARENA_NO_ARENA_KICK));
            return;
        }
        this.setArena(arena);
    }

    /**
     * Set current arena. If the new arena is null, player is joined to a new arena.
     *
     * @param arena the arena.
     */
    public void setArena(Arena arena) {
        this.arena = arena;

        // Join a new arena
        Player player = getPlayer();
        if (this.arena == null && player != null) {
            attemptIdealArenaJoin();
            DeathSwap.instance.kick(player, Config.parse(Config.ARENA_STOP_KICK));
        }
    }

}
