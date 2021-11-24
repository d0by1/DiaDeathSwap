package eu.diaworlds.deathswap.arena;

import eu.diaworlds.deathswap.executor.BurstExecutor;
import eu.diaworlds.deathswap.executor.MultiBurst;
import eu.diaworlds.deathswap.utils.collection.DList;
import lombok.Data;
import org.bukkit.Bukkit;
import org.bukkit.Chunk;
import org.bukkit.Location;
import org.bukkit.World;

@Data
public class ArenaWorld {

    private String world;
    private int minChunkX;
    private int maxChunkX;
    private int minChunkY;
    private int maxChunkY;

    public ArenaWorld(String world, int minChunkX, int maxChunkX, int minChunkY, int maxChunkY) {
        this.world = world;
        this.minChunkX = minChunkX;
        this.maxChunkX = maxChunkX;
        this.minChunkY = minChunkY;
        this.maxChunkY = maxChunkY;
    }

    public World getWorld() {
        return Bukkit.getWorld(world);
    }

    public Location getCenter() {
        World world = getWorld();
        if (world != null) {

        }
        return null;
    }

    public int volume() {
        return (maxChunkX - minChunkX) * (maxChunkY - minChunkY);
    }

    public DList<Chunk> getChunks() {
        DList<Chunk> list = new DList<>();
        World world = getWorld();
        if (world != null) {
            for (int x = minChunkX; x < maxChunkX; x++) {
                for (int y = minChunkY; y < maxChunkY; y++) {
                    list.add(world.getChunkAt(x, y));
                }
            }
        }
        return list;
    }

    @SuppressWarnings("deprecation")
    public void regenChunks() {
        BurstExecutor e = MultiBurst.burst.burst();
        World world = getWorld();
        if (world != null) {
            for (int x = minChunkX; x < maxChunkX; x++) {
                for (int y = minChunkY; y < maxChunkY; y++) {
                    int finalX = x;
                    int finalY = y;
                    e.queue(() -> world.regenerateChunk(finalX, finalY));
                }
            }
        }
        e.complete();
    }

}
