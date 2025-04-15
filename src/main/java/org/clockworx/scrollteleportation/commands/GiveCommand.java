package org.clockworx.scrollteleportation.commands;

import org.clockworx.scrollteleportation.ScrollTeleportation;
import org.clockworx.scrollteleportation.storage.Scroll;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import java.util.List;
import java.util.stream.Collectors;

public class GiveCommand implements CommandExecutor, TabCompleter {

    private final ScrollTeleportation plugin;

    public GiveCommand(ScrollTeleportation instance) {
        this.plugin = instance;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
        if (!sender.hasPermission("scrollteleportation.give")) {
            sender.sendMessage(Component.text("You are not allowed to give scrolls", NamedTextColor.RED));
            return true;
        }

        if (args.length < 2) {
            sender.sendMessage(Component.text("Incorrect command usage!", NamedTextColor.RED));
            sender.sendMessage(Component.text("Usage: /scroll give <scroll> (name)", NamedTextColor.YELLOW));
            return true;
        }

        Scroll scroll = plugin.getScrollStorage().getLoadedScroll(args[1]).orElse(null);

        if (scroll == null) {
            sender.sendMessage(Component.text("No scroll found with that name.", NamedTextColor.RED));
            return true;
        }

        Player target;
        
        // No target given
        if (args.length == 2) {
            if (!(sender instanceof Player)) {
                sender.sendMessage(Component.text("You are not a player!", NamedTextColor.RED));
                return true;
            }
            target = (Player) sender;
        } else if (args.length == 3) {
            // Target given
            target = plugin.getServer().getPlayer(args[2]);
            if (target == null) {
                sender.sendMessage(Component.text("There is no player with that name online!", NamedTextColor.RED));
                return true;
            }
        } else {
            sender.sendMessage(Component.text("Incorrect command usage!", NamedTextColor.RED));
            sender.sendMessage(Component.text("Usage: /scroll give <scroll> (name)", NamedTextColor.YELLOW));
            return true;
        }

        ItemStack itemStack = scroll.getItemStack();
        target.getInventory().addItem(itemStack);

        // Notify target
        target.sendMessage(Component.text("You have been given a ", NamedTextColor.GREEN)
                .append(Component.text(scroll.getDisplayName(), NamedTextColor.GOLD)));

        // Notify sender if giving to someone else
        if (args.length == 3 && !target.getName().equalsIgnoreCase(sender.getName())) {
            sender.sendMessage(Component.text("You have given ", NamedTextColor.GREEN)
                    .append(Component.text(target.getName(), NamedTextColor.GOLD))
                    .append(Component.text(" a ", NamedTextColor.GREEN))
                    .append(Component.text(scroll.getDisplayName(), NamedTextColor.GOLD)));
        }

        return true;
    }

    @Override
    public List<String> onTabComplete(CommandSender commandSender, Command command, String s, String[] strings) {
        if (strings.length == 2) {
            return plugin.getScrollStorage().getLoadedScrolls().stream()
                    .map(Scroll::getInternalName)
                    .filter(scrollName -> scrollName.toLowerCase().startsWith(strings[1].toLowerCase()))
                    .collect(Collectors.toList());
        } else if (strings.length == 3) {
            return plugin.getServer().getOnlinePlayers().stream()
                    .map(Player::getName)
                    .collect(Collectors.toList());
        }
        return null;
    }
} 