package org.clockworx.scrollteleportation.commands;

import org.clockworx.scrollteleportation.ScrollTeleportation;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;

import java.util.List;

public class HelpCommand implements CommandExecutor, TabCompleter {

    private final ScrollTeleportation plugin;

    public HelpCommand(ScrollTeleportation instance) {
        this.plugin = instance;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
        if (args.length == 1) {
            showHelpPage(1, sender);
        } else {
            try {
                int page = Integer.parseInt(args[1]);
                showHelpPage(page, sender);
            } catch (NumberFormatException e) {
                sender.sendMessage(Component.text(args[1] + " is not a valid page number!", NamedTextColor.RED));
                return true;
            }
        }

        return true;
    }

    private void showHelpPage(int page, CommandSender sender) {
        int maximumPages = 1;
        
        Component header = Component.text("--------------[", NamedTextColor.BLUE)
                .append(Component.text("Scroll Teleportation", NamedTextColor.GOLD))
                .append(Component.text("]------------------", NamedTextColor.BLUE));
                
        sender.sendMessage(header);
        
        // Command list
        sender.sendMessage(createCommandHelp("/scroll", "Shows basic information"));
        sender.sendMessage(createCommandHelp("/scroll help", "Shows a list of commands"));
        sender.sendMessage(createCommandHelp("/scroll reload", "Reload Scroll Teleportation"));
        sender.sendMessage(createCommandHelp("/scroll give <scroll> (player)", "Give a scroll to a player"));
        sender.sendMessage(createCommandHelp("/scroll create <scroll> <displayName> <delay> <uses>", 
                "Create a new scroll with a <delay>, <uses> and a destination at your location"));
        sender.sendMessage(createCommandHelp("/scroll set <var> <scroll> <result>", "Set a scroll variable"));
        
        // Page indicator
        sender.sendMessage(Component.text("Page ", NamedTextColor.GOLD)
                .append(Component.text("1", NamedTextColor.BLUE))
                .append(Component.text(" of ", NamedTextColor.GOLD))
                .append(Component.text(String.valueOf(maximumPages), NamedTextColor.BLUE)));
    }

    private Component createCommandHelp(String command, String description) {
        return Component.text(command, NamedTextColor.GOLD)
                .append(Component.text(" --- ", NamedTextColor.BLUE))
                .append(Component.text(description, NamedTextColor.BLUE));
    }

    @Override
    public List<String> onTabComplete(CommandSender commandSender, Command command, String s, String[] strings) {
        return null;
    }
} 