package eu.diaworlds.deathswap.grid;

import java.util.Optional;

public class SpiralGrid implements Grid {

    @Override
    public Optional<GridPart> getPart(int x, int y) {
        return Optional.empty();
    }

    @Override
    public Optional<GridPart> nextPart() {
        return Optional.empty();
    }

    @Override
    public boolean hasNext() {
        return false;
    }

    @Override
    public int parts() {
        return 0;
    }

    @Override
    public int size() {
        return 0;
    }
}
