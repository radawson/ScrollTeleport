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
import java.util.Set;

/**
 * Manages the storage and retrieval of scrolls in the plugin.
 * This class handles loading scrolls from configuration and providing access to them.
 */
public class ScrollStorage {

    private final ScrollTeleportation plugin;
    private final List<Scroll> loadedScrolls = new ArrayList<>();
    private MainConfig mainConfig;

    /**
     * Creates a new ScrollStorage instance.
     * 
     * @param plugin The plugin instance
     */
    public ScrollStorage(ScrollTeleportation plugin) {
        this.plugin = plugin;
    }

    /**
     * Sets the MainConfig instance.
     * 
     * @param mainConfig The MainConfig instance
     */
    public void setMainConfig(MainConfig mainConfig) {
        this.mainConfig = mainConfig;
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
     * Removes a scroll from the loaded scrolls list.
     * 
     * @param scrollName The name of the scroll to remove
     */
    public void removeLoadedScroll(String scrollName) {
        loadedScrolls.removeIf(scroll -> scroll.getInternalName().equals(scrollName));
    }

    /**
     * Gets a loaded scroll by its name.
     * 
     * @param scrollName The name of the scroll to get
     * @return An Optional containing the scroll if found
     */
    public Optional<Scroll> getLoadedScroll(String scrollName) {
        return loadedScrolls.stream()
                .filter(scroll -> scroll.getInternalName().equals(scrollName))
                .findFirst();
    }

    /**
     * Gets all loaded scrolls.
     * 
     * @return A list of all loaded scrolls
     */
    public List<Scroll> getLoadedScrolls() {
        return new ArrayList<>(loadedScrolls);
    }

    /**
     * Gets a list of all scroll names.
     * 
     * @return A list of scroll names
     */
    public List<String> getScrollNames() {
        return loadedScrolls.stream()
                .map(Scroll::getInternalName)
                .toList();
    }

    /**
     * Loads scrolls from the configuration.
     * 
     * @return true if loading was successful, false otherwise
     */
    public boolean loadScrollsFromConfig() {
        if (mainConfig == null) {
            plugin.getLogger().severe("Cannot load scrolls: MainConfig is null");
            return false;
        }
        
        try {
            // Clear existing scrolls
            loadedScrolls.clear();
            
            // Get all scrolls from config
            Set<String> scrollNames = mainConfig.getScrollsInConfig();
            
            if (scrollNames.isEmpty()) {
                plugin.getLogger().warning("No scrolls found in configuration");
                return true; // Return true as this is not a critical error
            }
            
            boolean allSuccessful = true;
            
            // Load each scroll
            for (String scrollName : scrollNames) {
                try {
                    Scroll scroll = loadScroll(scrollName);
                    if (scroll != null) {
                        addLoadedScroll(scroll);
                    } else {
                        plugin.getLogger().warning("Failed to load scroll: " + scrollName);
                        allSuccessful = false;
                    }
                } catch (Exception e) {
                    plugin.getLogger().warning("Error loading scroll " + scrollName + ": " + e.getMessage());
                    allSuccessful = false;
                }
            }
            
            return allSuccessful;
        } catch (Exception e) {
            plugin.getLogger().severe("Failed to load scrolls from config: " + e.getMessage());
            e.printStackTrace();
            return false;
        }
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

    /**
     * Loads a single scroll from the configuration.
     * 
     * @param scrollName The name of the scroll to load
     * @return The loaded scroll, or null if loading failed
     */
    private Scroll loadScroll(String scrollName) {
        try {
            Scroll scroll = new Scroll(scrollName);
            
            scroll.setDisplayName(mainConfig.getScrollDisplayName(scrollName));
            scroll.setDescriptionLore(mainConfig.getLoreStrings(scrollName));
            scroll.setCancelOnMove(mainConfig.doCancelOnMove(scrollName));
            scroll.setDestinationHidden(mainConfig.isDestinationHidden(scrollName));
            scroll.setEffects(mainConfig.getEffects(scrollName));
            scroll.setTeleportDelay(mainConfig.getDelay(scrollName));
            scroll.setUses(mainConfig.getTotalUses(scrollName));
            scroll.setDestination(mainConfig.getScrollDestination(scrollName));
            scroll.setMaterial(mainConfig.getScrollMaterial());
            
            return scroll;
        } catch (Exception e) {
            plugin.getLogger().warning("Error creating scroll " + scrollName + ": " + e.getMessage());
            return null;
        }
    }
} 