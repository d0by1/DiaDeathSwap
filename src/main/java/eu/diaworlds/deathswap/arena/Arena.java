package eu.diaworlds.deathswap.arena;

import eu.diaworlds.deathswap.Config;
import eu.diaworlds.deathswap.DeathSwap;
import eu.diaworlds.deathswap.utils.scoreboard.Board;
import eu.diaworlds.deathswap.tick.TickedObject;
import eu.diaworlds.deathswap.utils.Common;
import eu.diaworlds.deathswap.utils.Players;
import eu.diaworlds.deathswap.utils.S;
import eu.diaworlds.deathswap.utils.collection.DList;
import lombok.Getter;
import lombok.Setter;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.event.Listener;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

@Getter
@Setter
public class Arena extends TickedObject implements Listener {

    private final String id;
    private final DList<Player> players;
    private final AtomicInteger time;
    private final ArenaWorld arenaWorld;
    private Location center;
    private Phase phase;
    private Player winner;
    private volatile boolean shouldTick = false;

    public Arena(String id, ArenaWorld arenaWorld, Location center) {
        this.id = id;
        this.arenaWorld = arenaWorld;
        this.center = center;
        this.winner = null;
        this.phase = Phase.WAITING;
        this.players = new DList<>(2);
        this.time = new AtomicInteger(0);

        DeathSwap.instance.registerListener(this);
    }

    /**
     * Add given player to this arena. (This won't happen if the arena is already full or in game.)<br>
     *
     * (1) Check if it's possible to join.<br>
     * (2) Scoreboard is shown.<br>
     * (3) Players spawn location is set.<br>
     * (4) Player is healed.<br>
     * (5) Visibility is updated.<br>
     * (6) Join is handled. (Switch phase, messages, ...)<br>
     *
     * @param player the player.
     * @return boolean whether the join was successful. (false - Arena is full or in game.)
     */
    public boolean join(Player player) {
        if (players.size() >= 2 || !isWaiting()) {
            return false;
        }
        DeathSwap.instance.getPlayerLibrary().get(player).setArena(this);
        Board.create(player);
        players.add(player);
        player.setBedSpawnLocation(getCenter(), true);
        Players.heal(player);
        updateVisibility();
        bc(String.format(Config.PLAYER_JOIN, player.getName(), players.size()));
        if (players.size() == 2) {
            setPhase(Phase.STARTING);
        }
        return true;
    }

    /**
     * Remove given player from this arena.<br>
     *
     * (1) Scoreboard is hidden.<br>
     * (2) Arena in his profile is set to null.<br>
     * (3) Player is removed from this arena.<br>
     * (4) Quit is handled. (Switch phase, messages, ...)<br>
     *
     * @param player the player.
     */
    public void quit(Player player) {
        Board.remove(player);
        DeathSwap.instance.getPlayerLibrary().get(player).setArena(null);
        players.remove(player);
        bc(String.format(Config.PLAYER_QUIT, player.getName(), players.size()));
        if (isInGame()) {
            setPhase(Phase.ENDING);
        } else if (phase.equals(Phase.RESETTING)) {
            DeathSwap.instance.kick(player, Config.parse(Config.GAME_ENDED_KICK));
        }
    }

    /**
     * Just reset with a different name.
     */
    public void destroy() {
        reset();
    }

    /**
     * Reset this arena. Resets its chunks, winner, phase
     * and time. Also quits all players in this arena.
     */
    public void reset() {
        shouldTick = false;
        winner = null;
        phase = Phase.WAITING;
        time.set(0);
        for (Player player : players) {
            quit(player);
        }
        arenaWorld.regenChunks();
    }

    @Override
    public void onTick() {
        if (!shouldTick) return;
        int t;
        switch (phase) {
            case WAITING: return;
            case STARTING:
                t = time.decrementAndGet();
                if (t == 0) {
                    setPhase(Phase.IN_GAME);
                } else if (shouldAnnounce(t)) {
                    bc(String.format(Config.START_ANNOUNCE, t));
                }
                break;
            case IN_GAME:
                t = time.incrementAndGet();
                int gameTime = Config.GAME_TIME;
                if (t == gameTime) {
                    setPhase(Phase.ENDING);
                } else if (shouldAnnounce(gameTime - t)) {
                    bc(String.format(Config.END_ANNOUNCE, gameTime - t));
                } else {
                    int swapTime = Config.SWAP_TIME;
                    t = t % swapTime;
                    if (t == swapTime) {
                        S.r(this::swapPlayers);
                    } else if (shouldAnnounce(swapTime - t)) {
                        bc(String.format(Config.SWAP_ANNOUNCE, swapTime - t));
                    }
                }
                break;
            case ENDING:
                t = time.decrementAndGet();
                if (t == 0) {
                    setPhase(Phase.RESETTING);
                } else if (shouldAnnounce(t)) {
                    bc(String.format(Config.STOP_ANNOUNCE, t));
                }
                break;
            case RESETTING:
                players.forEach(this::quit);
        }
        players.forEach(Board::update);
    }

    public void setPhase(Phase phase) {
        switch (phase) {
            case WAITING:
                if (this.phase.ordinal() >= phase.ordinal() + 1) return;
                this.phase = phase;
                this.shouldTick = false;
                return;
            case STARTING:
                if (this.phase.ordinal() >= phase.ordinal()) return;
                this.phase = phase;
                this.time.set(10);
                this.shouldTick = true;
                return;
            case IN_GAME:
                if (this.phase.ordinal() >= phase.ordinal()) return;
                this.phase = phase;
                this.time.set(0);
                this.shouldTick = true;
                return;
            case ENDING:
                if (this.phase.ordinal() >= phase.ordinal()) return;
                this.phase = phase;
                this.time.set(10);
                this.shouldTick = true;
                players.forEach(Players::heal);
                if (winner != null) {
                    for (String line : Config.END_MESSAGE_WINNER) {
                        bc(Config.parse(line.replace("{winner}", winner.getName())));
                    }
                } else {
                    for (String line : Config.END_MESSAGE_TIME) {
                        bc(Config.parse(line));
                    }
                }
            case RESETTING:
                this.reset();
                break;
        }
    }

    public boolean isInGame() {
        return phase.equals(Phase.IN_GAME);
    }

    public boolean isWaiting() {
        return phase.equals(Phase.WAITING);
    }

    /*
     *  Utility Methods
     */

    public void bc(String message) {
        players.forEach(player -> Common.tell(player, message));
    }

    private boolean shouldAnnounce(int t) {
        return t <= 5 || t == 10 || t == 30 || t == 60;
    }

    private void swapPlayers() {
        List<Player> players = new ArrayList<>(Bukkit.getOnlinePlayers());
        Player player1 = players.get(0);
        Player player2 = players.get(1);
        Location loc = player1.getLocation();
        player1.teleport(player2.getLocation());
        player2.teleport(loc);
    }

    private void updateVisibility() {
        List<Player> players = new ArrayList<>(Bukkit.getOnlinePlayers());
        Player player1 = players.get(0);
        Player player2 = players.get(1);
        player1.showPlayer(DeathSwap.instance, player2);
        player2.showPlayer(DeathSwap.instance, player1);
    }

}
