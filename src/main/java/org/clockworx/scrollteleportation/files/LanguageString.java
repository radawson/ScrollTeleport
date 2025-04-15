package org.clockworx.scrollteleportation.files;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextColor;
import net.kyori.adventure.text.minimessage.MiniMessage;

/**
 * Enum representing all language strings used in the plugin.
 * Each string has a config path and a default value.
 */
public enum LanguageString {

    TELEPORTING_IN_TIME("teleporting message", "<gold>Teleporting in %time% seconds..</gold>"),
    MOVEMENT_WARNING("movement warning", "<red>Don't move or teleportation is cancelled.</red>"),
    COMMENCING_TELEPORT("teleport message", "<gold>Commencing teleport!</gold>"),
    NOT_ALLOWED_TO_USE_SCROLL("not allowed to use scroll", "<red>You are not allowed to use scrolls.</red>"),
    CANCELLED_DUE_TO_MOVEMENT("cancelled due to movement", "<red>Teleportation is cancelled because you moved.</red>"),
    CANCELLED_DUE_TO_INTERACTION("cancelled due to interaction", "<red>Teleportation is cancelled because you interacted.</red>"),
    POTION_EFFECTS_APPLIED("potions effects applied", "<gold>You feel strange effects as you've been teleported..</gold>");

    private final String configPath;
    private final String defaultString;
    private static final MiniMessage miniMessage = MiniMessage.miniMessage();

    /**
     * Creates a new language string.
     * 
     * @param configPath The path in the config file
     * @param defaultString The default string value
     */
    LanguageString(String configPath, String defaultString) {
        this.configPath = configPath;
        this.defaultString = defaultString;
    }

    /**
     * Gets the config path for this language string.
     * 
     * @return The config path
     */
    public String getConfigPath() {
        return configPath;
    }

    /**
     * Gets the default string value.
     * 
     * @return The default string
     */
    public String getDefaultString() {
        return defaultString;
    }
    
    /**
     * Parses the string as a Component using MiniMessage.
     * 
     * @param string The string to parse
     * @return The parsed Component
     */
    public static Component parseComponent(String string) {
        return miniMessage.deserialize(string);
    }
} 