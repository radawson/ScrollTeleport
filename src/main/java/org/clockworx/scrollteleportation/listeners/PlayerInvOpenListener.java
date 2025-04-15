package org.clockworx.scrollteleportation.listeners;

import org.clockworx.scrollteleportation.ScrollTeleportation;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryOpenEvent;
import org.bukkit.event.inventory.InventoryType;

public class PlayerInvOpenListener implements Listener {

    private final ScrollTeleportation plugin;

    public PlayerInvOpenListener(ScrollTeleportation instance) {
        this.plugin = instance;
    }

    @EventHandler
    public void onInventoryOpen(InventoryOpenEvent event) {
        if (!(event.getPlayer() instanceof Player player)) {
            return;
        }

        if (event.getInventory().getType() == InventoryType.PLAYER) {
            return;
        }

        if (!plugin.getTeleportHandler().isReady(player.getName())) {
            return;
        }

        if (player.hasPermission("scrollteleportation.invbypass")) {
            return;
        }

        // Player has opened inventory so teleportation is cancelled
        plugin.getTeleportHandler().setReady(player.getName(), false);

        Integer taskId = plugin.getTeleportHandler().taskID.get(player.getName());
        if (taskId != null) {
            // Cancel teleport task
            plugin.getServer().getScheduler().cancelTask(taskId);

            // Set taskID null
            plugin.getTeleportHandler().taskID.put(player.getName(), null);
        }

        // Inform player
        player.sendMessage(Component.text("Teleportation is cancelled because you opened an inventory.", NamedTextColor.RED));
    }
} 