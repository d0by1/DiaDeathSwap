package eu.diaworlds.deathswap.grid;

import java.util.Optional;

/**
 * SpiralGrid
 *
 * GridParts are not saved in this Grid implementation. This
 * is just used for calculation the next position of GridPart
 * in a spiral.
 *
 * @author d0by
 * @since 1.0
 */
public class SpiralGrid implements Grid {

    /**
     * Not implemented for SpiralGrid!
     *
     * @return empty Optional.
     */
    @Override
    public Optional<GridPart> getPart(int x, int y) {
        return Optional.empty();
    }

    /**
     * Get the n-th grid part in a spiral grid.
     *
     * @param index index of the part.
     * @return the grid part.
     */
    @Override
    public Optional<GridPart> getPart(int index) {
        int[] pos = getGridPartPositionSpiral(index);
        GridPart gridPart = new SimpleGridPart(pos[0], pos[1]);
        return Optional.of(gridPart);
    }

    /**
     * Calculate the position in a grid
     * on the specified index following a spiral.
     *
     * @param index the index.
     * @return the position. (x, y)
     */
    private int[] getGridPartPositionSpiral(int index) {
        if (index == 0) {
            return new int[] {0, 0};
        }

        // current position (x, y) and how much of current segment we passed
        int x = 0;
        int y = 0;
        // (dx, dy) is a vector - direction in which we move right now
        int dx = 0;
        int dy = 1;
        // length of current segment
        int segment_length = 1;
        int segment_passed = 0;

        for (int i = 0; i < index; ++i) {
            // make a step, add 'direction' vector (dx, dy) to current position (x, y)
            x += dx;
            y += dy;
            ++segment_passed;

            if (segment_passed == segment_length) {
                // done with current segment
                segment_passed = 0;

                // 'rotate' directions
                int buffer = dy;
                dy = -dx;
                dx = buffer;

                // increase segment length if necessary
                if (dx == 0) {
                    ++segment_length;
                }
            }
        }
        return new int[] {x, y};
    }

}
