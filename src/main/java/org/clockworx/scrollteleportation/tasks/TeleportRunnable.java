package org.clockworx.scrollteleportation.tasks;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.scheduler.BukkitRunnable;
import org.clockworx.scrollteleportation.ScrollTeleportation;
import org.clockworx.scrollteleportation.files.LanguageString;

import java.util.logging.Level;

/**
 * Runnable task that handles the actual teleportation of a player.
 * This class is responsible for executing the teleportation after the delay period.
 */
public class TeleportRunnable extends BukkitRunnable {

    private final ScrollTeleportation plugin;
    private final Location destination;
    private final ItemStack scroll;
    private final Player player;

    /**
     * Creates a new TeleportRunnable instance.
     * 
     * @param plugin The plugin instance
     * @param destination The destination to teleport to
     * @param scroll The scroll item being used
     * @param player The player to teleport
     */
    public TeleportRunnable(ScrollTeleportation plugin, Location destination, ItemStack scroll, Player player) {
        this.plugin = plugin;
        this.destination = destination;
        this.scroll = scroll;
        this.player = player;
    }

    /**
     * Executes the teleportation task.
     * This method is called by the scheduler after the delay has passed.
     */
    @Override
    public void run() {
        if (!player.isOnline()) {
            plugin.getLogger().log(Level.WARNING, "Player " + player.getName() + " is no longer online, cancelling teleportation");
            return;
        }

        try {
            // Check if the player is still ready to be teleported
            if (!plugin.getTeleportHandler().isReady(player)) {
                player.sendMessage(Component.text("Teleportation cancelled.", NamedTextColor.YELLOW));
                return;
            }

            // Execute the teleportation
            plugin.getTeleportHandler().teleport(player, destination, scroll);
        } catch (Exception e) {
            plugin.getLogger().log(Level.SEVERE, "Failed to teleport player " + player.getName(), e);
            player.sendMessage(Component.text("An error occurred during teleportation!", NamedTextColor.RED));
            plugin.getTeleportHandler().setReady(player, false);
        }
    }
} 