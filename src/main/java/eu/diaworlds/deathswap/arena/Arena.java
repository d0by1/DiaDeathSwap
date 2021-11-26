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
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.player.AsyncPlayerChatEvent;
import org.bukkit.event.player.PlayerMoveEvent;

import java.util.UUID;
import java.util.stream.Collectors;

@Getter
@Setter
public class Arena extends TickedObject implements Listener {

    private final String id;
    private final DList<UUID> players;
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
     * (3) Player is healed.<br>
     * (4) Visibility is updated.<br>
     * (5) Join is handled. (Switch phase, messages, ...)<br>
     *
     * @param player the player.
     * @return boolean whether the join was successful. (false - Arena is full or in game.)
     */
    public boolean join(Player player) {
        if (players.size() >= 2 || !isWaiting()) {
            return false;
        }
        players.add(player.getUniqueId());
        DeathSwap.instance.getPlayerLibrary().get(player).setArena(this);
        Board.create(player);
        getPlayers().forEach(Board::update);
        Players.clear(player);
        updateVisibility();
        bc(String.format(Config.parse(Config.ARENA_PLAYER_JOIN), player.getName(), players.size()));
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
        setWinner(getOtherPlayer(player));
        players.remove(player.getUniqueId());
        DeathSwap.instance.getPlayerLibrary().get(player).setArena(null);
        getPlayers().forEach(Board::update);
        bc(String.format(Config.parse(Config.ARENA_PLAYER_QUIT), player.getName(), players.size()));

        if (isInGame()) {
            setPhase(Phase.ENDING);
        } else if (phase.equals(Phase.STARTING)) {
            setPhase(Phase.WAITING);
        }
    }

    /**
     * Destroy and remove from ArenaLibrary.
     */
    public void remove() {
        destroy();
        DeathSwap.instance.getArenaLibrary().removeArena(this);
    }

    /**
     * Reset and unregister from ticker.
     */
    public void destroy() {
        reset();
        unregister();
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
        for (Player player : getPlayers()) {
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
                t = getGameTime();
                if (t == 0) {
                    setPhase(Phase.IN_GAME);
                } else if (shouldAnnounce(t)) {
                    announce(String.format(Config.parse(Config.GAME_START_ANNOUNCE), t), Sound.BLOCK_NOTE_BLOCK_PLING);
                }
                break;
            case IN_GAME:
                t = getGameTime();
                if (t == 0) {
                    setPhase(Phase.ENDING);
                } else if (shouldAnnounce(Config.GAME_TIME - t)) {
                    announce(String.format(Config.parse(Config.GAME_END_ANNOUNCE), Config.GAME_TIME - t), Sound.BLOCK_NOTE_BLOCK_PLING);
                } else {
                    t = getSwapTime();
                    if (t == Config.SWAP_TIME) {
                        S.r(this::swapPlayers);
                    } else if (shouldAnnounce(t)) {
                        announce(String.format(Config.parse(Config.GAME_SWAP_ANNOUNCE), t), Sound.BLOCK_NOTE_BLOCK_PLING);
                    }
                }
                break;
            case ENDING:
                t = getGameTime();
                if (t == 0) {
                    setPhase(Phase.RESETTING);
                } else if (shouldAnnounce(t)) {
                    bc(String.format(Config.parse(Config.ARENA_STOP_ANNOUNCE), t));
                }
                break;
        }
        getPlayers().forEach(Board::update);
    }

    public void setPhase(Phase phase) {
        switch (phase) {
            case WAITING:
                if (this.phase.ordinal() >= phase.ordinal() + 1) return;
                this.phase = phase;
                shouldTick = false;
                bc(Config.parse(Config.GAME_START_DELAYED));
                return;
            case STARTING:
                if (this.phase.ordinal() >= phase.ordinal()) return;
                this.phase = phase;
                setDirection(Direction.DECREMENT);
                getTime().set(11);
                shouldTick = true;
                return;
            case IN_GAME:
                if (this.phase.ordinal() >= phase.ordinal()) return;
                this.phase = phase;
                setDirection(Direction.INCREMENT);
                getTime().set(0);
                getPlayers().forEach((p) -> {
                    center(p);
                    Players.clear(p);
                });
                announce(Config.parse(Config.GAME_STARTED), Sound.ENTITY_PLAYER_LEVELUP);
                shouldTick = true;
                return;
            case ENDING:
                if (this.phase.ordinal() >= phase.ordinal()) return;
                this.phase = phase;
                getTime().set(21);
                setDirection(Direction.DECREMENT);
                shouldTick = true;
                getPlayers().forEach((p) -> {
                    DeathSwap.instance.spawn(p);
                    Players.clear(p);
                    if (winner != null) {
                        for (String line : Config.GAME_END_MESSAGE_WINNER) {
                            Common.tell(p, Config.parse(line.replace("{winner}", winner.getName())));
                        }
                        if (winner.equals(p)) {
                            p.sendTitle(
                                    Config.parse(Config.GAME_END_WINNER_TITLE.replace("{winner}", winner.getName())),
                                    Config.parse(Config.GAME_END_WINNER_SUBTITLE.replace("{winner}", winner.getName())),
                                    20, 60, 20
                            );
                        } else {
                            p.sendTitle(
                                    Config.parse(Config.GAME_END_LOSER_TITLE.replace("{winner}", winner.getName())),
                                    Config.parse(Config.GAME_END_LOSER_SUBTITLE.replace("{winner}", winner.getName())),
                                    20, 60, 20
                            );
                        }
                    } else {
                        for (String line : Config.GAME_END_MESSAGE_TIME) {
                            Common.tell(p, Config.parse(line));
                        }
                        p.sendTitle(
                                Config.parse(Config.GAME_END_DRAW_TITLE),
                                Config.parse(Config.GAME_END_DRAW_SUBTITLE),
                                20, 60, 20
                        );
                    }
                });
                return;
            case RESETTING:
                this.shouldTick = false;
                this.remove();
                break;
        }
    }

    public boolean isInGame() {
        return phase.equals(Phase.IN_GAME);
    }

    public boolean isWaiting() {
        return phase.equals(Phase.WAITING);
    }

    public DList<Player> getPlayers() {
        return new DList<>(players.stream().map(Bukkit::getPlayer).collect(Collectors.toList()));
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
        if (players.size() < 2 || !players.contains(player.getUniqueId())) return null;
        for (Player player1 : getPlayers()) {
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
        if (!isInGame() || !players.contains(player.getUniqueId())) return;
        Location to = e.getTo();
        if (to != null && !area.isInside(to.getBlockX(), to.getBlockZ())) {
            e.setCancelled(true);
            e.setTo(e.getFrom());
        }
    }

    @EventHandler
    public void onDeath(PlayerDeathEvent e) {
        Player player = e.getEntity();
        if (!isInGame() || !players.contains(player.getUniqueId())) return;
        e.setDeathMessage(null);
        bc(String.format(Config.parse(Config.GAME_PLAYER_DEATH), player.getName()));
        setWinner(getOtherPlayer(player));
        setPhase(Phase.ENDING);
    }

    @EventHandler
    public void onChat(AsyncPlayerChatEvent e) {
        Player player = e.getPlayer();
        if (!players.contains(player.getUniqueId())) return;
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
        getPlayers().forEach(player -> Common.tell(player, message));
    }

    /**
     * Broadcast a message only to players in this arena.
     *
     * @param message the message.
     */
    public void announce(String message, Sound sound) {
        getPlayers().forEach(p -> {
            Common.tell(p, message);
            p.playSound(p.getLocation(), sound, 1f, 1f);
        });
    }

    private boolean shouldAnnounce(long t) {
        return t <= 5 || t == 10 || t == 20 || t == 30  || t == 60;
    }

    /**
     * Perform location swap.
     */
    private void swapPlayers() {
        DList<Player> players = getPlayers();
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
        DList<Player> players = getPlayers();
        if (players.size() < 2) return;
        Player player1 = players.get(0);
        Player player2 = players.get(1);
        player1.showPlayer(DeathSwap.instance, player2);
        player2.showPlayer(DeathSwap.instance, player1);
    }

}
