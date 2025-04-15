package org.clockworx.scrollteleportation.storage;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.NamespacedKey;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.persistence.PersistentDataType;
import org.clockworx.scrollteleportation.ScrollTeleportation;
import org.clockworx.scrollteleportation.exceptions.ScrollInvalidException;
import org.clockworx.scrollteleportation.files.MainConfig;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.logging.Level;

/**
 * Manages the storage and retrieval of scrolls in the plugin.
 * This class handles loading scrolls from configuration and providing access to them.
 */
public class ScrollStorage {

    private final ScrollTeleportation plugin;
    private final List<Scroll> loadedScrolls = new ArrayList<>();

    /**
     * Creates a new ScrollStorage instance.
     * 
     * @param plugin The plugin instance
     */
    public ScrollStorage(ScrollTeleportation plugin) {
        this.plugin = plugin;
    }

    /**
     * Adds a scroll to the loaded scrolls list.
     * 
     * @param scroll The scroll to add
     * @throws ScrollInvalidException if the scroll is null or already exists
     */
    public void addLoadedScroll(Scroll scroll) throws ScrollInvalidException {
        if (scroll == null) {
            throw new ScrollInvalidException("Cannot add null scroll");
        }
        if (loadedScrolls.contains(scroll)) {
            throw new ScrollInvalidException("Scroll already exists: " + scroll.getInternalName());
        }
        loadedScrolls.add(scroll);
    }

    /**
     * Removes a scroll by its name.
     * 
     * @param scrollName The name of the scroll to remove
     */
    public void removeLoadedScroll(String scrollName) {
        loadedScrolls.removeIf(scroll -> scroll.getInternalName().equalsIgnoreCase(scrollName) 
            || scroll.getDisplayName().equalsIgnoreCase(scrollName));
    }

    /**
     * Gets a loaded scroll by its name.
     * 
     * @param scrollName The name of the scroll to get
     * @return An Optional containing the scroll if found
     */
    public Optional<Scroll> getLoadedScroll(String scrollName) {
        return loadedScrolls.stream()
            .filter(scroll -> scroll.getInternalName().equalsIgnoreCase(scrollName) 
                || scroll.getDisplayName().equalsIgnoreCase(scrollName))
            .findFirst();
    }

    /**
     * Gets all loaded scrolls.
     * 
     * @return The list of loaded scrolls
     */
    public List<Scroll> getLoadedScrolls() {
        return new ArrayList<>(loadedScrolls);
    }

    /**
     * Gets the names of all loaded scrolls.
     * 
     * @return The list of scroll names
     */
    public List<String> getScrollNames() {
        List<String> names = new ArrayList<>();
        for (Scroll scroll : loadedScrolls) {
            names.add(scroll.getDisplayName());
        }
        return names;
    }

    /**
     * Loads all scrolls from the config.
     * 
     * @return true if scrolls were loaded successfully
     */
    public boolean loadScrollsFromConfig() {
        // First clear all scrolls
        loadedScrolls.clear();

        MainConfig mainConfig = plugin.getMainConfig();

        // Go over each entry in the config and load the scroll in memory
        for (String internalScrollName : mainConfig.getScrollsInConfig()) {
            try {
                Scroll scroll = new Scroll(internalScrollName);

                scroll.setDisplayName(mainConfig.getScrollDisplayName(internalScrollName));
                scroll.setDescriptionLore(mainConfig.getLoreStrings(internalScrollName));
                scroll.setCancelOnMove(mainConfig.doCancelOnMove(internalScrollName));
                scroll.setDestinationHidden(mainConfig.isDestinationHidden(internalScrollName));
                scroll.setEffects(mainConfig.getEffects(internalScrollName));
                scroll.setTeleportDelay(mainConfig.getDelay(internalScrollName));
                scroll.setUses(mainConfig.getTotalUses(internalScrollName));
                scroll.setDestination(mainConfig.getScrollDestination(internalScrollName));
                scroll.setMaterial(mainConfig.getScrollMaterial());

                loadedScrolls.add(scroll);
            } catch (ScrollInvalidException e) {
                plugin.getLogger().log(Level.SEVERE, "Failed to load scroll: " + internalScrollName, e);
            }
        }

        return true;
    }

    /**
     * Gives a scroll to a player.
     * 
     * @param player The player to give the scroll to
     * @param scrollName The name of the scroll to give
     */
    public void giveScrollToPlayer(Player player, String scrollName) {
        getLoadedScroll(scrollName).ifPresent(scroll -> 
            player.getInventory().addItem(scroll.getItemStack()));
    }

    /**
     * Gets a scroll from an ItemStack.
     * 
     * @param stack The ItemStack to get the scroll from
     * @return An Optional containing the scroll if found
     */
    public Optional<Scroll> getScrollByItemStack(ItemStack stack) {
        if (!Scroll.hasPersistentData(stack, Scroll.KEY_INTERNAL_NAME, PersistentDataType.STRING)) {
            return Optional.empty();
        }

        NamespacedKey key = new NamespacedKey(plugin, Scroll.KEY_INTERNAL_NAME);
        String internalScrollName = stack.getItemMeta().getPersistentDataContainer()
            .get(key, PersistentDataType.STRING);

        return this.getLoadedScroll(internalScrollName);
    }
} 