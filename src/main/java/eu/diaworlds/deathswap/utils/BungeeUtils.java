package eu.diaworlds.deathswap.utils;

import com.google.common.io.ByteArrayDataOutput;
import com.google.common.io.ByteStreams;
import eu.diaworlds.deathswap.DeathSwap;
import lombok.experimental.UtilityClass;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.plugin.messaging.Messenger;

@UtilityClass
public class BungeeUtils {

    private static boolean initialized = false;

    public static void init() {
        if (initialized) return;
        Messenger messenger = Bukkit.getServer().getMessenger();
        messenger.registerOutgoingPluginChannel(DeathSwap.instance, "BungeeCord");
        initialized = true;
    }

    public static void destroy() {
        if (!initialized) return;
        Messenger messenger = Bukkit.getServer().getMessenger();
        messenger.unregisterOutgoingPluginChannel(DeathSwap.instance, "BungeeCord");
        initialized = false;
    }

    public static void connect(Player player, String server) {
        if (!initialized) init();
        try {
            ByteArrayDataOutput out = ByteStreams.newDataOutput();
            out.writeUTF("Connect");
            out.writeUTF(server);
            player.sendPluginMessage(DeathSwap.instance, "BungeeCord", out.toByteArray());
        } catch (Exception ignored) {}
    }

}
