package eu.diaworlds.deathswap.utils;

import lombok.experimental.UtilityClass;
import net.md_5.bungee.api.ChatColor;
import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;

import java.awt.*;
import java.util.List;
import java.util.logging.Level;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

@UtilityClass
public class Common {

	public static final Pattern RGB_REGEX = Pattern.compile("[<{&]?(#(?:[0-9a-fA-F]{3}){1,2})[}>]?");
	public static final Pattern GRADIENT_REGEX = Pattern.compile("[<{]#([A-Fa-f0-9]){6}[>}][^<]+[<{]/#([A-Fa-f0-9]){6}[>}]");

	public static int irand(int f, int t) {
		return f + (int) (Math.random() * ((t - f) + 1));
	}

	public static String formatSeconds(int seconds) {
		return String.format("%2d:%2d", seconds / 60, seconds % 60);
	}

	/*
	 * 	Colorize
	 */

	public static String colorize(String string) {
		try {
			Matcher matcher = RGB_REGEX.matcher(string);
			while (matcher.find()) {
				final ChatColor hexColor = Common.getChatColorFromHex(matcher.group(1));
				final String before = string.substring(0, matcher.start());
				final String after = string.substring(matcher.end());
				string = before + hexColor + after;
				matcher = RGB_REGEX.matcher(string);
			}
		} catch (Exception ignored) {}
		return ChatColor.translateAlternateColorCodes('&', string);
	}

	public static List<String> colorize(List<String> list) {
		return list.stream().map(Common::colorize).collect(Collectors.toList());
	}

	public static String colorizeGradients(String message) {
		message = Common.replaceGradients(message);
		return Common.colorize(message);
	}

	/*
	 * 	Text
	 */

	/**
	 * Colorize a String and format it with given arguments.
	 *
	 * @param text String to "Textify".
	 * @param args Arguments to format.
	 * @return The colorized and formatted String.
	 */
	public static String text(String text, Object... args) {
		return Common.colorize(String.format(text, args));
	}

	/**
	 * Trasform the first letters in words to uppercase and the trailing letters to lowercase.
	 *
	 * @param text The text to trasform.
	 * @return Transformed text.
	 */
	public static String titleCase(String text) {
		if (text != null) {
			String lowerText = text.toLowerCase();
			String[] textBuild = lowerText.split(" ");
			String newText = "";
			byte b;
			int i;
			String[] arrayOfString1;
			for (i = (arrayOfString1 = textBuild).length, b = 0; b < i; ) {
				String textPart = arrayOfString1[b++];
				String first = String.valueOf(textPart.charAt(0));
				textPart = textPart.replaceFirst(first, first.toUpperCase());
				newText = newText.isEmpty() ? textPart : (newText + " " + textPart);
			}
			return newText;
		}
		return "";
	}

	/**
	 * Trasform the first letters in words to uppercase and the trailing letters to lowercase.
	 *
	 * @param text The text to trasform.
	 * @param split String that will be used as a word splitter.
	 * @return Transformed text.
	 */
	public static String titleCaseSplit(String text, String split) {
		return titleCase(String.join(" ", text.split(split)));
	}

	/*
	 * 	Log
	 */

	/**
	 * Log a message into console.
	 *
	 * @param message The message.
	 */
	public static void log(String message) {
		log(Level.INFO, message);
	}

	/**
	 * Log a message into console.
	 * <p>
	 *     This method formats given arguments in the message.
	 * </p>
	 *
	 * @param message The message.
	 * @param args The arguments
	 */
	public static void log(String message, Object... args) {
		log(String.format(message, args));
	}

	/**
	 * Log a message into console.
	 *
	 * @param level Level of this message.
	 * @param message The message.
	 */
	public static void log(Level level, String message) {
		Bukkit.getServer().getLogger().log(level, String.format("[DiaDeathSwap] %s", message));
	}

	/**
	 * Log a message into console.
	 * <p>
	 *     This method formats given arguments in the message.
	 * </p>
	 *
	 * @param level Level of this message.
	 * @param message The message.
	 * @param args The arguments.
	 */
	public static void log(Level level, String message, Object... args) {
		log(level, String.format(message, args));
	}

	/*
	 * 	Debug
	 */

	/**
	 * Print an object into console.
	 *
	 * @param o Object to print.
	 */
	public static void debug(Object o) {
		System.out.println(o);
	}

	/*
	 * 	Tell
	 */

	/**
	 * Send a message to given CommandSender.
	 * <p>
	 *     This method will colorize the message.
	 * </p>
	 *
	 * @param player The CommandSender receiving the message.
	 * @param message The message.
	 */
	public static void tell(CommandSender player, String message) {
		player.sendMessage(colorize(message));
	}

	/**
	 * Send a message to given CommandSender.
	 * <p>
	 *     This method will colorize the message and formats given arguments to the message.
	 * </p>
	 *
	 * @param player The CommandSender receiving the message.
	 * @param message The message.
	 * @param args The arguments.
	 */
	public static void tell(CommandSender player, String message, Object... args) {
		tell(player, String.format(message, args));
	}

	/*
	 *	Utility Methods
	 */

	private static String replaceGradients(String message) {
		try {
			Matcher gradientMatcher = GRADIENT_REGEX.matcher(message);
			while (gradientMatcher.find()) {
				String group = gradientMatcher.group();
				ChatColor startColor = Common.getChatColorFromHex(group.substring(1, 8));
				ChatColor endColor = Common.getChatColorFromHex(group.substring(group.length() - 8, group.length() - 1));
				String gradient = asGradient(group.substring(9, group.length() - 10), startColor, endColor);
				message = message.replace(group, gradient);
			}
		} catch (Exception ignored) {}
		return message;
	}

	private static String asGradient(String text, ChatColor startColor, ChatColor endColor) {
		StringBuilder stringBuilder = new StringBuilder();
		Color start = startColor.getColor();
		Color end = endColor.getColor();
		char[] textArray = text.toCharArray();
		int length = textArray.length;

		for (int i = 0; i < length; i++) {
			int red = (int) (start.getRed() + (float) (end.getRed() - start.getRed()) / (length - 1) * i);
			int green = (int) (start.getGreen() + (float) (end.getGreen() - start.getGreen()) / (length - 1) * i);
			int blue = (int) (start.getBlue() + (float) (end.getBlue() - start.getBlue()) / (length - 1) * i);
			stringBuilder.append(String.format("<#%s>%c", Common.toHexString(red, green, blue), textArray[i]));
		}
		return stringBuilder.toString();
	}

	private static String toHexString(int red, int green, int blue) {
		StringBuilder stringBuilder = new StringBuilder(Integer.toHexString((red << 16) + (green << 8) + blue));
		while (stringBuilder.length() < 6) {
			stringBuilder.insert(0, 0);
		}
		return stringBuilder.toString();
	}

	private static ChatColor getChatColorFromHex(String hex) {
		return ChatColor.of(hex);
	}

}
