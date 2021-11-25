package eu.diaworlds.deathswap;

import eu.decentsoftware.holograms.api.DecentHologramsAPI;
import eu.diaworlds.deathswap.commands.DeadCommand;
import eu.diaworlds.deathswap.commands.api.CommandManager;
import eu.diaworlds.deathswap.library.ArenaLibrary;
import eu.diaworlds.deathswap.library.PlayerLibrary;
import eu.diaworlds.deathswap.player.PlayerListener;
import eu.diaworlds.deathswap.tick.Ticker;
import eu.diaworlds.deathswap.utils.BungeeUtils;
import eu.diaworlds.deathswap.utils.Common;
import eu.diaworlds.deathswap.utils.Locations;
import eu.diaworlds.deathswap.utils.WorldUtils;
import eu.diaworlds.deathswap.utils.config.CFG;
import lombok.Getter;
import org.bukkit.*;
import org.bukkit.entity.Player;
import org.bukkit.event.Listener;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.File;

@Getter
public final class DeathSwap extends JavaPlugin {

    /**
     * Running instance of the DeathSwap plugin.
     */
    public static DeathSwap instance;
    private CommandManager commandManager;
    private ArenaLibrary arenaLibrary;
    private PlayerLibrary playerLibrary;
    private Ticker ticker;

    // Spawn location of the server (Like a waiting lobby or something)
    private Location spawn;
    // The "config.yml" file.
    private File configFile;

    /**
     * Default constructor
     */
    public DeathSwap() {
        instance = this;
    }

    @Override
    public void onEnable() {
        Config.CONFIG = CFG.load(Config.class, getConfigFile());
        Common.log("Configuration loaded!");
        BungeeUtils.init();

        this.commandManager = new CommandManager();
        this.arenaLibrary = new ArenaLibrary();
        this.playerLibrary = new PlayerLibrary();
        this.ticker = new Ticker();

        registerListener(new PlayerListener());

        commandManager.registerMainCommand(new DeadCommand());

        // Run this when all worlds are ready
        Bukkit.getScheduler().runTask(this, () -> {
            setSpawn(Locations.asLocation(Config.SPAWN));
            setupWorld();
            Common.log("World is ready!");
        });
    }

    @Override
    public void onDisable() {
        for (Player player : Bukkit.getOnlinePlayers()) {
            kick(player, Config.parse(Config.SERVER_RESTART_KICK));
        }
        BungeeUtils.destroy();
        this.arenaLibrary.destroy();
        this.playerLibrary.destroy();
        WorldUtils.deleteWorld("world");
    }

    /**
     * Reload the configuration.
     */
    public void reload() {
        Config.CONFIG = CFG.load(Config.class, getConfigFile());
    }

    /**
     * Check whether the server is ready for players to join.
     *
     * @return ready state.
     */
    public boolean isReady() {
        return arenaLibrary.isReady();
    }

    /**
     * Set the server spawn location.
     * <p>
     *     This method also saves it into the "config.yml" file.
     * </p>
     *
     * @param spawn the spawn location.
     */
    public void setSpawn(Location spawn) {
        this.spawn = spawn;
        Config.SPAWN = Locations.asString(spawn, true);
        Config.save(getConfigFile());
    }

    /**
     * Get the "config.yml" file.
     *
     * @return the file.
     */
    public File getConfigFile() {
        if (configFile == null) {
            configFile = new File(getDataFolder(), "config.yml");
        }
        return configFile;
    }

    /*
     *  Utility methods
     */

    /**
     * Register a new listener.
     *
     * @param listener the listener.
     */
    public void registerListener(Listener listener) {
        Bukkit.getPluginManager().registerEvents(listener, this);
    }

    /**
     * Kick player to configured lobby server with a reason.
     *
     * @param player the player.
     * @param reason the reason.
     */
    public void kick(Player player, String reason) {
        Common.tell(player, Config.parse(reason));
        BungeeUtils.connect(player, Config.LOBBY_SERVER);
    }

    /**
     * Setup "world" for playing. (Just some general game rules and difficulty.)
     */
    private void setupWorld() {
        World world = Bukkit.getWorld("world");
        if (world == null) return;
        WorldUtils.setupBorder(world, Config.ARENA_SIZE_BLOCKS);
        world.setGameRule(GameRule.ANNOUNCE_ADVANCEMENTS, false);
        world.setGameRule(GameRule.DO_IMMEDIATE_RESPAWN, true);
        Difficulty difficulty;
        try {
            difficulty = Difficulty.valueOf(Config.DIFFICULTY);
        } catch (Throwable t) {
            difficulty = Difficulty.HARD;
        }
        world.setDifficulty(difficulty);
    }

}
