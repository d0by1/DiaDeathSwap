package eu.diaworlds.deathswap.arena;

public class Area {

    private final int centerX;
    private final int centerY;
    private final int size;

    public Area(int centerX, int centerY, int size) {
        this.centerX = centerX;
        this.centerY = centerY;
        this.size = size;
    }

    public int getCenterX() {
        return centerX;
    }

    public int getCenterY() {
        return centerY;
    }

    public int getSize() {
        return size;
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
