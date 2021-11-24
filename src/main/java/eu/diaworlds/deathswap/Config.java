package eu.diaworlds.deathswap;

import eu.diaworlds.deathswap.utils.Common;
import eu.diaworlds.deathswap.utils.collection.DList;
import eu.diaworlds.deathswap.utils.config.CFG;
import eu.diaworlds.deathswap.utils.config.ConfigValue;
import org.bukkit.Difficulty;
import org.bukkit.configuration.file.YamlConfiguration;

import java.io.File;
import java.util.List;
import java.util.logging.Level;

public class Config {

    public static final int ARENA_SIZE = 128;
    public static final int ARENA_SIZE_BLOCKS = ARENA_SIZE * 16;
    public static YamlConfiguration CONFIG;

    /*
     *  General
     */

    @ConfigValue("difficulty")
    public static Difficulty DIFFICULTY = Difficulty.HARD;

    @ConfigValue("spawn")
    public static String SPAWN = "world:0:100:0:0:180";

    @ConfigValue("lobby_server")
    public static String LOBBY_SERVER = "lobby2";

    @ConfigValue("chat_format")
    public static String CHAT_FORMAT = "{prefix}{name}{suffix} &7» &f{message}";

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
    public static String SCOREBOARD_TITLE = "&f☠ &c&lDEATHSWAP &f☠";

    @ConfigValue("scoreboard.text")
    public static List<String> SCOREBOARD_TEXT = new DList<>("",
            "",
            ""
    );

    /*
     *  Messages
     */

    @ConfigValue("messages.prefix")
    public static String PREFIX = "&8(( &cDeathSwap &8)) &7";

    @ConfigValue("messages.no_perm")
    public static String NO_PERM = "{prefix}&cNa toto nemáš oprávnění.";

    @ConfigValue("messages.join_info")
    public static List<String> JOIN_INFO = new DList<>("",
            "&c&lDEATHSWAP &7(1.0)\n",
            "",
            "&7&oKaždé 2 minuty si s protihráčem vyměníte lokace.",
            "&7&oTvým cílem je protihráče při výměně zabít tak, že",
            "&7&onapřiklad skočíš těsně před teleportem do pasti.",
            "",
            "&7&oDej si však pozor, protihráč se bude snažit o to samé.",
            "",
            "&7&oNormální PVP je vypnuto.",
            "",
            "&7&oNašel jsi nějaký bug? &fhttps://discord.diaworlds.eu/ \n"
    );

    @ConfigValue("messages.end.winner")
    public static List<String> END_MESSAGE_WINNER = new DList<>("",
            "",
            ""
    );

    @ConfigValue("messages.end.time")
    public static List<String> END_MESSAGE_TIME = new DList<>("",
            "",
            ""
    );

    @ConfigValue("messages.start_delayed")
    public static String START_DELAYED = "{prefix}Začátek hry byl přerušen.";

    @ConfigValue("messages.start_announce")
    public static String START_ANNOUNCE = "{prefix}Hra začíná za &b%1$d sekund!";

    @ConfigValue("messages.swap_announce")
    public static String SWAP_ANNOUNCE = "{prefix}Výměna za &b%1$d sekund!";

    @ConfigValue("messages.end_announce")
    public static String END_ANNOUNCE = "{prefix}Konec hry za &b%1$d sekund!";

    @ConfigValue("messages.stop_announce")
    public static String STOP_ANNOUNCE = "{prefix}Server se ukončí za &b%1$d sekund!";

    @ConfigValue("messages.player_join")
    public static String PLAYER_JOIN = "{prefix}%1$s &7se připojil. &b[%2$d/2]";

    @ConfigValue("messages.player_quit")
    public static String PLAYER_QUIT = "{prefix}%1$s &7se odpojil. &b[%2$d/2]";

    @ConfigValue("messages.game_ended_kick")
    public static String GAME_ENDED_KICK = "{prefix}&bHra skončila.";

    @ConfigValue("messages.no_arena_kick")
    public static String NO_ARENA_KICK = "{prefix}&cNebyla nalezena žádná volná hra...";

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
