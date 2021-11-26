package eu.diaworlds.deathswap.grid;

public class SimpleGridPart implements GridPart {

    private final int x;
    private final int y;

    /**
     * Create new instance of SimpleGridPart.
     *
     * @param x the X position of this part in a grid.
     * @param y the Y position of this part in a grid.
     */
    public SimpleGridPart(int x, int y) {
        this.x = x;
        this.y = y;
    }

    @Override
    public int getX() {
        return x;
    }

    @Override
    public int getY() {
        return y;
    }

}
