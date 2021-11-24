package eu.diaworlds.deathswap.utils;

import lombok.experimental.UtilityClass;
import net.luckperms.api.LuckPerms;
import net.luckperms.api.LuckPermsProvider;
import net.luckperms.api.model.user.User;
import org.bukkit.entity.Player;

import java.util.logging.Level;

@UtilityClass
public class LP {

    private static LuckPerms LUCK_PERMS = null;

    static {
        try {
            LUCK_PERMS = LuckPermsProvider.get();
            Common.log("Hooked into LuckPerms API!");
        } catch (Exception e) {
            Common.log(Level.WARNING, "Failed to hook into LuckPerms API.");
        }
    }

    public static User getUser(Player player) {
        if (LUCK_PERMS == null) return null;
        return LUCK_PERMS.getPlayerAdapter(Player.class).getUser(player);
    }

    public static String getPrefix(Player player) {
        if (LUCK_PERMS == null) return null;
        User user = getUser(player);
        return user.getCachedData().getMetaData().getPrefix();
    }

    public static String getSuffix(Player player) {
        if (LUCK_PERMS == null) return null;
        User user = getUser(player);
        return user.getCachedData().getMetaData().getSuffix();
    }

}
