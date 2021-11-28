package eu.diaworlds.deathswap.utils;

import lombok.NonNull;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.World;

public class Locations {

    public static String asString(@NonNull Location l, boolean includeYawPitch) {
        if (l.getWorld() == null) {
            return null;
        }

        String location = String.format("%s:%.3f:%.3f:%.3f", l.getWorld().getName(), l.getX(), l.getY(), l.getZ());
        if (includeYawPitch) {
            location += String.format(":%.3f:%.3f", l.getYaw(), l.getPitch());
        }
        return location;
    }

    public static Location asLocation(String string) {
        if (string == null || string.trim().isEmpty()) return null;
        String[] spl = string.replace(",", ".").split(":");
        Location location;
        if (spl.length >= 4) {
            World world = Bukkit.getWorld(spl[0]);
            if (world != null) {
                try {
                    location = new Location(world, Double.parseDouble(spl[1]), Double.parseDouble(spl[2]), Double.parseDouble(spl[3]));
                    if (spl.length >= 6) {
                        location.setYaw(Float.parseFloat(spl[4]));
                        location.setPitch(Float.parseFloat(spl[5]));
                    }
                    return location;
                } catch (NumberFormatException e) {
                    e.printStackTrace();
                }
            }
            Common.log(String.format("World '%s' not found.", spl[0]));
            return null;
        }
        Common.log(String.format("Wrong location format: %s", string));
        return null;
    }

}
