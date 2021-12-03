package eu.diaworlds.deathswap.arena;

import eu.diaworlds.deathswap.Config;
import eu.diaworlds.deathswap.DeathSwap;
import eu.diaworlds.deathswap.utils.Common;
import eu.diaworlds.deathswap.utils.Players;
import eu.diaworlds.deathswap.utils.S;
import eu.diaworlds.deathswap.utils.scoreboard.Board;
import eu.diaworlds.deathswap.utils.ticker.Ticked;
import lombok.Getter;
import org.bukkit.Bukkit;
import org.bukkit.Sound;
import org.bukkit.entity.Player;

import java.util.UUID;

@Getter
public class ArenaState extends Ticked {

    private final Arena parent;
    private ArenaPhase phase;
    private UUID winner;
    private volatile boolean shouldTick;

    public ArenaState(Arena parent) {
        super(20L);
        this.parent = parent;
        this.reset();
    }

    /*
     *  General Methods
     */

    public void reset() {
        this.phase = ArenaPhase.WAITING;
        this.winner = null;
        this.shouldTick = false;
        this.setTime(0);
    }

    public void setWinner(UUID winner) {
        this.winner = winner;
    }

    public boolean isInGame() {
        return phase.equals(ArenaPhase.IN_GAME);
    }

    public boolean isWaiting() {
        return phase.equals(ArenaPhase.WAITING);
    }

    /**
     * Get time until next swap in seconds.
     *
     * @return the time.
     */
    public int getSwapTime() {
        int swapTime = Config.SWAP_TIME;
        int t = getTime() % swapTime;
        return swapTime - t;
    }

    public void setPhase(ArenaPhase phase) {
        switch (phase) {
            case WAITING:
                if (this.phase.ordinal() >= phase.ordinal() + 1) return;
                this.phase = phase;
                shouldTick = false;
                parent.bc(Config.parse(Config.GAME_START_DELAYED));
                return;
            case STARTING:
                if (this.phase.ordinal() >= phase.ordinal()) return;
                this.phase = phase;
                setDirection(Direction.DECREMENT);
                setTime(11);
                shouldTick = true;
                return;
            case IN_GAME:
                if (this.phase.ordinal() >= phase.ordinal()) return;
                this.phase = phase;
                setDirection(Direction.INCREMENT);
                setTime(0);
                parent.getPlayers().forEach((p) -> {
                    parent.center(p);
                    Players.clear(p);
                });
                parent.bc(Config.parse(Config.GAME_STARTED), Sound.ENTITY_PLAYER_LEVELUP);
                shouldTick = true;
                return;
            case ENDING:
                if (this.phase.ordinal() >= phase.ordinal()) return;
                this.phase = phase;
                setTime(21);
                setDirection(Direction.DECREMENT);
                shouldTick = true;
                parent.getPlayers().forEach((p) -> {
                    // Teleport them to spawn and heal them
                    DeathSwap.instance.spawn(p);
                    Players.clear(p);
                    p.resetTitle();

                    Player winnerPlayer;
                    // End with a winner
                    if (winner != null && ((winnerPlayer = Bukkit.getPlayer(winner)) != null)) {
                        for (String line : Config.GAME_END_MESSAGE_WINNER) {
                            Common.tell(p, Config.parse(line.replace("{winner}", winnerPlayer.getName())));
                        }
                        // Send different title to the winner
                        if (winner.equals(p.getUniqueId())) {
                            p.sendTitle(
                                    Config.parse(Config.GAME_END_WINNER_TITLE.replace("{winner}", winnerPlayer.getName())),
                                    Config.parse(Config.GAME_END_WINNER_SUBTITLE.replace("{winner}", winnerPlayer.getName())),
                                    20, 60, 20
                            );
                        } else {
                            p.sendTitle(
                                    Config.parse(Config.GAME_END_LOSER_TITLE.replace("{winner}", winnerPlayer.getName())),
                                    Config.parse(Config.GAME_END_LOSER_SUBTITLE.replace("{winner}", winnerPlayer.getName())),
                                    20, 60, 20
                            );
                        }
                    // End without a winner (Time out)
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
            case STOPPING:
                this.shouldTick = false;
                parent.remove();
                break;
        }
    }

    /*
     *  Ticker Methods
     */

    @Override
    public boolean shouldTick(long tick) {
        return super.shouldTick(tick) && shouldTick;
    }

    @Override
    public void onTick() {
        int t;
        switch (phase) {
            case WAITING: return;
            case STARTING:
                t = getTime();
                if (t == 0) {
                    setPhase(ArenaPhase.IN_GAME);
                } else if (shouldAnnounce(t)) {
                    parent.bc(String.format(Config.parse(Config.GAME_START_ANNOUNCE), t), Sound.BLOCK_NOTE_BLOCK_PLING);
                }
                break;
            case IN_GAME:
                t = getTime();
                if (t == 0) {
                    setPhase(ArenaPhase.ENDING);
                } else if (shouldAnnounce(Config.GAME_TIME - t)) {
                    parent.bc(String.format(Config.parse(Config.GAME_END_ANNOUNCE), Config.GAME_TIME - t), Sound.BLOCK_NOTE_BLOCK_PLING);
                } else {
                    t = getSwapTime();
                    if (t == Config.SWAP_TIME) {
                        S.sync(parent::swapPlayers);
                    } else if (shouldAnnounce(t)) {
                        parent.bc(String.format(Config.parse(Config.GAME_SWAP_ANNOUNCE), t), Sound.BLOCK_NOTE_BLOCK_PLING);
                    }
                }
                break;
            case ENDING:
                t = getTime();
                if (t == 0) {
                    setPhase(ArenaPhase.STOPPING);
                } else if (shouldAnnounce(t)) {
                    parent.bc(String.format(Config.parse(Config.ARENA_AUTO_JOIN_ANNOUNCE), t));
                }
                break;
        }
        parent.getPlayers().forEach(Board::update);
    }

    /*
     *  Utility Methods
     */

    private boolean shouldAnnounce(int t) {
        return t <= 5 || t == 10 || t == 20 || t == 30  || t == 60;
    }

}
