package eu.diaworlds.deathswap.grid;

import org.bukkit.Location;

public interface GridPart {

    int getSize();

    int getX();

    int getY();

    Location getCenter();

}
