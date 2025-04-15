package org.clockworx.scrollteleportation.commands;

import org.clockworx.scrollteleportation.ScrollTeleportation;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;

import java.util.List;

public class ReloadCommand implements CommandExecutor, TabCompleter {

    private final ScrollTeleportation plugin;

    public ReloadCommand(ScrollTeleportation instance) {
        this.plugin = instance;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
        if (!sender.hasPermission("scrollteleportation.reload")) {
            sender.sendMessage(Component.text("You are not allowed to reload", NamedTextColor.RED));
            return true;
        }

        // Reload the config
        plugin.getMainConfig().reload();

        // Reload the scrolls
        plugin.getScrollStorage().loadScrollsFromConfig();
        
        sender.sendMessage(Component.text("Configuration file reloaded!", NamedTextColor.GREEN));
        return true;
    }

    @Override
    public List<String> onTabComplete(CommandSender commandSender, Command command, String s, String[] strings) {
        return null;
    }
} 