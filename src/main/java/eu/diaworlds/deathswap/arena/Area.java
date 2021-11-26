package eu.diaworlds.deathswap.arena;

import lombok.Getter;

@Getter
public class Area {

    private final int centerX;
    private final int centerY;
    private final int size;

    public Area(int centerX, int centerY, int size) {
        this.centerX = centerX;
        this.centerY = centerY;
        this.size = size;
    }

    /**
     * Check whether the given position is inside the area.
     *
     * @param x the X coordinate.
     * @param y the Y coordinate.
     * @return true if the position is inside, otherwise false.
     */
    public boolean isInside(int x, int y) {
        return getMaxX() > x && getMinX() < x && getMaxY() > y && getMinY() < y;
    }

    /**
     * Get the maximum X position a player can reach.
     *
     * @return the maximum X position.
     */
    public int getMaxX() {
        return centerX + size / 2;
    }

    /**
     * Get the minimum X position a player can reach.
     *
     * @return the minimum X position.
     */
    public int getMinX() {
        return centerX - size / 2;
    }

    /**
     * Get the maximum Y position a player can reach.
     *
     * @return the maximum Y position.
     */
    public int getMaxY() {
        return centerY + size / 2;
    }

    /**
     * Get the minimum Y position a player can reach.
     *
     * @return the minimum Y position.
     */
    public int getMinY() {
        return centerY - size / 2;
    }

}
