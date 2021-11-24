package eu.diaworlds.deathswap.utils;

import eu.diaworlds.deathswap.DeathSwap;
import lombok.experimental.UtilityClass;
import org.bukkit.Bukkit;
import org.bukkit.World;
import org.bukkit.WorldBorder;
import org.bukkit.entity.Player;

import java.io.File;

@UtilityClass
public class WorldUtils {

    public static boolean deleteWorld(String name) {
        File worldFolder = new File(Bukkit.getWorldContainer() + name);
        if (worldFolder.exists() && worldFolder.isDirectory()) {
            return worldFolder.delete();
        }
        return false;
    }

    public static void setupBorder(String worldName, double size) {
        setupBorder(Bukkit.getWorld(worldName), size);
    }

    public static void setupBorder(World world, double size) {
        if (world != null) {
            WorldBorder border = world.getWorldBorder();
            border.setCenter(0, 0);
            border.setSize(size);
        }
    }

}
