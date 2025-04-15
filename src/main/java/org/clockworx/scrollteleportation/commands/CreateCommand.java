package org.clockworx.scrollteleportation.commands;

import org.clockworx.scrollteleportation.ScrollTeleportation;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;

import java.util.List;

public class CreateCommand implements CommandExecutor, TabCompleter {

    private final ScrollTeleportation plugin;

    public CreateCommand(ScrollTeleportation instance) {
        this.plugin = instance;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
        if (!sender.hasPermission("scrollteleportation.create")) {
            sender.sendMessage(Component.text("You are not allowed to create scrolls", NamedTextColor.RED));
            return true;
        }

        if (!(sender instanceof Player)) {
            sender.sendMessage(Component.text("Only players can perform this command!", NamedTextColor.RED));
            return true;
        }

        if (args.length != 5) {
            sender.sendMessage(Component.text("Invalid command usage!", NamedTextColor.RED));
            sender.sendMessage(Component.text("Usage: /scroll create <name> <displayName> <delay> <uses>", NamedTextColor.YELLOW));
            return true;
        }

        Player player = (Player) sender;
        String scroll = args[1];
        String scrollName = args[2];
        
        int delay;
        int uses;
        
        try {
            delay = Integer.parseInt(args[3]);
        } catch (NumberFormatException e) {
            sender.sendMessage(Component.text("Invalid delay time!", NamedTextColor.RED));
            return true;
        }

        try {
            uses = Integer.parseInt(args[4]);
        } catch (NumberFormatException e) {
            sender.sendMessage(Component.text("Invalid uses!", NamedTextColor.RED));
            return true;
        }

        if (plugin.getMainConfig().createNewScroll(scroll, scrollName, player.getLocation(), delay, uses)) {
            Component successMessage = Component.text("Successfully created new scroll with ", NamedTextColor.GREEN)
                    .append(Component.text(delay, NamedTextColor.GOLD))
                    .append(Component.text(" seconds delay and ", NamedTextColor.GREEN))
                    .append(Component.text(uses, NamedTextColor.GOLD))
                    .append(Component.text(" uses and with a destination at ", NamedTextColor.GREEN))
                    .append(Component.text("your location", NamedTextColor.GOLD))
                    .append(Component.text("!", NamedTextColor.GREEN));
            sender.sendMessage(successMessage);
        } else {
            sender.sendMessage(Component.text("Scroll already exists!", NamedTextColor.RED));
        }
        return true;
    }

    @Override
    public List<String> onTabComplete(CommandSender commandSender, Command command, String s, String[] strings) {
        return null;
    }
} 