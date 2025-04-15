package org.clockworx.scrollteleportation.listeners;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.ItemStack;
import org.bukkit.scheduler.BukkitTask;
import org.clockworx.scrollteleportation.ScrollTeleportation;
import org.clockworx.scrollteleportation.files.LanguageString;
import org.clockworx.scrollteleportation.storage.Scroll;
import org.clockworx.scrollteleportation.tasks.TeleportRunnable;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.logging.Level;

/**
 * Listens for scroll activation events.
 * This class handles the interaction with scrolls and initiates teleportation.
 */
public class ActivateScrollListener implements Listener {

    private final ScrollTeleportation plugin;
    private final List<Material> ignoredBlocks = Arrays.asList(
            Material.CHEST,
            Material.TRAPPED_CHEST,
            Material.BARREL,
            Material.FURNACE,
            Material.BLAST_FURNACE,
            Material.SMOKER,
            Material.HOPPER,
            Material.DROPPER,
            Material.DISPENSER,
            Material.BREWING_STAND,
            Material.ANVIL,
            Material.GRINDSTONE,
            Material.STONECUTTER,
            Material.CRAFTING_TABLE,
            Material.ENCHANTING_TABLE,
            Material.LECTERN,
            Material.CARTOGRAPHY_TABLE,
            Material.LOOM,
            Material.SMITHING_TABLE
    );

    /**
     * Creates a new ActivateScrollListener instance.
     * 
     * @param plugin The plugin instance
     */
    public ActivateScrollListener(ScrollTeleportation plugin) {
        this.plugin = plugin;
    }

    /**
     * Handles player interaction events to activate scrolls.
     * 
     * @param event The interaction event
     */
    @EventHandler(priority = EventPriority.MONITOR)
    public void onInteract(PlayerInteractEvent event) {
        // Only handle right-click actions with the main hand
        if (event.getHand() != EquipmentSlot.HAND || event.getAction() != Action.RIGHT_CLICK_AIR && event.getAction() != Action.RIGHT_CLICK_BLOCK) {
            return;
        }

        // Check if the player clicked on an ignored block
        if (event.getAction() == Action.RIGHT_CLICK_BLOCK && event.getClickedBlock() != null) {
            if (ignoredBlocks.contains(event.getClickedBlock().getType())) {
                return;
            }
        }

        Player player = event.getPlayer();
        ItemStack item = player.getInventory().getItemInMainHand();

        // Check if the item is a scroll
        if (item == null || item.getType() != plugin.getMainConfig().getScrollMaterial() || !item.hasItemMeta()) {
            return;
        }

        // Get the scroll from the item
        Optional<Scroll> scrollOpt = plugin.getScrollStorage().getScrollByItemStack(item);
        if (scrollOpt.isEmpty()) {
            return;
        }

        Scroll scroll = scrollOpt.get();

        // Check if the player has permission to use the scroll
        if (!player.hasPermission("scrollteleportation.use." + scroll.getInternalName().toLowerCase())) {
            player.sendMessage(Component.text("You don't have permission to use this scroll!", NamedTextColor.RED));
            return;
        }

        // Check if the player is already teleporting
        if (plugin.getTeleportHandler().isReady(player)) {
            player.sendMessage(Component.text("You are already teleporting!", NamedTextColor.RED));
            return;
        }

        // Check if the player is in a blocked world
        if (plugin.getMainConfig().isWorldBlocked(player.getWorld().getName())) {
            player.sendMessage(Component.text("You cannot use scrolls in this world!", NamedTextColor.RED));
            return;
        }

        // Check if the player is in a blocked region
        if (plugin.getMainConfig().isRegionBlocked(player.getLocation())) {
            player.sendMessage(Component.text("You cannot use scrolls in this region!", NamedTextColor.RED));
            return;
        }

        // Check if the player is in combat
        if (plugin.getMainConfig().isCombatBlocked() && player.hasMetadata("inCombat")) {
            player.sendMessage(Component.text("You cannot use scrolls while in combat!", NamedTextColor.RED));
            return;
        }

        // Check if the player is in a vehicle
        if (player.isInsideVehicle()) {
            player.sendMessage(Component.text("You cannot use scrolls while in a vehicle!", NamedTextColor.RED));
            return;
        }

        // Check if the player is on an ignored block
        if (ignoredBlocks.contains(player.getLocation().getBlock().getType())) {
            player.sendMessage(Component.text("You cannot use scrolls while standing on this block!", NamedTextColor.RED));
            return;
        }

        // Set the player as ready to be teleported
        plugin.getTeleportHandler().setReady(player, true);

        // Get the destination location
        try {
            // Get the destination location
            Location destination = scroll.getDestination().getLocation();
            if (destination == null) {
                player.sendMessage(Component.text("Invalid destination!", NamedTextColor.RED));
                plugin.getTeleportHandler().setReady(player, false);
                return;
            }

            // Send a message to the player
            int delay = scroll.getTeleportDelay();
            if (delay > 0) {
                player.sendMessage(Component.text("Teleporting in ", NamedTextColor.GREEN)
                        .append(Component.text(delay, NamedTextColor.GOLD))
                        .append(Component.text(" seconds...", NamedTextColor.GREEN)));
            }

            // Schedule the teleportation
            BukkitTask task = new TeleportRunnable(plugin, destination, item, player)
                    .runTaskLater(plugin, delay * 20L);
            plugin.getTeleportHandler().setTaskID(player.getUniqueId(), task.getTaskId());

            // Cancel the event
            event.setCancelled(true);
        } catch (Exception e) {
            plugin.getLogger().log(Level.SEVERE, "Failed to activate scroll for player " + player.getName(), e);
            player.sendMessage(Component.text("An error occurred while activating the scroll!", NamedTextColor.RED));
            plugin.getTeleportHandler().setReady(player, false);
        }
    }
} 