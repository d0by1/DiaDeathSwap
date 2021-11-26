package eu.diaworlds.deathswap.grid;

import java.util.Optional;

public interface Grid {

    Optional<GridPart> getPart(int x, int y);

    Optional<GridPart> getPart(int index);

}
