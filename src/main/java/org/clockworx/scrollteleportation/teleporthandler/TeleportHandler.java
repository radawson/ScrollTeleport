package org.clockworx.scrollteleportation.teleporthandler;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.scheduler.BukkitTask;
import org.clockworx.scrollteleportation.ScrollTeleportation;
import org.clockworx.scrollteleportation.storage.Scroll;
import org.clockworx.scrollteleportation.storage.ScrollStorage;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.logging.Level;

/**
 * Handles teleportation tasks for the plugin.
 * This class manages the teleportation process, including delays and effects.
 */
public class TeleportHandler {

    private final ScrollTeleportation plugin;
    private final Map<UUID, Integer> taskIDs;
    private final Map<UUID, Boolean> readyPlayers;

    /**
     * Creates a new TeleportHandler instance.
     * 
     * @param plugin The plugin instance
     */
    public TeleportHandler(ScrollTeleportation plugin) {
        this.plugin = plugin;
        this.taskIDs = new HashMap<>();
        this.readyPlayers = new HashMap<>();
    }

    /**
     * Checks if a player is ready to be teleported.
     * 
     * @param player The player to check
     * @return True if the player is ready
     */
    public boolean isReady(Player player) {
        return readyPlayers.getOrDefault(player.getUniqueId(), false);
    }

    /**
     * Sets whether a player is ready to be teleported.
     * 
     * @param player The player to set
     * @param ready Whether the player is ready
     */
    public void setReady(Player player, boolean ready) {
        readyPlayers.put(player.getUniqueId(), ready);
    }

    /**
     * Teleports a player to a location.
     * 
     * @param player The player to teleport
     * @param location The location to teleport to
     * @param scrollItem The scroll item being used
     */
    public void teleport(Player player, Location location, ItemStack scrollItem) {
        if (location == null) {
            player.sendMessage(Component.text("Invalid destination!", NamedTextColor.RED));
            return;
        }

        try {
            // Secure the location to prevent suffocation or falling
            location = secureLocation(location);
            
            // Teleport the player
            player.teleport(location);
            
            // Apply effects from the scroll
            Optional<Scroll> scrollOpt = plugin.getScrollStorage().getScrollByItemStack(scrollItem);
            if (scrollOpt.isPresent()) {
                Scroll scroll = scrollOpt.get();
                
                // Apply effects if the player doesn't have the bypass permission
                if (!player.hasPermission("scrollteleportation.bypass.effects")) {
                    scroll.applyEffects(player);
                }
                
                // Decrease the number of uses
                decreaseUse(scrollItem);
            }
            
            // Send a success message
            player.sendMessage(Component.text("You have been teleported!", NamedTextColor.GREEN));
            
            // Set the player as not ready
            setReady(player, false);
        } catch (Exception e) {
            plugin.getLogger().log(Level.SEVERE, "Failed to teleport player " + player.getName(), e);
            player.sendMessage(Component.text("An error occurred during teleportation!", NamedTextColor.RED));
            setReady(player, false);
        }
    }

    /**
     * Secures a location to prevent suffocation or falling.
     * 
     * @param location The location to secure
     * @return The secured location
     */
    private Location secureLocation(Location location) {
        if (location == null) {
            return null;
        }

        // Find the highest block at the location
        int highestY = location.getWorld().getHighestBlockYAt(location.getBlockX(), location.getBlockZ());
        
        // Create a new location at the highest block + 1
        return new Location(
            location.getWorld(),
            location.getX(),
            highestY + 1,
            location.getZ(),
            location.getYaw(),
            location.getPitch()
        );
    }

    /**
     * Decreases the number of uses for a scroll item.
     * 
     * @param item The scroll item
     */
    private void decreaseUse(ItemStack item) {
        if (item == null || !item.hasItemMeta()) {
            return;
        }

        // Get the current number of uses
        int currentUses = Scroll.getCurrentUses(item);
        
        // If the scroll has infinite uses, don't decrease
        if (currentUses < 0) {
            return;
        }
        
        // Decrease the number of uses
        Scroll.setCurrentUses(item, currentUses - 1);
        
        // If the scroll has no more uses, remove it
        if (currentUses - 1 <= 0) {
            item.setAmount(0);
        }
    }

    /**
     * Sets the task ID for a player.
     * 
     * @param player The player to set the task ID for
     * @param taskId The task ID
     */
    public void setTaskID(Player player, Integer taskId) {
        taskIDs.put(player.getUniqueId(), taskId);
    }

    /**
     * Gets the task ID for a player.
     * 
     * @param player The player
     * @return The task ID
     */
    public Integer getTaskID(Player player) {
        return taskIDs.get(player.getUniqueId());
    }

    /**
     * Cancels a teleportation task.
     * 
     * @param player The player
     */
    public void cancelTask(Player player) {
        Integer taskId = getTaskID(player);
        if (taskId != null) {
            Bukkit.getScheduler().cancelTask(taskId);
            taskIDs.remove(player.getUniqueId());
        }
    }

    /**
     * Cleans up a player's teleportation state.
     * 
     * @param player The player to clean up
     */
    public void cleanup(Player player) {
        readyPlayers.remove(player.getUniqueId());
        taskIDs.remove(player.getUniqueId());
    }
} 