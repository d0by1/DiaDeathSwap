package eu.diaworlds.deathswap.library;

import eu.diaworlds.deathswap.Config;
import eu.diaworlds.deathswap.DeathSwap;
import eu.diaworlds.deathswap.arena.Arena;
import eu.diaworlds.deathswap.arena.Area;
import eu.diaworlds.deathswap.grid.Grid;
import eu.diaworlds.deathswap.grid.GridPart;
import eu.diaworlds.deathswap.grid.SpiralGrid;
import eu.diaworlds.deathswap.utils.Common;
import eu.diaworlds.deathswap.utils.collection.DList;
import lombok.Getter;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.entity.Player;

import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;

@Getter
public class ArenaLibrary {

    private static final int AREA_SIZE = 2000;
    private static final int AREA_SIZE_WITH_SPACE = 3000;
    private final DList<Arena> arenas;
    private final Grid grid;
    private final AtomicInteger arenaCounter;
    private final AtomicBoolean ready;

    /**
     * Default constructor.
     */
    public ArenaLibrary() {
        this.arenas = new DList<>(128);
        this.grid = new SpiralGrid();
        this.arenaCounter = new AtomicInteger(0);
        this.ready = new AtomicBoolean(false);

        Bukkit.getScheduler().runTask(DeathSwap.instance, () -> initArenas(Config.ARENA_COUNT));
    }

    /**
     * Initialize the given amount of new arenas.
     *
     * @param amount the amount.
     */
    private void initArenas(int amount) {
        World world = Bukkit.getWorld("world");
        if (world == null) return;

        Common.log("Initializing arenas... (%d)", amount);
        for (int i = 0; i < amount; i++) {
            // Get arenas location from the spiral grid.
            Optional<GridPart> gridPartOptional = grid.getPart(arenaCounter.getAndIncrement());
            if (gridPartOptional.isPresent()) {
                GridPart gridPart = gridPartOptional.get();
                // Calculate the actual world location.
                int x = gridPart.getX() * AREA_SIZE_WITH_SPACE;
                int z = gridPart.getY() * AREA_SIZE_WITH_SPACE;
                int y = world.getHighestBlockYAt(x, z);
                Location center = new Location(world, x, y, z);
                // Add the arena
                Arena arena = new Arena(UUID.randomUUID().toString(), new Area(x, z, AREA_SIZE), center);
                arenas.add(arena);
            }
        }
        Common.log("Arenas initialized! (%d)", arenas.size());
        ready.set(true);
    }

    /**
     * Destroy and remove all arenas.
     */
    public void destroy() {
        for (Arena arena : arenas) {
            arena.destroy();
        }
        arenas.clear();
        ready.set(false);
    }

    /**
     * Check whether the ArenaLibrary is ready for players to join.
     *
     * @return the status.
     */
    public boolean isReady() {
        return ready.get();
    }

    /**
     * Remove given arena from the library. Once the arena is removed,
     * this method will check if there are enough arenas and if not,
     * it will initialize a new one.
     *
     * @param arena the arena to remove.
     */
    public void removeArena(Arena arena) {
        arenas.remove(arena);

        // Initialize new arena
        if (arenas.size() < Config.ARENA_COUNT) {
            initArenas(1);
        }
    }

    /**
     * Join given player to an ideal arena.
     *
     * @param player the player.
     * @return boolean whether the join was successful.
     */
    public boolean joinIdealArena(Player player) {
        Optional<Arena> arena = getIdealArena();
        return arena.map(value -> value.join(player)).orElse(false);
    }

    /**
     * Get the ideal arena to join. Ideal arena is in waiting phase, not full and has the most players.
     *
     * @return the ideal arena Optional. (There might be no ideal arena)
     */
    public Optional<Arena> getIdealArena() {
        return arenas.stream().filter(Arena::isWaiting).min((o1, o2) -> o1.getPlayers().size() >= o2.getPlayers().size() ? 0 : 1);
    }

    /**
     * Get the arena given player is joined.
     *
     * @param player the player.
     * @return the arena Optional. (He might not be in any arena)
     */
    public Optional<Arena> getArena(Player player) {
        return Optional.ofNullable(DeathSwap.instance.getPlayerLibrary().get(player).getArena());
    }

}
