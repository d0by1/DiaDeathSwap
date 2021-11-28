package eu.diaworlds.deathswap.intent;

import eu.diaworlds.deathswap.utils.collection.DList;
import org.bukkit.entity.Player;

import java.util.UUID;

public interface PlayerList {

    DList<UUID> getPlayerList();

    void clearPlayers();

    boolean onJoin(Player player);

    boolean onQuit(Player player);

    boolean isJoined(UUID player);

    boolean isAllowed(UUID player);

}
