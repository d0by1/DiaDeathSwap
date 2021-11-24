package eu.diaworlds.deathswap.library;

import eu.diaworlds.deathswap.Config;
import eu.diaworlds.deathswap.DeathSwap;
import eu.diaworlds.deathswap.arena.Arena;
import eu.diaworlds.deathswap.arena.ArenaWorld;
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

@Getter
public class ArenaLibrary {

    private final DList<Arena> arenas;
    private final AtomicBoolean ready;

    public ArenaLibrary() {
        this.arenas = new DList<>(Config.ARENA_COUNT);
        this.ready = new AtomicBoolean(false);

        Bukkit.getScheduler().runTask(DeathSwap.instance, this::init);
    }

    private void init() {
        World world = Bukkit.getWorld("world");
        if (world == null) return;
        Common.log("Initializing arenas...");
        float arenaSize2 = Config.ARENA_SIZE / 2f;
        for (int x = 0; x < Config.ARENA_COUNT; x++) {
            for (int y = 0; y < Config.ARENA_COUNT; y++) {
                ArenaWorld arenaWorld = new ArenaWorld(world.getName(),
                        x * Config.ARENA_SIZE,
                        (x + 1) * Config.ARENA_SIZE,
                        y * Config.ARENA_SIZE,
                        (y + 1) * Config.ARENA_SIZE);
                double centerX = (int) ((arenaWorld.getMaxChunkX() - arenaSize2) * 16);
                double centerZ = (arenaWorld.getMaxChunkY() - arenaSize2) * 16;
                double centerY = world.getHighestBlockYAt((int) centerX, (int) centerZ);
                Location center = new Location(arenaWorld.getWorld(), centerX, centerY, centerZ);
                Arena arena = new Arena(UUID.randomUUID().toString(), arenaWorld, center);
                arenas.add(arena);
            }
        }
        ready.set(true);
        Common.log("Arenas initialized! (%d)", Config.ARENA_COUNT);
    }

    public void destroy() {
        for (Arena arena : arenas) {
            arena.destroy();
        }
        arenas.clear();
        ready.set(false);
    }

    public boolean isReady() {
        return ready.get();
    }

    public boolean joinIdealArena(Player player) {
        Optional<Arena> arena = getIdealArena();
        return arena.map(value -> value.join(player)).orElse(false);
    }

    public Optional<Arena> getIdealArena() {
        return arenas.stream().filter(Arena::isWaiting).min((o1, o2) -> o1.getPlayers().size() > o2.getPlayers().size() ? 1 : 0);
    }

    public Optional<Arena> getArena(Player player) {
        return Optional.ofNullable(DeathSwap.instance.getPlayerLibrary().get(player).getArena());
    }

}
