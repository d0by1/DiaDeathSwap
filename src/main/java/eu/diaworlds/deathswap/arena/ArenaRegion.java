package eu.diaworlds.deathswap.arena;

import lombok.Getter;
import lombok.ToString;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.World;

@Getter
@ToString
public class ArenaRegion {

    private final String worldName;
    private final int minX;
    private final int minY;
    private final int maxX;
    private final int maxY;

    private Location center;

    public ArenaRegion(String worldName, int minX, int minY, int maxX, int maxY) {
        this.worldName = worldName;
        this.minX = minX;
        this.minY = minY;
        this.maxX = maxX;
        this.maxY = maxY;
        this.center = null;
    }

    /**
     * Check whether the given position is inside this region.
     *
     * @param x the X coordinate.
     * @param y the Y coordinate.
     * @return true if the position is inside, otherwise false.
     */
    public boolean isInside(int x, int y) {
        return getMaxX() >= x && getMinX() <= x && getMaxY() >= y && getMinY() <= y;
    }

    public Location getCenter() {
        if (center == null) {
            World world = getWorld();
            if (world == null) {
                return null;
            }
            int x = getCenterX();
            int z = getCenterY();
            int y = world.getHighestBlockYAt(x, z);
            center = new Location(world, x, y, z);
        }
        return center;
    }

    public World getWorld() {
        return Bukkit.getWorld(worldName);
    }

    public int lengthX() {
        return maxX - minX;
    }

    public int lengthY() {
        return maxY - minY;
    }

    public int getCenterX() {
        return minX + lengthX() / 2;
    }

    public int getCenterY() {
        return minY + lengthY() / 2;
    }

}
