package eu.diaworlds.deathswap.grid;

import java.util.Optional;

public interface Grid {

    Optional<GridPart> getPart(int x, int y);

    Optional<GridPart> nextPart();

    boolean hasNext();

    int parts();

    int getMaxSize();

    int getSpacing();

}
