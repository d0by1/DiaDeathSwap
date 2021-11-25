package eu.diaworlds.deathswap.utils;

import lombok.experimental.UtilityClass;
import net.luckperms.api.LuckPerms;
import net.luckperms.api.LuckPermsProvider;
import net.luckperms.api.cacheddata.CachedMetaData;
import net.luckperms.api.model.group.Group;
import net.luckperms.api.model.user.User;
import org.bukkit.entity.Player;

import java.util.Optional;
import java.util.logging.Level;

/**
 * Simple access point to player groups via LuckPermsAPI.
 *
 * @author d0by
 * @since 1.0
 */
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

    /**
     * Get the LP user profile of given player.
     *
     * @param player the player.
     * @return the user profile.
     */
    public static User getUser(Player player) {
        if (LUCK_PERMS == null) return null;
        return LUCK_PERMS.getPlayerAdapter(Player.class).getUser(player);
    }

    /**
     * Get the name of player's primary LP group.
     *
     * @param player the player.
     * @return name of the group.
     */
    public static String getGroupName(Player player) {
        User user = getUser(player);
        if (user == null) return null;
        return user.getPrimaryGroup();
    }

    /**
     * Get the primary LP group of given player.
     *
     * @param player the player.
     * @return the Group.
     */
    public static Group getGroup(Player player) {
        String name = getGroupName(player);
        if (name == null) return null;
        return LUCK_PERMS.getGroupManager().getGroup(name);
    }

    /**
     * Get the LP metadata of given player.
     *
     * @param player the player.
     * @return the metadata Optional.
     */
    public static Optional<CachedMetaData> getMetaData(Player player) {
        User user = getUser(player);
        if (user == null) {
            return Optional.empty();
        }
        return Optional.of(user.getCachedData().getMetaData());
    }

    /**
     * Get the LP metadata of given players group.
     *
     * @param player the player.
     * @return the metadata Optional.
     */
    public static Optional<CachedMetaData> getGroupMetaData(Player player) {
        Group group = getGroup(player);
        if (group == null) {
            return Optional.empty();
        }
        return Optional.of(group.getCachedData().getMetaData());
    }

    /**
     * Get the prefix from LP metadata of given players user profile.
     *
     * @param player the player.
     * @return the prefix.
     */
    public static String getPrefix(Player player) {
        User user = getUser(player);
        if (user == null) return "";
        String prefix = user.getCachedData().getMetaData().getPrefix();
        return prefix == null ? "" : prefix;
    }

    /**
     * Get the suffix from LP metadata of given players user profile.
     *
     * @param player the player.
     * @return the suffix.
     */
    public static String getSuffix(Player player) {
        User user = getUser(player);
        if (user == null) return "";
        String suffix = user.getCachedData().getMetaData().getSuffix();
        return suffix == null ? "" : suffix;
    }

    /**
     * Get the value of "username-color" from LP metadata of players user profile or group.
     *
     * @param player the player.
     * @return the value of "username-color";
     */
    public static String getUsernameColor(Player player) {
        Optional<CachedMetaData> metaData;
        if ((metaData = getMetaData(player)).isPresent()) {
            String messageColor = metaData.get().getMetaValue("username-color");
            if (messageColor != null) {
                return messageColor;
            }
        }
        if ((metaData = getGroupMetaData(player)).isPresent()) {
            String messageColor = metaData.get().getMetaValue("username-color");
            if (messageColor != null) {
                return messageColor;
            }
        }
        return "";
    }

    /**
     * Get the value of "message-color" from LP metadata of players user profile or group.
     *
     * @param player the player.
     * @return the value of "message-color";
     */
    public static String getMessageColor(Player player) {
        Optional<CachedMetaData> metaData;
        if ((metaData = getMetaData(player)).isPresent()) {
            String messageColor = metaData.get().getMetaValue("message-color");
            if (messageColor != null) {
                return messageColor;
            }
        }
        if ((metaData = getGroupMetaData(player)).isPresent()) {
            String messageColor = metaData.get().getMetaValue("message-color");
            if (messageColor != null) {
                return messageColor;
            }
        }
        return "";
    }

}
