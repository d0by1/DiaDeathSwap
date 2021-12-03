package eu.diaworlds.deathswap.utils.scoreboard;

import eu.diaworlds.deathswap.Config;
import eu.diaworlds.deathswap.DeathSwap;
import eu.diaworlds.deathswap.arena.Arena;
import eu.diaworlds.deathswap.player.PlayerProfile;
import eu.diaworlds.deathswap.utils.Common;
import eu.diaworlds.deathswap.utils.S;
import eu.diaworlds.deathswap.utils.collection.DList;
import lombok.experimental.UtilityClass;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

import java.util.List;

@UtilityClass
public class Board {

    public static void create(Player player) {
        // Needs to run on the main thread
        if (!Bukkit.isPrimaryThread()) {
            S.sync(() -> create(player));
            return;
        }

        if (!APIScoreboard.hasScore(player)) {
            APIScoreboard scoreboard = APIScoreboard.createScore(player);
            scoreboard.setTitle(parse(player, Config.SCOREBOARD_WAITING_TITLE));
            DList<String> lines = new DList<>(Config.SCOREBOARD_WAITING_TEXT);
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
        PlayerProfile profile = DeathSwap.instance.getPlayerController().get(player.getUniqueId());
        Arena arena = profile.getArena();

        String title = "";
        List<String> lines = new DList<>();
        if (arena != null) {
            switch (arena.getState().getPhase()) {
                case WAITING:
                    title = parse(player, Config.SCOREBOARD_WAITING_TITLE);
                    lines = new DList<>(Config.SCOREBOARD_WAITING_TEXT);
                    break;
                case STARTING:
                    title = parse(player, Config.SCOREBOARD_STARTING_TITLE);
                    lines = new DList<>(Config.SCOREBOARD_STARTING_TEXT);
                    break;
                case IN_GAME:
                    title = parse(player, Config.SCOREBOARD_INGAME_TITLE);
                    lines = new DList<>(Config.SCOREBOARD_INGAME_TEXT);
                    break;
                case ENDING:
                    title = parse(player, Config.SCOREBOARD_ENDING_TITLE);
                    lines = new DList<>(Config.SCOREBOARD_ENDING_TEXT);
                    break;
            }
        }
        lines.replaceAll(s -> parse(player, s));

        if (APIScoreboard.hasScore(player)) {
            APIScoreboard scoreboard = APIScoreboard.getByPlayer(player);
            scoreboard.setTitle(title);
            scoreboard.setSlotsFromList(lines);
        }
    }

    private static String parse(Player player, String string) {
        PlayerProfile playerProfile = DeathSwap.instance.getPlayerController().get(player.getUniqueId());
        if (playerProfile == null) return string;
        Arena arena = playerProfile.getArena();
        if (arena == null) return string;
        return string
                .replace("{game_time}", Common.formatSeconds(arena.getTime()))
                .replace("{swap_time}", Common.formatSeconds(arena.getSwapTime()))
                .replace("{arena_phase}", arena.getState().getPhase().getDisplayName())
                .replace("{arena_players}", String.valueOf(arena.getPlayers().size()))
                ;
    }

}
