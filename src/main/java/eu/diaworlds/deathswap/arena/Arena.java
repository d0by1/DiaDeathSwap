package eu.diaworlds.deathswap.arena;

import eu.diaworlds.deathswap.Config;
import eu.diaworlds.deathswap.DeathSwap;
import eu.diaworlds.deathswap.intent.PlayerList;
import eu.diaworlds.deathswap.utils.Common;
import eu.diaworlds.deathswap.utils.Players;
import eu.diaworlds.deathswap.utils.S;
import eu.diaworlds.deathswap.utils.collection.DList;
import eu.diaworlds.deathswap.utils.scoreboard.Board;
import lombok.Getter;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Sound;
import org.bukkit.entity.Player;

import java.util.UUID;
import java.util.stream.Collectors;

@Getter
public class Arena implements PlayerList {

    private final String id;
    private final DList<UUID> players;
    private ArenaRegion region;
    private ArenaListener listener;
    private ArenaState state;

    /**
     * Create new instance of arena.
     *
     * @param id Arenas id.
     * @param region Arenas region.
     */
    public Arena(String id, ArenaRegion region) {
        this.id = id;
        this.region = region;
        this.players = new DList<>();
        this.listener = new ArenaListener(this);
        this.state = new ArenaState(this);
    }

    /*
     *  General Methods
     */

    /**
     * Get the current game time in seconds.
     *
     * @return the game time.
     */
    public int getTime() {
        return state.getTime();
    }

    /**
     * Get the time until next swap in seconds.
     *
     * @return the time.
     */
    public int getSwapTime() {
        return state.getSwapTime();
    }

    /**
     * Teleport given player to the center of this arena.
     *
     * @param player the player.
     */
    public void center(Player player) {
        DeathSwap.instance.teleport(player, region.getCenter());
    }

    /**
     * Get the other player in this arena.
     *
     * @param player the first player.
     * @return the other player.
     */
    public Player getOtherPlayer(Player player) {
        if (players.size() < 1) return null;
        for (Player player1 : getPlayers()) {
            if (player.getUniqueId().equals(player1.getUniqueId())) continue;
            return player1;
        }
        return null;
    }

    /**
     * Clear players, reset state and stop ticking.
     */
    public void destroy() {
        clearPlayers();
        listener.unregister();
        state.reset();
        state.unregister();

        listener = null;
        region = null;
        state = null;
    }

    /**
     * Remove this arena from the ArenaController and destroy it.
     */
    public void remove() {
        DeathSwap.instance.getArenaController().unregisterArena(getId());
    }

    /*
     *  Player List Methods
     */

    public DList<Player> getPlayers() {
        return new DList<>(players.stream().map(Bukkit::getPlayer).collect(Collectors.toList()));
    }

    @Override
    public DList<UUID> getPlayerList() {
        return players;
    }

    /**
     * Clear the player list, kicking all the joined players from this arena.
     */
    @Override
    public void clearPlayers() {
        // Make them quit first
        for (Player player : getPlayers()) {
            onQuit(player);
            // Set player profiles arena to null.
            DeathSwap.instance.getPlayerController().get(player.getUniqueId()).setArena(null);
        }
        // Then clear the list
        players.clear();
    }

    /**
     * Join given player to this arena.
     *
     * @param player The player.
     * @return Boolean whether the join was successful.
     */
    @Override
    public boolean onJoin(Player player) {
        synchronized (players) {
            if (player == null || !isAllowed(player.getUniqueId()) || isJoined(player.getUniqueId())) {
                return false;
            }
            // Add them to the list
            players.add(player.getUniqueId());
            // Heal them
            Players.clear(player);
            // Make them see each other
            if (players.size() > 1) {
                S.sync(this::updatePlayerVisibility);
            }
            // Update scoreboard
            Board.create(player);
            this.getPlayers().forEach(Board::update);
            // Send messages
            this.bc(String.format(Config.parse(Config.ARENA_PLAYER_JOIN), player.getName(), players.size()));
            // Handle join
            if (players.size() == 2) {
                state.setPhase(ArenaPhase.STARTING);
            }
            return true;
        }
    }

    @Override
    public boolean onQuit(Player player) {
        synchronized (players) {
            if (player == null || !isJoined(player.getUniqueId())) {
                return false;
            }
            // Remove them from the list
            players.remove(player.getUniqueId());
            // Heal them and Hide them
            Players.clear(player);
            Players.hide(player);
            // Remove scoreboard
            Board.remove(player);
            // Update scoreboard for the remaining players
            this.getPlayers().forEach(Board::update);
            // Send messages
            bc(String.format(Config.parse(Config.ARENA_PLAYER_QUIT), player.getName(), players.size()));
            // Handle quit
            if (state.isInGame()) {
                // Make the other player win (If there is one)
                if (players.size() >= 1) {
                    state.setWinner(getOtherPlayer(player).getUniqueId());
                }
                state.setPhase(ArenaPhase.ENDING);
            } else if (state.getPhase().equals(ArenaPhase.STARTING)) {
                // Delay start
                state.setPhase(ArenaPhase.WAITING);
            }
            return true;
        }
    }

    @Override
    public boolean isJoined(UUID player) {
        return players.contains(player);
    }

    @Override
    public boolean isAllowed(UUID player) {
        return players.size() < 2 && state.isWaiting();
    }

    /*
     *  Utility Methods
     */

    /**
     * Broadcast a message only to players in this arena.
     *
     * @param message the message.
     */
    public void bc(String message) {
        getPlayers().forEach(player -> Common.tell(player, message));
    }

    /**
     * Broadcast a message and a sound only to players in this arena.
     *
     * @param message the message.
     * @param sound the sound.
     */
    public void bc(String message, Sound sound) {
        getPlayers().forEach(p -> {
            Common.tell(p, message);
            p.playSound(p.getLocation(), sound, 1f, 1f);
        });
    }

    /**
     * Perform location swap.
     */
    public void swapPlayers() {
        DList<Player> players = getPlayers();
        if (players.size() < 2) return;
        Player player1 = players.get(0);
        Player player2 = players.get(1);
        Location loc = player1.getLocation();
        player1.teleport(player2.getLocation());
        player2.teleport(loc);
        bc(Config.parse(Config.GAME_SWAPPED), Sound.ENTITY_PLAYER_LEVELUP);
    }

    /**
     * Make the players in this arena see each other.
     */
    public void updatePlayerVisibility() {
        DList<Player> players = getPlayers();
        if (players.size() < 2) return;
        Player player1 = players.get(0);
        Player player2 = players.get(1);
        player1.showPlayer(DeathSwap.instance, player2);
        player2.showPlayer(DeathSwap.instance, player1);
    }

}
