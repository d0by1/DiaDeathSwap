package eu.diaworlds.deathswap.utils.scoreboard;

import eu.diaworlds.deathswap.Config;
import eu.diaworlds.deathswap.DeathSwap;
import eu.diaworlds.deathswap.arena.Arena;
import eu.diaworlds.deathswap.player.DeadPlayer;
import eu.diaworlds.deathswap.utils.collection.DList;
import lombok.experimental.UtilityClass;
import org.bukkit.entity.Player;

@UtilityClass
public class Board {

    public static void create(Player player) {
        if (!APIScoreboard.hasScore(player)) {
            APIScoreboard scoreboard = APIScoreboard.createScore(player);
            scoreboard.setTitle(parse(player, Config.SCOREBOARD_TITLE));
            DList<String> lines = new DList<>(Config.SCOREBOARD_TEXT);
            lines.replaceAll(s -> parse(player, s));
            scoreboard.setSlotsFromList(lines);
        }
    }

    public static void remove(Player player) {
        if (APIScoreboard.hasScore(player)) {
            APIScoreboard.removeScore(player);
        }
    }

    public static void update(Player player) {
        if (APIScoreboard.hasScore(player)) {
            APIScoreboard scoreboard = APIScoreboard.getByPlayer(player);
            scoreboard.setTitle(parse(player, Config.SCOREBOARD_TITLE));
            DList<String> lines = new DList<>(Config.SCOREBOARD_TEXT);
            lines.replaceAll(s -> parse(player, s));
            scoreboard.setSlotsFromList(lines);
        }
    }

    private static String parse(Player player, String string) {
        DeadPlayer deadPlayer = DeathSwap.instance.getPlayerLibrary().get(player);
        Arena arena = deadPlayer.getArena();
        if (arena == null) return string;
        return string
                .replace("{game_time}", String.valueOf(arena.getGameTime()))
                .replace("{swap_time}", String.valueOf(arena.getSwapTime()))
                .replace("{arena_phase}", arena.getPhase().name())
                .replace("{arena_players}", String.valueOf(arena.getPlayers().size()))
                ;
    }

}
