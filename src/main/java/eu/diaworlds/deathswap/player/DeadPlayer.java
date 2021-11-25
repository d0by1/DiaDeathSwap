package eu.diaworlds.deathswap.player;

import eu.diaworlds.deathswap.Config;
import eu.diaworlds.deathswap.DeathSwap;
import eu.diaworlds.deathswap.arena.Arena;
import eu.diaworlds.deathswap.utils.Players;
import lombok.Getter;
import lombok.Setter;
import org.bukkit.entity.Player;

@Getter
@Setter
public class DeadPlayer {

    private final Player parent;
    private Arena arena;

    public DeadPlayer(Player parent) {
        this.parent = parent;
    }

    public void onJoin() {
        if (arena != null) return;
        for (String line : Config.JOIN_INFO) {
            parent.sendMessage(Config.parse(line));
        }
        parent.teleport(DeathSwap.instance.getSpawn());
        Players.hide(parent);
        boolean joined = DeathSwap.instance.getArenaLibrary().joinIdealArena(parent);
        if (!joined) {
            DeathSwap.instance.kick(parent, Config.parse(Config.NO_ARENA_KICK));
        }
    }

    public void onQuit() {
        if (arena != null) {
            arena.quit(parent);
        }
        Players.show(parent);
    }

}
