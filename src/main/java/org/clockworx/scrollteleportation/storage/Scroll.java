package org.clockworx.scrollteleportation.storage;

import org.clockworx.scrollteleportation.ScrollTeleportation;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.minimessage.MiniMessage;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataType;
import org.bukkit.potion.PotionEffect;
import org.clockworx.scrollteleportation.exceptions.ScrollInvalidException;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.logging.Level;

/**
 * Represents a teleportation scroll in the game.
 * This class manages the properties and behavior of scrolls.
 */
public class Scroll {

    public static final String KEY_INTERNAL_NAME = "internalName";
    public static final String KEY_TOTAL_USES = "totalUses";
    public static final String KEY_CURRENT_USES = "currentUses";
    public static final int SCROLL_USES_INFINITE = -1;
    private static final MiniMessage miniMessage = MiniMessage.miniMessage();

    private String internalName;
    private String displayName;
    private List<String> descriptionLore = new ArrayList<>();
    private boolean destinationHidden = false;
    private boolean cancelOnMove = true;
    private int teleportDelay = 5; // In seconds
    private int uses = 1;
    private List<PotionEffect> effects = new ArrayList<>();
    private ScrollDestination destination;
    private Material material = Material.PAPER;

    /**
     * Creates a new scroll with the given internal name.
     * 
     * @param internalName The internal name of the scroll
     * @throws ScrollInvalidException if the internal name is invalid
     */
    public Scroll(String internalName) throws ScrollInvalidException {
        this.setInternalName(internalName);
    }

    /**
     * Gets the internal name of the scroll.
     * 
     * @return The internal name
     */
    public String getInternalName() {
        return internalName;
    }

    /**
     * Sets the internal name of the scroll.
     * 
     * @param internalName The internal name
     * @throws ScrollInvalidException if the internal name is null or empty
     */
    public void setInternalName(String internalName) throws ScrollInvalidException {
        if (internalName == null || internalName.trim().isEmpty()) {
            throw new ScrollInvalidException("Internal name cannot be null or empty");
        }
        this.internalName = internalName;
    }

    /**
     * Gets the display name of the scroll.
     * 
     * @return The display name
     */
    public String getDisplayName() {
        return displayName;
    }

    /**
     * Sets the display name of the scroll.
     * 
     * @param displayName The display name
     * @throws ScrollInvalidException if the display name is null or empty
     */
    public void setDisplayName(String displayName) throws ScrollInvalidException {
        if (displayName == null || displayName.trim().isEmpty()) {
            throw new ScrollInvalidException("Display name cannot be null or empty");
        }
        this.displayName = displayName;
    }

    /**
     * Gets the description lore of the scroll.
     * 
     * @return The description lore
     */
    public List<String> getDescriptionLore() {
        return descriptionLore;
    }

    /**
     * Sets the description lore of the scroll.
     * 
     * @param lore The description lore
     * @throws ScrollInvalidException if the lore is null
     */
    public void setDescriptionLore(List<String> lore) throws ScrollInvalidException {
        if (lore == null) {
            throw new ScrollInvalidException("Lore cannot be null");
        }
        this.descriptionLore = new ArrayList<>(lore);
    }

    /**
     * Checks if the destination is hidden.
     * 
     * @return True if the destination is hidden
     */
    public boolean isDestinationHidden() {
        return destinationHidden;
    }

    /**
     * Sets whether the destination is hidden.
     * 
     * @param destinationHidden True if the destination should be hidden
     */
    public void setDestinationHidden(boolean destinationHidden) {
        this.destinationHidden = destinationHidden;
    }

    /**
     * Checks if teleportation is cancelled on move.
     * 
     * @return True if teleportation is cancelled on move
     */
    public boolean isCancelledOnMove() {
        return cancelOnMove;
    }

    /**
     * Sets whether teleportation is cancelled on move.
     * 
     * @param cancelOnMove True if teleportation should be cancelled on move
     */
    public void setCancelOnMove(boolean cancelOnMove) {
        this.cancelOnMove = cancelOnMove;
    }

    /**
     * Gets the teleport delay in seconds.
     * 
     * @return The teleport delay
     */
    public int getTeleportDelay() {
        return teleportDelay;
    }

    /**
     * Sets the teleport delay in seconds.
     * 
     * @param teleportDelay The teleport delay
     * @throws ScrollInvalidException if the delay is negative
     */
    public void setTeleportDelay(int teleportDelay) throws ScrollInvalidException {
        if (teleportDelay < 0) {
            throw new ScrollInvalidException("Teleport delay cannot be negative");
        }
        this.teleportDelay = teleportDelay;
    }

    /**
     * Gets the number of uses of the scroll.
     * 
     * @return The number of uses
     */
    public int getUses() {
        return uses;
    }

    /**
     * Sets the number of uses of the scroll.
     * 
     * @param uses The number of uses
     * @throws ScrollInvalidException if uses is less than -1
     */
    public void setUses(int uses) throws ScrollInvalidException {
        if (uses < SCROLL_USES_INFINITE) {
            throw new ScrollInvalidException("Uses cannot be less than " + SCROLL_USES_INFINITE);
        }
        this.uses = uses;
    }

    /**
     * Gets the effects of the scroll.
     * 
     * @return The effects
     */
    public List<PotionEffect> getEffects() {
        return effects;
    }

    /**
     * Sets the effects of the scroll.
     * 
     * @param effects The effects
     * @throws ScrollInvalidException if effects is null
     */
    public void setEffects(List<PotionEffect> effects) throws ScrollInvalidException {
        if (effects == null) {
            throw new ScrollInvalidException("Effects cannot be null");
        }
        this.effects = new ArrayList<>(effects);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Scroll scroll = (Scroll) o;
        return Objects.equals(internalName, scroll.internalName);
    }

    @Override
    public int hashCode() {
        return Objects.hash(internalName);
    }

    /**
     * Gets the destination of the scroll.
     * 
     * @return The destination
     */
    public ScrollDestination getDestination() {
        return destination;
    }

    /**
     * Sets the destination of the scroll.
     * 
     * @param destination The destination
     * @throws ScrollInvalidException if destination is null
     */
    public void setDestination(ScrollDestination destination) throws ScrollInvalidException {
        if (destination == null) {
            throw new ScrollInvalidException("Destination cannot be null");
        }
        this.destination = destination;
    }

    /**
     * Gets the material of the scroll.
     * 
     * @return The material
     */
    public Material getMaterial() {
        return material;
    }

    /**
     * Sets the material of the scroll.
     * 
     * @param material The material
     * @throws ScrollInvalidException if material is null
     */
    public void setMaterial(Material material) throws ScrollInvalidException {
        if (material == null) {
            throw new ScrollInvalidException("Material cannot be null");
        }
        this.material = material;
    }

    /**
     * Creates an ItemStack representing this scroll.
     * 
     * @return The ItemStack
     */
    public ItemStack getItemStack() {
        ItemStack item = new ItemStack(this.getMaterial(), 1);
        ItemMeta im = item.getItemMeta();
        
        if (im == null) {
            ScrollTeleportation.getInstance().getLogger().log(Level.SEVERE, "Failed to get ItemMeta for scroll " + this.getInternalName());
            return item;
        }

        // Set name using Adventure API
        im.displayName(miniMessage.deserialize("<gold>" + this.getDisplayName() + "</gold>"));

        // Determine destination text
        String destinationText;
        if (this.isDestinationHidden()) {
            destinationText = "Destination: Unknown";
        } else {
            destinationText = "Destination: " + getDestination().getLocationDescription();
        }

        // Set lore
        List<Component> loreComponents = new ArrayList<>();
        
        // Add description lore
        for (String loreLine : this.descriptionLore) {
            loreComponents.add(miniMessage.deserialize(loreLine));
        }

        // Add destination
        loreComponents.add(miniMessage.deserialize("<green>" + destinationText + "</green>"));

        // Add uses
        String usesText = this.getUses() < 0 ? "infinite" : String.valueOf(this.getUses());
        loreComponents.add(miniMessage.deserialize("<green>Uses: " + usesText + "</green>"));

        // Set lore
        im.lore(loreComponents);

        // Create namespaced keys
        NamespacedKey internalNameKey = new NamespacedKey(ScrollTeleportation.getInstance(), KEY_INTERNAL_NAME);
        NamespacedKey totalUsesKey = new NamespacedKey(ScrollTeleportation.getInstance(), KEY_TOTAL_USES);
        NamespacedKey currentUsesKey = new NamespacedKey(ScrollTeleportation.getInstance(), KEY_CURRENT_USES);

        // Store the internal name in the itemstack
        im.getPersistentDataContainer().set(internalNameKey, PersistentDataType.STRING, this.getInternalName());

        // Store the uses of the scroll
        im.getPersistentDataContainer().set(totalUsesKey, PersistentDataType.INTEGER, this.getUses());
        im.getPersistentDataContainer().set(currentUsesKey, PersistentDataType.INTEGER, this.getUses());

        // Set ItemMeta
        item.setItemMeta(im);

        return item;
    }

    /**
     * Applies the scroll's effects to a player.
     * 
     * @param player The player to apply effects to
     */
    public void applyEffects(Player player) {
        player.addPotionEffects(this.effects);
    }

    /**
     * Checks if an ItemStack has persistent data with the given key and type.
     * 
     * @param stack The ItemStack to check
     * @param keyString The key to check for
     * @param dataType The type of data to check for
     * @return True if the ItemStack has the persistent data
     */
    public static boolean hasPersistentData(ItemStack stack, String keyString, PersistentDataType dataType) {
        ScrollTeleportation plugin = ScrollTeleportation.getInstance();

        if (stack == null) return false;
        if (stack.getType() != plugin.getMainConfig().getScrollMaterial()) return false;
        if (!stack.hasItemMeta()) return false;
        if (stack.getItemMeta() == null) return false;

        NamespacedKey key = new NamespacedKey(plugin, keyString);
        return stack.getItemMeta().getPersistentDataContainer().has(key, dataType);
    }
    
    /**
     * Gets the current number of uses for an ItemStack.
     * 
     * @param stack The ItemStack to check
     * @return The current number of uses, or -1 if infinite
     */
    public static int getCurrentUses(ItemStack stack) {
        ScrollTeleportation plugin = ScrollTeleportation.getInstance();
        
        if (stack == null || !stack.hasItemMeta() || stack.getItemMeta() == null) {
            return 0;
        }
        
        NamespacedKey key = new NamespacedKey(plugin, KEY_CURRENT_USES);
        return stack.getItemMeta().getPersistentDataContainer().getOrDefault(key, PersistentDataType.INTEGER, 0);
    }
    
    /**
     * Sets the current number of uses for an ItemStack.
     * 
     * @param stack The ItemStack to update
     * @param uses The new number of uses
     * @return The updated ItemStack
     */
    public static ItemStack setCurrentUses(ItemStack stack, int uses) {
        ScrollTeleportation plugin = ScrollTeleportation.getInstance();
        
        if (stack == null || !stack.hasItemMeta() || stack.getItemMeta() == null) {
            return stack;
        }
        
        ItemMeta meta = stack.getItemMeta();
        NamespacedKey key = new NamespacedKey(plugin, KEY_CURRENT_USES);
        meta.getPersistentDataContainer().set(key, PersistentDataType.INTEGER, uses);
        stack.setItemMeta(meta);
        
        return stack;
    }
    
    /**
     * Decreases the number of uses for an ItemStack by one.
     * 
     * @param stack The ItemStack to update
     * @return The updated ItemStack
     */
    public static ItemStack decreaseUses(ItemStack stack) {
        int currentUses = getCurrentUses(stack);
        
        if (currentUses <= 0) {
            return stack;
        }
        
        return setCurrentUses(stack, currentUses - 1);
    }
} 