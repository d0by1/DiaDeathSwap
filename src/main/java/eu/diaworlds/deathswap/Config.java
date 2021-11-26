package eu.diaworlds.deathswap;

import eu.diaworlds.deathswap.utils.Common;
import eu.diaworlds.deathswap.utils.collection.DList;
import eu.diaworlds.deathswap.utils.config.CFG;
import eu.diaworlds.deathswap.utils.config.ConfigValue;
import org.bukkit.configuration.file.YamlConfiguration;

import java.io.File;
import java.util.List;
import java.util.logging.Level;

@SuppressWarnings("SpellCheckingInspection")
public class Config {

    public static YamlConfiguration CONFIG;

    /*
     *  General
     */

    @ConfigValue("difficulty")
    public static String DIFFICULTY = "HARD";

    @ConfigValue("spawn")
    public static String SPAWN = "spawn:7.500:70.000:3.500:90.000:0.000";

    @ConfigValue("lobby_server")
    public static String LOBBY_SERVER = "lobby2";

    @ConfigValue("chat_format")
    public static String CHAT_FORMAT = "{prefix}{name}{username-color}{suffix} &7» {message-color}{message}";

    @ConfigValue("arena_count")
    public static int ARENA_COUNT = 32;

    @ConfigValue("swap_time")
    public static int SWAP_TIME = 120;

    @ConfigValue("game_time")
    public static int GAME_TIME = 3600;

    /*
     *  Scoreboard
     */

    @ConfigValue("scoreboard.title")
    public static String SCOREBOARD_TITLE = "&f☠ &3&lDEATHSWAP &f☠";

    @ConfigValue("scoreboard.text")
    public static List<String> SCOREBOARD_TEXT = new DList<>("",
            "&3Arena:",
            "  &7Hráči: &b{arena_players}/2",
            "  &7Fáze: &b{arena_phase}",
            "  &7Čas hry: &b{game_time}",
            "  &7Výměna za &b{swap_time}",
            ""
    );

    /*
     *  Titles
     */

    @ConfigValue("titles.end.winner.title")
    public static String GAME_END_WINNER_TITLE = "&b&lVyhrál Jsi!";

    @ConfigValue("titles.end.winner.subtitle")
    public static String GAME_END_WINNER_SUBTITLE = "&fGratulujeme.";

    @ConfigValue("titles.end.loser.title")
    public static String GAME_END_LOSER_TITLE = "&c&lHra skončila";

    @ConfigValue("titles.end.loser.subtitle")
    public static String GAME_END_LOSER_SUBTITLE = "&fVyhrál &b{winner}";

    @ConfigValue("titles.end.draw.title")
    public static String GAME_END_DRAW_TITLE = "&c&lHra skončila";

    @ConfigValue("titles.end.draw.subtitle")
    public static String GAME_END_DRAW_SUBTITLE = "&fNikdo nevyhrál.";

    /*
     *  Messages
     */

    @ConfigValue("messages.prefix")
    public static String PREFIX = "&8[&bDeathSwap&8] &7";

    @ConfigValue("messages.no_perm")
    public static String NO_PERM = "{prefix}&cNa toto nemáš oprávnění.";

    @ConfigValue("messages.join_info")
    public static List<String> JOIN_INFO = new DList<>("",
            "&f☠ &c&lDEATHSWAP &f☠ &7(v1.0)\n",
            "",
            " &f- Každé 2 minuty si s protihráčem vyměníte pozice.",
            " &fTvým cílem je protihráče při výměně zabít tak, že",
            " &fnapříklad skočíš těsně před teleportem do pasti.",
            "",
            " &f- Dej si však pozor, protihráč se bude snažit o to samé.",
            "",
            " &f- Normální PVP je vypnuto.",
            "",
            " &bNašel jsi nějaký bug? &7&nhttps://discord.diaworlds.eu/ \n"
    );

    @ConfigValue("messages.game.end.winner")
    public static List<String> GAME_END_MESSAGE_WINNER = new DList<>("",
            "Winner: {winner}",
            ""
    );

    @ConfigValue("messages.game.end.time")
    public static List<String> GAME_END_MESSAGE_TIME = new DList<>("",
            "Draw",
            ""
    );

    @ConfigValue("messages.game.start_delayed")
    public static String GAME_START_DELAYED = "{prefix}&bZačátek hry byl přerušen.";

    @ConfigValue("messages.game.start_announce")
    public static String GAME_START_ANNOUNCE = "{prefix}Hra začíná za &b%1$d sekund!";

    @ConfigValue("messages.game.started")
    public static String GAME_STARTED = "{prefix}&bHra začala! Přelsti svého nepřítele.";

    @ConfigValue("messages.game.swap_announce")
    public static String GAME_SWAP_ANNOUNCE = "{prefix}Výměna za &b%1$d sekund!";

    @ConfigValue("messages.game.end_announce")
    public static String GAME_END_ANNOUNCE = "{prefix}Konec hry za &b%1$d sekund!";

    @ConfigValue("messages.game.player_death")
    public static String GAME_PLAYER_DEATH = "{prefix}&b%1$s &7died.";

    @ConfigValue("messages.arena.stop_announce")
    public static String ARENA_STOP_ANNOUNCE = "{prefix}Aréna se vypne za &b%1$d sekund!";

    @ConfigValue("messages.arena.player_join")
    public static String ARENA_PLAYER_JOIN = "{prefix}&b%1$s &7se připojil. &b[%2$d/2]";

    @ConfigValue("messages.arena.player_quit")
    public static String ARENA_PLAYER_QUIT = "{prefix}&b%1$s &7se odpojil. &b[%2$d/2]";

    @ConfigValue("messages.arena.no_arena_kick")
    public static String ARENA_NO_ARENA_KICK = "{prefix}&bNebyla nalezena žádná volná hra...";

    @ConfigValue("messages.arena.arena_stop_kick")
    public static String ARENA_STOP_KICK = "{prefix}&bAréna se restartuje...";

    /*
     *  General Methods
     */

    /**
     * Colorize a string and replace "{prefix}" to configured prefix.
     *
     * @param string the string.
     * @return the parsed string.
     */
    public static String parse(String string) {
        return Common.colorize(string.replace("{prefix}", PREFIX));
    }

    /**
     * Save current cached configuration to the given file.
     *
     * @param file the file.
     */
    public static void save(File file) {
        try {
            CFG.write(Config.class, file);
        } catch (Exception e) {
            Common.log(Level.WARNING, "Failed to save config to a file.");
            e.printStackTrace();
        }
    }

}
