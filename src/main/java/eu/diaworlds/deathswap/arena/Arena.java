package eu.diaworlds.deathswap.arena;

import eu.diaworlds.deathswap.Config;
import eu.diaworlds.deathswap.DeathSwap;
import eu.diaworlds.deathswap.tick.TickedObject;
import eu.diaworlds.deathswap.utils.Common;
import eu.diaworlds.deathswap.utils.LP;
import eu.diaworlds.deathswap.utils.Players;
import eu.diaworlds.deathswap.utils.S;
import eu.diaworlds.deathswap.utils.collection.DList;
import eu.diaworlds.deathswap.utils.scoreboard.Board;
import lombok.Getter;
import lombok.Setter;
import org.bukkit.Location;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.player.AsyncPlayerChatEvent;
import org.bukkit.event.player.PlayerMoveEvent;

@Getter
@Setter
public class Arena extends TickedObject implements Listener {

    private final String id;
    private final DList<Player> players;
    private final Area area;
    private Location center;
    private Phase phase;
    private Player winner;
    private volatile boolean shouldTick = false;

    public Arena(String id, Area area, Location center) {
        this.id = id;
        this.area = area;
        this.center = center;
        this.winner = null;
        this.phase = Phase.WAITING;
        this.players = new DList<>(2);
        setInterval(20);

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
        players.add(player);
        Board.create(player);
        players.forEach(Board::update);
        player.setBedSpawnLocation(getCenter(), true);
        Players.heal(player);
        updateVisibility();
        bc(String.format(Config.parse(Config.PLAYER_JOIN), player.getName(), players.size()));
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
        setWinner(getOtherPlayer(player));
        Board.remove(player);
        DeathSwap.instance.getPlayerLibrary().get(player).setArena(null);
        players.remove(player);
        bc(String.format(Config.parse(Config.PLAYER_QUIT), player.getName(), players.size()));
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
        unregister(); // Unregister from ticker
        DeathSwap.instance.getArenaLibrary().removeArena(this);
    }

    /**
     * Reset this arena. Resets its winner, phase
     * and time. Also quits all players in this arena.
     */
    public void reset() {
        shouldTick = false;
        winner = null;
        phase = Phase.WAITING;
        getTime().set(0);
        for (Player player : new DList<>(players)) {
            quit(player);
        }
    }

    @Override
    public boolean shouldTick(long ticks) {
        return super.shouldTick(ticks) && shouldTick;
    }

    @Override
    public void onTick() {
        int t;
        switch (phase) {
            case WAITING: return;
            case STARTING:
                t = getTime().get();
                if (t == 0) {
                    setPhase(Phase.IN_GAME);
                } else if (shouldAnnounce(t)) {
                    announce(String.format(Config.parse(Config.START_ANNOUNCE), t));
                }
                break;
            case IN_GAME:
                t = getTime().get();
                int gameTime = Config.GAME_TIME * 20;
                if (t == gameTime) {
                    setPhase(Phase.ENDING);
                } else if (shouldAnnounce(gameTime - t)) {
                    announce(String.format(Config.parse(Config.END_ANNOUNCE), gameTime - t));
                } else {
                    int swapTime = Config.SWAP_TIME * 20;
                    t = t % swapTime;
                    if (t == swapTime) {
                        S.r(this::swapPlayers);
                    } else if (shouldAnnounce(swapTime - t)) {
                        announce(String.format(Config.parse(Config.SWAP_ANNOUNCE), swapTime - t));
                    }
                }
                break;
            case ENDING:
                t = getTime().get();
                if (t == 0) {
                    setPhase(Phase.RESETTING);
                } else if (shouldAnnounce(t)) {
                    bc(String.format(Config.parse(Config.STOP_ANNOUNCE), t));
                }
                break;
        }
        players.forEach(Board::update);
    }

    public void setPhase(Phase phase) {
        switch (phase) {
            case WAITING:
                if (this.phase.ordinal() >= phase.ordinal() + 1) return;
                this.phase = phase;
                shouldTick = false;
                return;
            case STARTING:
                if (this.phase.ordinal() >= phase.ordinal()) return;
                this.phase = phase;
                setDirection(Direction.DECREMENT);
                getTime().set(10);
                shouldTick = true;
                return;
            case IN_GAME:
                if (this.phase.ordinal() >= phase.ordinal()) return;
                this.phase = phase;
                setDirection(Direction.INCREMENT);
                getTime().set(0);
                players.forEach(this::center);
                shouldTick = true;
                return;
            case ENDING:
                if (this.phase.ordinal() >= phase.ordinal()) return;
                this.phase = phase;
                setDirection(Direction.DECREMENT);
                getTime().set(20);
                shouldTick = true;
                players.forEach((p) -> {
                    DeathSwap.instance.spawn(p);
                    Players.heal(p);
                    if (winner != null) {
                        for (String line : Config.END_MESSAGE_WINNER) {
                            Common.tell(p, Config.parse(line.replace("{winner}", winner.getName())));
                        }
                        if (winner.equals(p)) {
                            p.sendTitle(
                                    Config.parse(Config.END_WINNER_TITLE.replace("{winner}", winner.getName())),
                                    Config.parse(Config.END_WINNER_SUBTITLE.replace("{winner}", winner.getName())),
                                    20, 60, 20
                            );
                        } else {
                            p.sendTitle(
                                    Config.parse(Config.END_LOSER_TITLE.replace("{winner}", winner.getName())),
                                    Config.parse(Config.END_LOSER_SUBTITLE.replace("{winner}", winner.getName())),
                                    20, 60, 20
                            );
                        }
                    } else {
                        for (String line : Config.END_MESSAGE_TIME) {
                            Common.tell(p, Config.parse(line));
                        }
                        p.sendTitle(
                                Config.parse(Config.END_DRAW_TITLE),
                                Config.parse(Config.END_DRAW_SUBTITLE),
                                20, 60, 20
                        );
                    }
                });
            case RESETTING:
                this.destroy();
                break;
        }
    }

    public boolean isInGame() {
        return phase.equals(Phase.IN_GAME);
    }

    public boolean isWaiting() {
        return phase.equals(Phase.WAITING);
    }

    /**
     * Teleport given player to the center of this arena.
     *
     * @param player the player.
     */
    public void center(Player player) {
        DeathSwap.instance.teleport(player, getCenter());
    }

    /**
     * Get the other player in this arena.
     *
     * @param player the first player.
     * @return the other player.
     */
    public Player getOtherPlayer(Player player) {
        if (players.size() < 2 || !players.contains(player)) return null;
        for (Player player1 : players) {
            if (player.equals(player1)) continue;
            return player1;
        }
        return null;
    }

    /**
     * Get game duration in seconds.
     *
     * @return the duration.
     */
    public int getGameTime() {
        return getTime().get();
    }

    /**
     * Get time until next swap in seconds.
     *
     * @return the time.
     */
    public int getSwapTime() {
        int swapTime = Config.SWAP_TIME;
        int t = getGameTime() % swapTime;
        return swapTime - t;
    }

    /*
     *  Event Handlers
     */

    @EventHandler
    public void onMove(PlayerMoveEvent e) {
        Player player = e.getPlayer();
        if (!players.contains(player)) return;
        Location to = e.getTo();
        if (to != null && !area.isInside(to.getBlockX(), to.getBlockZ())) {
            e.setCancelled(true);
            e.setTo(e.getFrom());
        }
    }

    @EventHandler
    public void onDeath(PlayerDeathEvent e) {
        Player player = e.getEntity();
        if (!players.contains(player)) return;
        setWinner(getOtherPlayer(player));
        setPhase(Phase.ENDING);
    }

    @EventHandler
    public void onChat(AsyncPlayerChatEvent e) {
        Player player = e.getPlayer();
        if (!players.contains(player)) return;
        String message = Common.colorize(Config.CHAT_FORMAT
                .replace("{name}", player.getName())
                .replace("{prefix}", LP.getPrefix(player))
                .replace("{suffix}", LP.getSuffix(player))
                .replace("{username-color}", LP.getUsernameColor(player))
                .replace("{message-color}", LP.getMessageColor(player))
                .replace("{message}", e.getMessage())
        );
        bc(message);
        e.setMessage("");
        e.setFormat("");
        e.setCancelled(true);
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
        players.forEach(player -> Common.tell(player, message));
    }

    /**
     * Broadcast a message only to players in this arena.
     *
     * @param message the message.
     */
    public void announce(String message) {
        players.forEach(p -> {
            Common.tell(p, message);
            p.playSound(p.getLocation(), Sound.BLOCK_NOTE_BLOCK_PLING, 1f, 1f);
        });
    }

    private boolean shouldAnnounce(long t) {
        return t <= 5 || t == 10 || t == 20 || t == 30  || t == 60;
    }

    /**
     * Perform location swap.
     */
    private void swapPlayers() {
        if (players.size() < 2) return;
        Player player1 = players.get(0);
        Player player2 = players.get(1);
        Location loc = player1.getLocation();
        player1.teleport(player2.getLocation());
        player2.teleport(loc);
    }

    /**
     * Make the players in this arena see each other.
     */
    private void updateVisibility() {
        if (players.size() < 2) return;
        Player player1 = players.get(0);
        Player player2 = players.get(1);
        player1.showPlayer(DeathSwap.instance, player2);
        player2.showPlayer(DeathSwap.instance, player1);
    }

}
