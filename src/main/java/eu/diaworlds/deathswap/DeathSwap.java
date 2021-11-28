package eu.diaworlds.deathswap;

import eu.diaworlds.deathswap.arena.ArenaController;
import eu.diaworlds.deathswap.commands.DeadCommand;
import eu.diaworlds.deathswap.commands.api.CommandManager;
import eu.diaworlds.deathswap.player.PlayerController;
import eu.diaworlds.deathswap.player.PlayerListener;
import eu.diaworlds.deathswap.utils.*;
import eu.diaworlds.deathswap.utils.config.CFG;
import eu.diaworlds.deathswap.utils.ticker.Ticker;
import lombok.Getter;
import org.bukkit.*;
import org.bukkit.entity.Player;
import org.bukkit.event.Listener;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.File;

@Getter
public final class DeathSwap extends JavaPlugin {

    // Running instance of the DeathSwap plugin.
    public static DeathSwap instance;
    private Ticker ticker;
    private ArenaController arenaController;
    private PlayerController playerController;
    private CommandManager commandManager;
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
        DExecutor.init(Runtime.getRuntime().availableProcessors());

        this.ticker = new Ticker();
        this.arenaController = new ArenaController();
        this.playerController = new PlayerController();
        this.commandManager = new CommandManager();

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
            kick(player, Config.parse(Config.ARENA_STOP_KICK));
        }
        BungeeUtils.destroy();
        this.arenaController.destroy();
        this.playerController.destroy();
        deleteWorld();
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
        return arenaController.isReady();
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
     * Teleport given player to set spawn location.
     *
     * @param player the player.
     */
    public void spawn(Player player) {
        teleport(player, getSpawn());
    }

    /**
     * Teleport given player to the given location. Always teleported on the primary thread.
     *
     * @param player the player.
     * @param location the location.
     */
    public void teleport(Player player, Location location) {
        if (!Bukkit.isPrimaryThread()) {
            S.sync(() -> player.teleport(location));
            return;
        }
        player.teleport(location);
    }

    /**
     * Setup "world" for playing. (Just some general game rules and difficulty.)
     */
    private void setupWorld() {
        // Settings for world "world"
        World world = Bukkit.getWorld("world");
        if (world != null) {
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

        // Settings for spawn world
        World spawn = getSpawn().getWorld();
        if (spawn != null) {
            if (spawn.getName().equals("world")) return;
            spawn.setStorm(false);
            spawn.setThundering(false);
            spawn.setClearWeatherDuration(Integer.MAX_VALUE);
            spawn.setGameRule(GameRule.ANNOUNCE_ADVANCEMENTS, false);
            spawn.setGameRule(GameRule.DO_FIRE_TICK, false);
            spawn.setGameRule(GameRule.DO_MOB_SPAWNING, false);
            spawn.setGameRule(GameRule.DO_PATROL_SPAWNING, false);
            spawn.setGameRule(GameRule.DO_TRADER_SPAWNING, false);
            spawn.setGameRule(GameRule.KEEP_INVENTORY, true);
            spawn.setGameRule(GameRule.DO_IMMEDIATE_RESPAWN, true);
            spawn.setGameRule(GameRule.DO_DAYLIGHT_CYCLE, false);
            spawn.setGameRule(GameRule.DO_WEATHER_CYCLE, false);
            spawn.setDifficulty(Difficulty.PEACEFUL);
        }
    }

    private void deleteWorld() {
        File worldFolder = new File(Bukkit.getWorldContainer(), "world");
        if (worldFolder.exists() && worldFolder.isDirectory()) {
            worldFolder.delete();
        }
    }

}
