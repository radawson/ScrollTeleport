package org.clockworx.scrollteleportation.files;

import org.clockworx.scrollteleportation.ScrollTeleportation;
import org.clockworx.scrollteleportation.exceptions.DestinationInvalidException;
import org.clockworx.scrollteleportation.storage.Scroll;
import org.clockworx.scrollteleportation.storage.ScrollDestination;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.plugin.Plugin;
import org.clockworx.scrollteleportation.storage.ScrollStorage;
import org.clockworx.scrollteleportation.exceptions.ScrollException;
import org.clockworx.scrollteleportation.exceptions.ScrollInvalidException;

import java.io.File;
import java.io.IOException;
import java.util.*;
import java.util.logging.Level;
import java.util.stream.Collectors;

/**
 * Handles the main configuration for the ScrollTeleportation plugin.
 * This includes loading and saving configuration files, managing scroll settings,
 * and handling plugin messages.
 */
public class MainConfig {

    private final ScrollTeleportation plugin;
    private FileConfiguration config;
    private File configFile;
    private final Map<String, String> messages;
    private ScrollStorage scrollStorage;
    private MessagesConfig messagesConfig;

    /**
     * Creates a new MainConfig instance.
     *
     * @param plugin The plugin instance
     */
    public MainConfig(Plugin plugin) {
        this.plugin = (ScrollTeleportation) plugin;
        this.messages = new HashMap<>();
        loadConfig();
    }

    /**
     * Loads the configuration from file.
     * 
     * @return true if loading was successful, false otherwise
     */
    public boolean loadConfig() {
        try {
            if (!plugin.getDataFolder().exists()) {
                plugin.getDataFolder().mkdir();
            }

            // Load main config
            configFile = new File(plugin.getDataFolder(), "config.yml");
            if (!configFile.exists()) {
                plugin.saveResource("config.yml", false);
            }
            config = YamlConfiguration.loadConfiguration(configFile);

            // Initialize messages config
            try {
                if (messagesConfig == null) {
                    messagesConfig = new MessagesConfig(plugin);
                } else {
                    messagesConfig.reloadConfig();
                }
            } catch (Exception e) {
                plugin.getLogger().warning("Failed to load messages config: " + e.getMessage());
                // Continue loading even if messages fail
            }

            // Initialize scroll storage after config is loaded
            try {
                if (scrollStorage == null) {
                    scrollStorage = new ScrollStorage(plugin);
                }
                
                // Set the MainConfig instance in ScrollStorage
                scrollStorage.setMainConfig(this);
                
                // Load scrolls from config
                scrollStorage.loadScrollsFromConfig();
            } catch (Exception e) {
                plugin.getLogger().warning("Failed to load scrolls: " + e.getMessage());
                // Continue loading even if scrolls fail
            }
            
            return true;
        } catch (Exception e) {
            plugin.getLogger().severe("Failed to load configuration: " + e.getMessage());
            e.printStackTrace();
            return false;
        }
    }

    /**
     * Saves the current configuration to file.
     */
    public void saveConfig() {
        try {
            config.save(configFile);
        } catch (IOException e) {
            plugin.getLogger().log(Level.SEVERE, "Could not save config to " + configFile, e);
        }
    }

    /**
     * Reloads the configuration from file.
     */
    public void reloadConfig() {
        loadConfig();
    }

    /**
     * Gets the configuration instance.
     *
     * @return The FileConfiguration instance
     */
    public FileConfiguration getConfig() {
        return config;
    }

    /**
     * Loads messages from the configuration.
     */
    private void loadMessages() {
        // Check if we should use the messages.yml file
        String messagesFile = config.getString("messages-file");
        if (messagesFile != null && !messagesFile.isEmpty()) {
            // Use the messages from the MessagesConfig
            messages.putAll(messagesConfig.getMessages());
            return;
        }

        // Fallback to the old method of loading messages from config.yml
        ConfigurationSection messagesSection = config.getConfigurationSection("messages");
        if (messagesSection == null) {
            plugin.getLogger().warning("No messages section found in config.yml");
            return;
        }

        for (String key : messagesSection.getKeys(false)) {
            messages.put(key, messagesSection.getString(key));
        }
    }

    /**
     * Gets a message by its key.
     *
     * @param key The message key
     * @return The message, or the key if the message is not found
     */
    public String getMessage(String key) {
        // First try to get the message from the MessagesConfig
        if (messagesConfig != null) {
            return messagesConfig.getMessage(key);
        }
        
        // Fallback to the old method
        return messages.getOrDefault(key, key);
    }

    /**
     * Gets a message by its key with the prefix.
     *
     * @param key The message key
     * @return The message with prefix, or the key if the message is not found
     */
    public String getMessageWithPrefix(String key) {
        // First try to get the message from the MessagesConfig
        if (messagesConfig != null) {
            return messagesConfig.getMessageWithPrefix(key);
        }
        
        // Fallback to the old method
        String prefix = getMessage("prefix");
        String message = getMessage(key);
        return prefix + " " + message;
    }

    /**
     * Gets the prefix message.
     *
     * @return The prefix message
     */
    public String getPrefix() {
        // First try to get the prefix from the MessagesConfig
        if (messagesConfig != null) {
            return messagesConfig.getPrefix();
        }
        
        // Fallback to the old method
        return getMessage("prefix");
    }

    /**
     * Gets the scroll storage instance.
     *
     * @return The ScrollStorage instance
     */
    public ScrollStorage getScrollStorage() {
        return scrollStorage;
    }

    /**
     * Loads the configuration from the config file.
     */
    public void loadConfiguration() {
        config = plugin.getConfig();
        
        // Set default values
        setDefaults();
        
        // Save config
        plugin.saveConfig();
    }

    /**
     * Sets the default configuration values.
     */
    private void setDefaults() {
        // Set header using the recommended approach
        config.options().setHeader(Arrays.asList(
            "Scroll Teleportation Configuration",
            "",
            "Scroll Configuration:",
            "- name: The display name of the scroll (must be unique)",
            "- lores: List of lore lines (use '' for blank line)",
            "- destination: Where the scroll teleports to",
            "- destination_hidden: Whether to show 'unknown' as destination",
            "- delay: Time in seconds before teleportation",
            "- cancel_on_move: Whether to cancel teleport if player moves",
            "- uses: Number of uses (-1 for infinite)",
            "- effects: List of potion effects (format: EFFECT_NAME DURATION)",
            "",
            "Destination Types:",
            "1. Fixed point: 'world,x,y,z'",
            "2. Random point: 'random world' or 'random' for any world",
            "3. Random radius: 'random_radius(point=world,x,y,z radius=1000)'",
            "4. Named location: 'spawn world'"
        ));

        // Set defaults for scroll material
        config.addDefault("Scroll.material", Material.PAPER.name());

        // Example scroll configuration
        config.addDefault("Scrolls.spawn_scroll.name", "&6Scroll of Spawn");
        config.addDefault("Scrolls.spawn_scroll.lores", Arrays.asList(
            "&7Teleports you to spawn",
            "",
            "&7Common scroll"
        ));
        config.addDefault("Scrolls.spawn_scroll.destination", "spawn world");
        config.addDefault("Scrolls.spawn_scroll.destination_hidden", false);
        config.addDefault("Scrolls.spawn_scroll.delay", 3);
        config.addDefault("Scrolls.spawn_scroll.cancel_on_move", true);
        config.addDefault("Scrolls.spawn_scroll.uses", 1);
        config.addDefault("Scrolls.spawn_scroll.effects", Arrays.asList("BLINDNESS 3"));

        config.options().copyDefaults(true);
    }

    /**
     * Gets a translatable message from the config.
     * 
     * @param message The message to get
     * @return The translated message
     */
    public Component getTranslatableMessage(LanguageString message) {
        String text = config.getString(message.getConfigPath(), message.getDefaultString());
        return Component.text(text.replace('&', '§'));
    }

    /**
     * Gets a scroll by its display name.
     * 
     * @param scrollName The display name of the scroll
     * @return The internal name of the scroll, or null if not found
     */
    public String getScroll(String scrollName) {
        return config.getConfigurationSection("Scrolls").getKeys(false).stream()
            .filter(key -> getScrollDisplayName(key).equalsIgnoreCase(scrollName))
            .findFirst()
            .orElse(null);
    }

    /**
     * Gets the display name of a scroll.
     * 
     * @param scroll The internal name of the scroll
     * @return The display name
     */
    public String getScrollDisplayName(String scroll) {
        return config.getString("Scrolls." + scroll + ".name", scroll)
            .replace('&', '§');
    }

    /**
     * Gets the teleport delay for a scroll.
     * 
     * @param scroll The internal name of the scroll
     * @return The delay in seconds
     */
    public int getDelay(String scroll) {
        return config.getInt("Scrolls." + scroll + ".delay", 5);
    }

    /**
     * Gets the lore strings for a scroll.
     * 
     * @param scroll The internal name of the scroll
     * @return The list of lore strings
     */
    public List<String> getLoreStrings(String scroll) {
        return config.getStringList("Scrolls." + scroll + ".lores").stream()
            .map(text -> text.replace('&', '§'))
            .collect(Collectors.toList());
    }

    /**
     * Checks if a scroll's destination is hidden.
     * 
     * @param scroll The internal name of the scroll
     * @return True if the destination is hidden
     */
    public boolean isDestinationHidden(String scroll) {
        return config.getBoolean("Scrolls." + scroll + ".destination_hidden", false);
    }

    /**
     * Checks if a scroll's teleportation is cancelled on move.
     * 
     * @param scroll The internal name of the scroll
     * @return True if teleportation is cancelled on move
     */
    public boolean doCancelOnMove(String scroll) {
        return config.getBoolean("Scrolls." + scroll + ".cancel_on_move", true);
    }

    /**
     * Gets the destination for a scroll.
     * 
     * @param scroll The internal name of the scroll
     * @return The scroll destination
     * @throws ScrollInvalidException if the destination is invalid
     */
    public ScrollDestination getScrollDestination(String scroll) throws ScrollInvalidException {
        String destination = config.getString("Scrolls." + scroll + ".destination", "random");
        try {
            return ScrollDestination.createFromLocationString(destination);
        } catch (ScrollException e) {
            throw new ScrollInvalidException("Invalid destination for scroll " + scroll + ": " + e.getMessage(), e);
        }
    }

    /**
     * Gets the total number of uses for a scroll.
     * 
     * @param scroll The internal name of the scroll
     * @return The number of uses
     */
    public int getTotalUses(String scroll) {
        int uses = config.getInt("Scrolls." + scroll + ".uses", 1);
        return uses < 0 ? Scroll.SCROLL_USES_INFINITE : uses;
    }

    /**
     * Gets the material used for scrolls.
     * 
     * @return The material
     */
    public Material getScrollMaterial() {
        String materialName = config.getString("Scroll.material", Material.PAPER.name());
        return Material.getMaterial(materialName);
    }

    /**
     * Gets the list of potion effects for a scroll.
     * 
     * @param scroll The internal name of the scroll
     * @return The list of potion effects
     */
    public List<PotionEffect> getEffects(String scroll) {
        List<PotionEffect> effects = new ArrayList<>();

        for (String effectString : config.getStringList("Scrolls." + scroll + ".effects")) {
            String[] args = effectString.split(" ");

            if (args.length != 2) {
                plugin.getLogger().warning("Invalid effect format for scroll " + scroll + ": " + effectString);
                continue;
            }

            String effectName = args[0].trim();
            int duration;

            try {
                duration = Integer.parseInt(args[1].trim());
            } catch (NumberFormatException e) {
                plugin.getLogger().warning("Invalid duration for effect " + effectName + " in scroll " + scroll);
                continue;
            }

            PotionEffectType effectType = null;
            try {
                effectType = org.bukkit.Registry.POTION_EFFECT_TYPE.get(org.bukkit.NamespacedKey.minecraft(effectName.toLowerCase()));
            } catch (Exception e) {
                plugin.getLogger().warning("Invalid effect type " + effectName + " in scroll " + scroll);
                continue;
            }

            effects.add(new PotionEffect(effectType, duration * 20, 1));
        }

        return effects;
    }

    /**
     * Creates a new scroll in the configuration.
     * 
     * @param scroll The internal name of the scroll
     * @param scrollName The display name of the scroll
     * @param destination The destination location
     * @param delay The teleport delay
     * @param uses The number of uses
     * @return True if the scroll was created successfully
     */
    public boolean createNewScroll(String scroll, String scrollName, Location destination, int delay, int uses) {
        if (config.getString("Scrolls." + scroll + ".name") != null) {
            return false;
        }

        config.set("Scrolls." + scroll + ".name", scrollName);
        config.set("Scrolls." + scroll + ".destination", 
            destination.getWorld().getName() + ", " + destination.getBlockX() + ", " + 
            destination.getBlockY() + ", " + destination.getBlockZ());
        config.set("Scrolls." + scroll + ".delay", delay);
        config.set("Scrolls." + scroll + ".destination_hidden", false);
        config.set("Scrolls." + scroll + ".cancel_on_move", true);
        config.set("Scrolls." + scroll + ".uses", uses);
        config.set("Scrolls." + scroll + ".effects", new ArrayList<>());
        config.set("Scrolls." + scroll + ".lores", Arrays.asList(
            "<blue>This mighty and rare scroll</blue>",
            "<blue>will teleport you</blue>",
            "",
            ""
        ));

        plugin.saveConfig();
        return true;
    }

    /**
     * Sets the name of a scroll.
     * 
     * @param scroll The internal name of the scroll
     * @param name The new display name
     */
    public void setName(String scroll, String name) {
        config.set("Scrolls." + scroll + ".name", name);
        plugin.saveConfig();
    }

    /**
     * Sets the delay of a scroll.
     * 
     * @param scroll The internal name of the scroll
     * @param delay The new delay
     * @throws ScrollInvalidException if the delay is negative
     */
    public void setDelay(String scroll, int delay) throws ScrollInvalidException {
        if (delay < 0) {
            throw new ScrollInvalidException("Delay cannot be negative");
        }
        config.set("Scrolls." + scroll + ".delay", delay);
        plugin.saveConfig();
    }

    /**
     * Sets the number of uses for a scroll.
     * 
     * @param scroll The internal name of the scroll
     * @param uses The new number of uses
     * @throws ScrollInvalidException if uses is less than -1
     */
    public void setUses(String scroll, int uses) throws ScrollInvalidException {
        if (uses < Scroll.SCROLL_USES_INFINITE) {
            throw new ScrollInvalidException("Uses cannot be less than " + Scroll.SCROLL_USES_INFINITE);
        }
        config.set("Scrolls." + scroll + ".uses", uses);
        plugin.saveConfig();
    }

    /**
     * Sets whether a scroll's destination is hidden.
     * 
     * @param scroll The internal name of the scroll
     * @param hidden Whether to hide the destination
     */
    public void setDestinationHidden(String scroll, boolean hidden) {
        config.set("Scrolls." + scroll + ".destination_hidden", hidden);
        plugin.saveConfig();
    }

    /**
     * Sets whether a scroll's teleportation is cancelled on move.
     * 
     * @param scroll The internal name of the scroll
     * @param cancel Whether to cancel on movement
     */
    public void setCancelOnMove(String scroll, boolean cancel) {
        config.set("Scrolls." + scroll + ".cancel_on_move", cancel);
        plugin.saveConfig();
    }

    /**
     * Sets the destination of a scroll.
     * 
     * @param scroll The internal name of the scroll
     * @param location The new destination location
     */
    public void setDestination(String scroll, Location location) {
        config.set("Scrolls." + scroll + ".destination",
                location.getWorld().getName() + ", " + location.getBlockX() + ", " + 
                location.getBlockY() + ", " + location.getBlockZ());
        plugin.saveConfig();
    }

    /**
     * Gets all scrolls in the configuration.
     * 
     * @return The set of scroll internal names
     */
    public Set<String> getScrollsInConfig() {
        // First try the uppercase "Scrolls" section
        ConfigurationSection scrollsSection = config.getConfigurationSection("Scrolls");
        
        // If not found, try the lowercase "scrolls" section
        if (scrollsSection == null) {
            scrollsSection = config.getConfigurationSection("scrolls");
            
            // If still not found, create a default section
            if (scrollsSection == null) {
                plugin.getLogger().warning("No 'Scrolls' or 'scrolls' section found in config.yml. Creating default scrolls section.");
                config.createSection("Scrolls");
                // Add a default scroll if none exist
                config.set("Scrolls.spawn_scroll.name", "&6Scroll of Spawn");
                config.set("Scrolls.spawn_scroll.lores", Arrays.asList(
                    "&7Teleports you to spawn",
                    "",
                    "&7Common scroll"
                ));
                config.set("Scrolls.spawn_scroll.destination", "spawn world");
                config.set("Scrolls.spawn_scroll.destination_hidden", false);
                config.set("Scrolls.spawn_scroll.delay", 3);
                config.set("Scrolls.spawn_scroll.cancel_on_move", true);
                config.set("Scrolls.spawn_scroll.uses", 1);
                config.set("Scrolls.spawn_scroll.effects", Arrays.asList("BLINDNESS 3"));
                saveConfig();
                return new HashSet<>(Arrays.asList("spawn_scroll"));
            }
        }
        
        return scrollsSection.getKeys(false);
    }

    /**
     * Checks if chunks should be loaded on teleportation.
     * 
     * @return True if chunks should be loaded
     */
    public boolean doLoadChunk() {
        return config.getBoolean("Scroll.load-chunk-on-teleport");
    }

    /**
     * Checks if a world is blocked from teleportation.
     * 
     * @param worldName The name of the world to check
     * @return true if the world is blocked
     */
    public boolean isWorldBlocked(String worldName) {
        return config.getStringList("blocked_worlds").contains(worldName);
    }

    /**
     * Checks if a location is in a blocked region.
     * 
     * @param location The location to check
     * @return true if the location is in a blocked region
     */
    public boolean isRegionBlocked(Location location) {
        // TODO: Implement region blocking logic
        return false;
    }

    /**
     * Checks if teleportation is blocked during combat.
     * 
     * @return true if teleportation is blocked during combat
     */
    public boolean isCombatBlocked() {
        return config.getBoolean("block_combat_teleport", true);
    }

    /**
     * Reloads the configuration.
     */
    public void reload() {
        loadConfiguration();
        loadMessages();
        scrollStorage.loadScrollsFromConfig();
    }
} 