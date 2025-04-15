package org.clockworx.scrollteleportation.commands;

import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;
import org.clockworx.scrollteleportation.ScrollTeleportation;
import org.clockworx.scrollteleportation.files.LanguageString;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.TextComponent;

import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;

/**
 * Handles command execution for the ScrollTeleportation plugin.
 * This class manages all plugin commands and their tab completion.
 */
public class CommandHandler implements CommandExecutor, TabCompleter {

    private final ScrollTeleportation plugin;

    /**
     * Creates a new CommandHandler instance.
     * 
     * @param plugin The plugin instance
     */
    public CommandHandler(ScrollTeleportation plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (args.length == 0) {
            sendHelp(sender);
            return true;
        }

        switch (args[0].toLowerCase()) {
            case "reload":
                if (!sender.hasPermission("scrollteleportation.reload")) {
                    sender.sendMessage(LanguageString.NO_PERMISSION.parse());
                    return true;
                }
                reloadPlugin(sender);
                break;
            case "give":
                if (!sender.hasPermission("scrollteleportation.give")) {
                    sender.sendMessage(LanguageString.NO_PERMISSION.parse());
                    return true;
                }
                if (args.length < 2) {
                    sender.sendMessage(LanguageString.INVALID_COMMAND.parse());
                    return true;
                }
                giveScroll(sender, args);
                break;
            default:
                sendHelp(sender);
                break;
        }

        return true;
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String alias, String[] args) {
        List<String> completions = new ArrayList<>();

        if (args.length == 1) {
            if (sender.hasPermission("scrollteleportation.reload")) {
                completions.add("reload");
            }
            if (sender.hasPermission("scrollteleportation.give")) {
                completions.add("give");
            }
            return completions;
        }

        if (args.length == 2 && args[0].equalsIgnoreCase("give")) {
            if (sender.hasPermission("scrollteleportation.give")) {
                completions.addAll(plugin.getScrollStorage().getScrollNames());
            }
            return completions;
        }

        if (args.length == 3 && args[0].equalsIgnoreCase("give")) {
            if (sender.hasPermission("scrollteleportation.give")) {
                completions.add("<amount>");
            }
            return completions;
        }

        return completions;
    }

    /**
     * Sends the help message to the command sender.
     * 
     * @param sender The command sender
     */
    private void sendHelp(CommandSender sender) {
        sender.sendMessage(LanguageString.HELP_HEADER.parse());
        if (sender.hasPermission("scrollteleportation.reload")) {
            sender.sendMessage(LanguageString.HELP_RELOAD.parse());
        }
        if (sender.hasPermission("scrollteleportation.give")) {
            sender.sendMessage(LanguageString.HELP_GIVE.parse());
        }
    }

    /**
     * Reloads the plugin configuration.
     * 
     * @param sender The command sender
     */
    private void reloadPlugin(CommandSender sender) {
        try {
            plugin.reloadConfig();
            plugin.getMainConfig().reload();
            sender.sendMessage(LanguageString.RELOAD_SUCCESS.parse());
        } catch (Exception e) {
            plugin.getLogger().log(Level.SEVERE, "Failed to reload plugin configuration", e);
            sender.sendMessage(LanguageString.RELOAD_FAILED.parse());
        }
    }

    /**
     * Gives a scroll to a player.
     * 
     * @param sender The command sender
     * @param args The command arguments
     */
    private void giveScroll(CommandSender sender, String[] args) {
        if (args.length < 3) {
            sender.sendMessage(LanguageString.INVALID_COMMAND.parse());
            return;
        }

        Player target = plugin.getServer().getPlayer(args[1]);
        if (target == null) {
            sender.sendMessage(LanguageString.PLAYER_NOT_FOUND.parse());
            return;
        }

        try {
            int amount = Integer.parseInt(args[2]);
            if (amount <= 0) {
                sender.sendMessage(LanguageString.INVALID_AMOUNT.parse());
                return;
            }

            String scrollName = args[1];
            plugin.getScrollStorage().giveScrollToPlayer(target, scrollName);
            Component message = plugin.getMainConfig().getTranslatableMessage(LanguageString.GIVE_SUCCESS);
            message = message.replaceText(builder -> builder.match("%player%").replacement(target.getName()));
            message = message.replaceText(builder -> builder.match("%scroll%").replacement(scrollName));
            sender.sendMessage(message);
        } catch (NumberFormatException e) {
            sender.sendMessage(LanguageString.INVALID_AMOUNT.parse());
        }
    }
} 