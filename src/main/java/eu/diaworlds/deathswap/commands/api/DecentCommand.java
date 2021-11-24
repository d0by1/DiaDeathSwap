package eu.diaworlds.deathswap.commands.api;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.Lists;
import eu.diaworlds.deathswap.utils.Common;
import org.apache.commons.lang.Validate;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.util.StringUtil;

import java.util.*;
import java.util.stream.Collectors;

public abstract class DecentCommand extends Command implements CommandBase {

	protected final Map<String, CommandBase> subCommands = new LinkedHashMap<>();
	protected final CommandInfo info;

	public DecentCommand(String name) {
		super(name);
		this.info = getClass().getAnnotation(CommandInfo.class);
		if (info == null) {
			throw new RuntimeException(String.format("Command %s is not annotated with @CommandInfo.", name));
		}
		this.setAliases(Arrays.asList(info.aliases()));
	}

	@Override
	public Set<String> getSubCommandNames() {
		return null;
	}

	@Override
	public Collection<CommandBase> getSubCommands() {
		return subCommands.values();
	}

	@Override
	public CommandBase getSubCommand(String name) {
		Validate.notNull(name);
		return subCommands.get(name);
	}

	@Override
	public CommandBase addSubCommand(CommandBase commandBase) {
		subCommands.put(commandBase.getName(), commandBase);
		return this;
	}

	@Override
	public boolean execute(CommandSender sender, String s, String[] args) {
		try {
			return this.handle(sender, args);
		} catch (DecentCommandException e) {
			Common.tell(sender, e.getMessage());
			return true;
		}
	}

	@Override
	public List<String> tabComplete(CommandSender sender, String alias, String[] args) throws IllegalArgumentException {
		return handeTabComplete(sender, args);
	}

	@Override
	public String getPermission() {
		return info.permission();
	}

	@Override
	public boolean isPlayerOnly() {
		return info.playerOnly();
	}

	@Override
	public int getMinArgs() {
		return info.minArgs();
	}

	@Override
	public String getUsage() {
		return info.usage();
	}

	@Override
	public String getDescription() {
		return info.description();
	}

	/**
	 * Handle the Command.
	 *
	 * @param sender The sender.
	 * @param args The arguments.
	 * @return Boolean whether the execution was successful.
	 */
	protected final boolean handle(CommandSender sender, String[] args) throws DecentCommandException {
		if (!CommandValidator.canExecute(sender, this)) {
			return true;
		}

		if (args.length != 0) {
			for (CommandBase subCommand : getSubCommands()) {
				if (CommandValidator.isIdentifier(args[0], subCommand)) {
					final String[] subCommandArgs = Arrays.copyOfRange(args, 1, args.length);
					if (subCommandArgs.length < subCommand.getMinArgs()) {
						Common.tell(sender, subCommand.getUsage());
						return true;
					}
					return ((DecentCommand) subCommand).handle(sender, subCommandArgs);
				}
			}
		} else if (getMinArgs() > 0) {
			Common.tell(sender, getUsage());
			return false;
		}

		return this.getCommandHandler().handle(sender, args);
	}

	/**
	 * Handle Tab Complete of the Command.
	 *
	 * @param sender The sender.
	 * @param args The arguments.
	 * @return List of tab completed Strings.
	 */
	protected final List<String> handeTabComplete(CommandSender sender, String[] args) {
		if (getPermission() != null && !sender.hasPermission(getPermission())) {
			return ImmutableList.of();
		}

		if (args.length == 1) {
			List<String> matches = Lists.newLinkedList();
			List<String> subs = getSubCommands().stream()
					.map(CommandBase::getName)
					.collect(Collectors.toList());
			getSubCommands().forEach(sub ->
					subs.addAll(Lists.newArrayList(sub.getAliases()))
			);

			StringUtil.copyPartialMatches(args[0], subs, matches);

			if (!matches.isEmpty()) {
				Collections.sort(matches);
				return matches;
			}
		} else if (args.length > 1) {
			for (CommandBase subCommand : getSubCommands()) {
				if (CommandValidator.isIdentifier(args[0], subCommand)) {
					return ((DecentCommand) subCommand).handeTabComplete(sender, Arrays.copyOfRange(args, 1, args.length));
				}
			}
		}

		if (this.getTabCompleteHandler() == null) {
			return ImmutableList.of();
		}

		return this.getTabCompleteHandler().handleTabComplete(sender, args);
	}

}
