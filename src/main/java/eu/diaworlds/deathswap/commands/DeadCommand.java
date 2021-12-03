package eu.diaworlds.deathswap.commands;

import eu.diaworlds.deathswap.Config;
import eu.diaworlds.deathswap.DeathSwap;
import eu.diaworlds.deathswap.commands.api.*;
import eu.diaworlds.deathswap.utils.Common;
import org.bukkit.Location;
import org.bukkit.entity.Player;

@CommandInfo(
        description = "Main DeathSwap command.",
        usage = "/deathswap <args>",
        permission = "dia.deathswap.admin",
        aliases = { "ds" }
)
public class DeadCommand extends DecentCommand {

    public DeadCommand() {
        super("deathswap");

        addSubCommand(new ReloadCommand());
        addSubCommand(new SetSpawnCommand());
    }

    @Override
    public CommandHandler getCommandHandler() {
        return (sender, args) -> {
            // Do nothing basically.
            Common.tell(sender, Config.parse(Config.NO_PERM));
            return true;
        };
    }

    @Override
    public TabCompleteHandler getTabCompleteHandler() {
        return null;
    }

    /*
     *  Reload
     */

    @CommandInfo(
            description = "Reload the plugins configuration.",
            usage = "/deathswap reload",
            permission = "dia.deathswap.admin"
    )
    public static class ReloadCommand extends DecentCommand {

        public ReloadCommand() {
            super("reload");
        }

        @Override
        public CommandHandler getCommandHandler() {
            return (sender, args) -> {
                DeathSwap.instance.reload();
                Common.tell(sender, Config.PREFIX + "&bSuccessfully reloaded!");
                return true;
            };
        }

        @Override
        public TabCompleteHandler getTabCompleteHandler() {
            return null;
        }
    }

    /*
     *  Set Spawn
     */

    @CommandInfo(
            description = "Set spawn command.",
            usage = "/deathswap setspawn",
            permission = "dia.deathswap.admin",
            playerOnly = true
    )
    public static class SetSpawnCommand extends DecentCommand {

        public SetSpawnCommand() {
            super("setspawn");
        }

        @Override
        public CommandHandler getCommandHandler() {
            return (sender, args) -> {
                Player player = CommandValidator.getPlayer(sender);
                if (player == null) return true;
                Location location = player.getLocation();
                // Set player's location as spawn.
                DeathSwap.instance.setSpawn(location, true);
                Common.tell(player, Config.PREFIX + "&bSpawn set!");
                return true;
            };
        }

        @Override
        public TabCompleteHandler getTabCompleteHandler() {
            return null;
        }
    }

}
