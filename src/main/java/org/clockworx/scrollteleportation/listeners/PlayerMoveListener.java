package org.clockworx.scrollteleportation.listeners;

import org.clockworx.scrollteleportation.ScrollTeleportation;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerMoveEvent;
import org.clockworx.scrollteleportation.teleporthandler.TeleportHandler;
import org.clockworx.scrollteleportation.files.LanguageString;

/**
 * Listens for player movement events to cancel teleportation if the player moves.
 * This class is responsible for ensuring players stay still during the teleportation delay.
 */
public class PlayerMoveListener implements Listener {

    private final ScrollTeleportation plugin;

    /**
     * Creates a new PlayerMoveListener instance.
     * 
     * @param plugin The plugin instance
     */
    public PlayerMoveListener(ScrollTeleportation plugin) {
        this.plugin = plugin;
    }

    /**
     * Handles player movement events.
     * Cancels teleportation if the player moves during the teleportation delay.
     * 
     * @param event The player move event
     */
    @EventHandler(priority = EventPriority.MONITOR)
    public void onPlayerMove(PlayerMoveEvent event) {
        Player player = event.getPlayer();

        // Check if player is ready to be teleported
        if (!plugin.getTeleportHandler().isReady(player)) {
            return;
        }

        // Check if player has moved from their initial position
        if (event.getFrom().getX() != event.getTo().getX() || 
            event.getFrom().getY() != event.getTo().getY() || 
            event.getFrom().getZ() != event.getTo().getZ()) {
            
            // Cancel teleportation
            plugin.getTeleportHandler().setReady(player, false);
            player.sendMessage(plugin.getMainConfig().getTranslatableMessage(LanguageString.TELEPORT_CANCELLED_MOVEMENT));
        }
    }
} 