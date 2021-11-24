package eu.diaworlds.deathswap.utils.scoreboard;

import eu.diaworlds.deathswap.Config;
import lombok.experimental.UtilityClass;
import org.bukkit.entity.Player;

@UtilityClass
public class Board {

    public static void create(Player player) {
        if (!APIScoreboard.hasScore(player)) {
            APIScoreboard scoreboard = APIScoreboard.createScore(player);
            scoreboard.setTitle(Config.SCOREBOARD_TITLE);
            scoreboard.setSlotsFromList(Config.SCOREBOARD_TEXT);
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
            scoreboard.setTitle(Config.SCOREBOARD_TITLE);
            scoreboard.setSlotsFromList(Config.SCOREBOARD_TEXT);
        }
    }

}
