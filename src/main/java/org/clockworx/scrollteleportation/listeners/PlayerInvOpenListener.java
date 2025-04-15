package org.clockworx.scrollteleportation.listeners;

import org.clockworx.scrollteleportation.ScrollTeleportation;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryOpenEvent;
import org.bukkit.event.inventory.InventoryType;
import org.clockworx.scrollteleportation.teleporthandler.TeleportHandler;

/**
 * Listens for inventory open events to handle teleportation readiness.
 */
public class PlayerInvOpenListener implements Listener {

    private final ScrollTeleportation plugin;
    private final TeleportHandler teleportHandler;

    /**
     * Creates a new PlayerInvOpenListener instance.
     * 
     * @param plugin The plugin instance
     */
    public PlayerInvOpenListener(ScrollTeleportation plugin) {
        this.plugin = plugin;
        this.teleportHandler = plugin.getTeleportHandler();
    }

    /**
     * Handles inventory open events.
     * 
     * @param event The inventory open event
     */
    @EventHandler
    public void onInventoryOpen(InventoryOpenEvent event) {
        if (!(event.getPlayer() instanceof Player)) {
            return;
        }

        Player player = (Player) event.getPlayer();

        if (event.getInventory().getType() == InventoryType.PLAYER) {
            return;
        }

        if (!teleportHandler.isReady(player)) {
            return;
        }

        if (player.hasPermission("scrollteleportation.invbypass")) {
            return;
        }

        // Player has opened inventory so teleportation is cancelled
        teleportHandler.cancelTask(player);
        teleportHandler.setReady(player, false);

        // Inform player
        player.sendMessage(Component.text("Teleportation is cancelled because you opened an inventory.", NamedTextColor.RED));
    }
} 