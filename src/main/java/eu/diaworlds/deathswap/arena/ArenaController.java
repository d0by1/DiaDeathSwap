package eu.diaworlds.deathswap.arena;

import eu.diaworlds.deathswap.Config;
import eu.diaworlds.deathswap.DeathSwap;
import eu.diaworlds.deathswap.grid.Grid;
import eu.diaworlds.deathswap.grid.GridPart;
import eu.diaworlds.deathswap.grid.SpiralGrid;
import eu.diaworlds.deathswap.player.PlayerProfile;
import eu.diaworlds.deathswap.utils.Common;
import eu.diaworlds.deathswap.utils.S;
import eu.diaworlds.deathswap.utils.collection.DList;
import org.bukkit.Bukkit;
import org.bukkit.World;
import org.bukkit.entity.Player;

import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;

public class ArenaController {

    private static final int ARENA_SIZE = 2000;
    private static final int ARENA_HALF_SIZE = 1000;
    private static final int ARENA_SPACE = 1000;

    private final Map<String, Arena> arenas;
    private final AtomicInteger arenaCounter;
    private final AtomicBoolean ready;
    private final Grid grid;
    private final DList<UUID> joinQueue;

    /**
     * Default constructor. Set up and initialize arenas.
     */
    public ArenaController() {
        this.arenas = new ConcurrentHashMap<>(Config.ARENA_COUNT);
        this.arenaCounter = new AtomicInteger(0);
        this.ready = new AtomicBoolean(false);
        this.grid = new SpiralGrid();
        this.joinQueue = new DList<>();

        S.sync(() -> initArenas(Config.ARENA_COUNT));
    }

    /*
     *  General Methods
     */

    /**
     * Destroy and remove all arenas.
     */
    public void destroy() {
        joinQueue.clear();
        ready.set(false);
        // Destroy all arenas
        for (Arena arena : arenas.values()) {
            arena.destroy();
        }
        // Clear
        arenas.clear();
    }

    /**
     * Check whether the ArenaLibrary is ready for players to join.
     *
     * @return the status.
     */
    public boolean isReady() {
        return ready.get() && !arenas.isEmpty();
    }

    /*
     *  Queue Methods
     */

    public void joinQueue(Player player) {
        UUID uuid = player.getUniqueId();
        if (!joinQueue.contains(uuid)) {
            joinQueue.add(uuid);

        }
    }

    public void leaveQueue(UUID uuid) {
        joinQueue.remove(uuid);
    }

    public void updateQueue() {
        for (UUID uuid : joinQueue) {
            Optional<Arena> arenaOptional = getIdealArena();
            if (arenaOptional.isPresent()) {
                PlayerProfile profile = DeathSwap.instance.getPlayerController().get(uuid);
                profile.attemptArenaJoin(arenaOptional.get());
            }
            return;
        }
    }

    /*
     *  Arena Methods
     */

    /**
     * Register a new arena if not present.
     *
     * @param arena The arena.
     */
    public void registerArena(Arena arena) {
        if (!arenas.containsKey(arena.getId())) {
            arenas.put(arena.getId(), arena);
        }
    }

    /**
     * Remove and destroy an arena by id.
     *
     * @param id The id.
     */
    public void unregisterArena(String id) {
        Arena arena = arenas.remove(id);
        if (arena != null) {
            arena.destroy();
        }

        // Init new arenas if missing
        if (arenas.size() < Config.ARENA_COUNT) {
            initArenas(1);
//            updateQueue();
        }
    }

    /**
     * Get the ideal arena to join. Ideal arena is in waiting phase, not full and has the most players.
     *
     * @return the ideal arena Optional. (There might be no ideal arena)
     */
    public Optional<Arena> getIdealArena() {
        if (isReady()) {
            return arenas.values().stream()
                    .filter(arena -> arena.getState().isWaiting())
                    .min((o1, o2) -> o1.getPlayers().size() >= o2.getPlayers().size() ? 0 : 1);
        }
        return Optional.empty();
    }

    /**
     * Get the arena given player is joined.
     *
     * @param player the player.
     * @return the arena Optional. (He might not be in any arena)
     */
    public Optional<Arena> getArena(UUID player) {
        return Optional.ofNullable(DeathSwap.instance.getPlayerController().get(player).getArena());
    }

    /**
     * Initialize the given amount of new arenas.
     *
     * @param amount the amount.
     */
    private void initArenas(int amount) {
        World world = Bukkit.getWorld("world");
        if (world == null) return;

        Common.log("Initializing %d arenas...", amount);
        for (int i = 0; i < amount; i++) {
            // Get arenas location from the spiral grid.
            Optional<GridPart> gridPartOptional = grid.getPart(arenaCounter.getAndIncrement());
            if (gridPartOptional.isPresent()) {
                GridPart gridPart = gridPartOptional.get();
                // Calculate the actual world location.
                int x = gridPart.getX() * (ARENA_SIZE + ARENA_SPACE);
                int z = gridPart.getY() * (ARENA_SIZE + ARENA_SPACE);
                ArenaRegion region = new ArenaRegion(
                        world.getName(),
                        x - ARENA_HALF_SIZE,
                        z - ARENA_HALF_SIZE,
                        x + ARENA_HALF_SIZE,
                        z + ARENA_HALF_SIZE
                );
                // Add the arena
                Arena arena = new Arena(UUID.randomUUID().toString(), region);
                registerArena(arena);
            }
        }
        Common.log("Arenas initialized! Total: %d", arenas.size());
        ready.set(true);
    }

}
