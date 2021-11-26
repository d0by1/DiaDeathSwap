package eu.diaworlds.deathswap.grid;

import java.util.Optional;

/**
 * SpiralGrid.
 */
public class SpiralGrid implements Grid {

    private final GridPart[][] parts;
    private final int maxSize;
    private final int spacing;

    /**
     * Default constructor of SpiralGrid.
     *
     * @param maxSize
     *          maximum size of on of the grids sides in blocks.
     * @param spacing
     *          spacing between grid parts in blocks.
     */
    public SpiralGrid(int maxSize, int spacing) {
        this.maxSize = maxSize;
        this.spacing = spacing;
        this.parts = new GridPart[256][256];
    }

    @Override
    public Optional<GridPart> getPart(int x, int y) {
        return Optional.empty();
    }

    public Optional<GridPart> getSpiralPart(int index) {
        return Optional.empty();
    }

    @Override
    public Optional<GridPart> nextPart() {
        return getSpiralPart(parts());
    }

    @Override
    public boolean hasNext() {
        return true;
    }

    @Override
    public int parts() {
        return parts.length;
    }

    @Override
    public int getMaxSize() {
        return maxSize;
    }

    @Override
    public int getSpacing() {
        return spacing;
    }
}
