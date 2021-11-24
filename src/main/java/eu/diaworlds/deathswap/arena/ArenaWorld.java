package eu.diaworlds.deathswap.arena;

import eu.diaworlds.deathswap.utils.S;
import lombok.Data;
import org.bukkit.Bukkit;
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

    public void regenChunks() {
        World world = getWorld();
        if (world != null) {
            for (int x = minChunkX; x < maxChunkX; x++) {
                for (int y = minChunkY; y < maxChunkY; y++) {
                    int finalX = x;
                    int finalY = y;
                    S.r(() -> world.regenerateChunk(finalX, finalY));
                }
            }
        }
    }

}
