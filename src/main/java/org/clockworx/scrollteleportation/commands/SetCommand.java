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

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class SetCommand implements CommandExecutor, TabCompleter {

    private final ScrollTeleportation plugin;
    private static final List<String> VALID_VARIABLES = List.of(
            "name", "delay", "uses", "destination_hidden", "destination", "cancel_on_move"
    );

    public SetCommand(ScrollTeleportation instance) {
        this.plugin = instance;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
        if (!sender.hasPermission("scrollteleportation.set")) {
            sender.sendMessage(Component.text("You are not allowed to set a variable", NamedTextColor.RED));
            return true;
        }

        if (args.length < 4 && !(args.length == 3 && args[1].equalsIgnoreCase("destination"))) {
            sender.sendMessage(Component.text("Incorrect command usage!", NamedTextColor.RED));
            sender.sendMessage(Component.text("Usage: /scroll set <variable> <scroll> <result>", NamedTextColor.YELLOW));
            return true;
        }

        String variable = args[1];
        Scroll scroll = plugin.getScrollStorage().getLoadedScroll(args[2]).orElse(null);

        if (scroll == null) {
            sender.sendMessage(Component.text("There is no scroll by that name!", NamedTextColor.RED));
            return true;
        }

        List<String> resultList = new ArrayList<>();
        for (int i = 3; i < args.length; i++) {
            resultList.add(args[i]);
        }
        String result = String.join(" ", resultList);

        switch (variable.toLowerCase()) {
            case "name" -> {
                plugin.getMainConfig().setName(scroll.getInternalName(), result);
                sender.sendMessage(createSuccessMessage("name", scroll.getInternalName(), result));
                return true;
            }
            case "delay" -> {
                try {
                    int delay = Integer.parseInt(result);
                    plugin.getMainConfig().setDelay(scroll.getInternalName(), delay);
                    sender.sendMessage(createSuccessMessage("delay", scroll.getInternalName(), result + " seconds"));
                    return true;
                } catch (NumberFormatException e) {
                    sender.sendMessage(Component.text("Delay number is not a valid number!", NamedTextColor.RED));
                    return true;
                }
            }
            case "uses" -> {
                try {
                    int uses = Integer.parseInt(result);
                    plugin.getMainConfig().setUses(scroll.getInternalName(), uses);
                    sender.sendMessage(createSuccessMessage("uses", scroll.getInternalName(), result));
                    return true;
                } catch (NumberFormatException e) {
                    sender.sendMessage(Component.text("Use number is not a valid number!", NamedTextColor.RED));
                    return true;
                }
            }
            case "destination_hidden" -> {
                boolean value = Boolean.parseBoolean(result);
                plugin.getMainConfig().setDestinationHidden(scroll.getInternalName(), value);
                sender.sendMessage(createSuccessMessage("destination_hidden", scroll.getInternalName(), String.valueOf(value)));
                return true;
            }
            case "cancel_on_move" -> {
                boolean value = Boolean.parseBoolean(result);
                plugin.getMainConfig().setCancelOnMove(scroll.getInternalName(), value);
                sender.sendMessage(createSuccessMessage("cancel_on_move", scroll.getInternalName(), String.valueOf(value)));
                return true;
            }
            case "destination" -> {
                if (!(sender instanceof Player player)) {
                    sender.sendMessage(Component.text("Cannot get your location!", NamedTextColor.RED));
                    return true;
                }
                plugin.getMainConfig().setDestination(scroll.getInternalName(), player.getLocation());
                sender.sendMessage(createSuccessMessage("destination", scroll.getInternalName(), "your location"));
                return true;
            }
            default -> {
                sender.sendMessage(Component.text("I don't recognise '" + variable + "' as a variable!", NamedTextColor.RED));
                sender.sendMessage(Component.text("You can only use: " + String.join(", ", VALID_VARIABLES), NamedTextColor.YELLOW));
                return true;
            }
        }
    }

    private Component createSuccessMessage(String variable, String scrollName, String value) {
        return Component.text("Set ", NamedTextColor.GREEN)
                .append(Component.text(variable, NamedTextColor.GOLD))
                .append(Component.text(" of ", NamedTextColor.GREEN))
                .append(Component.text(scrollName, NamedTextColor.GOLD))
                .append(Component.text(" to ", NamedTextColor.GREEN))
                .append(Component.text(value, NamedTextColor.GOLD));
    }

    @Override
    public List<String> onTabComplete(CommandSender commandSender, Command command, String s, String[] strings) {
        if (strings.length == 2) {
            return VALID_VARIABLES.stream()
                    .filter(option -> option.toLowerCase().startsWith(strings[1].toLowerCase()))
                    .collect(Collectors.toList());
        } else if (strings.length == 3) {
            return plugin.getScrollStorage().getLoadedScrolls().stream()
                    .map(Scroll::getInternalName)
                    .filter(scrollName -> scrollName.toLowerCase().startsWith(strings[2].trim().toLowerCase()))
                    .collect(Collectors.toList());
        }
        return null;
    }
} 