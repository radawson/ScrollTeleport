package org.clockworx.scrollteleportation.files;

import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.clockworx.scrollteleportation.ScrollTeleportation;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Level;

/**
 * Handles loading and saving of messages from the messages.yml file.
 * Supports localization with language-specific messages.
 */
public class MessagesConfig {
    private final ScrollTeleportation plugin;
    private final File messagesFile;
    private FileConfiguration messagesConfig;
    private final Map<String, Map<String, String>> messages;
    private String defaultLanguage;

    /**
     * Creates a new MessagesConfig instance.
     *
     * @param plugin The plugin instance
     */
    public MessagesConfig(ScrollTeleportation plugin) {
        this.plugin = plugin;
        this.messages = new HashMap<>();
        this.messagesFile = new File(plugin.getDataFolder(), "messages.yml");
        this.defaultLanguage = "en";
        loadConfig();
    }

    /**
     * Loads the messages configuration from the messages.yml file.
     */
    public void loadConfig() {
        if (!messagesFile.exists()) {
            plugin.saveResource("messages.yml", false);
        }

        messagesConfig = YamlConfiguration.loadConfiguration(messagesFile);
        loadMessages();
    }

    /**
     * Loads messages from the configuration.
     */
    private void loadMessages() {
        messages.clear();
        
        // Load default language
        ConfigurationSection defaultSection = messagesConfig.getConfigurationSection(defaultLanguage);
        if (defaultSection != null) {
            Map<String, String> defaultMessages = new HashMap<>();
            for (String key : defaultSection.getKeys(false)) {
                defaultMessages.put(key, defaultSection.getString(key));
            }
            messages.put(defaultLanguage, defaultMessages);
        }

        // Load other languages
        for (String lang : messagesConfig.getKeys(false)) {
            if (!lang.equals(defaultLanguage)) {
                ConfigurationSection langSection = messagesConfig.getConfigurationSection(lang);
                if (langSection != null) {
                    Map<String, String> langMessages = new HashMap<>();
                    for (String key : langSection.getKeys(false)) {
                        langMessages.put(key, langSection.getString(key));
                    }
                    messages.put(lang, langMessages);
                }
            }
        }
    }

    /**
     * Saves the messages configuration to the messages.yml file.
     */
    public void saveConfig() {
        try {
            messagesConfig.save(messagesFile);
        } catch (IOException e) {
            plugin.getLogger().log(Level.SEVERE, "Could not save messages.yml", e);
        }
    }

    /**
     * Reloads the messages configuration.
     */
    public void reloadConfig() {
        loadConfig();
    }

    /**
     * Gets a message by its key for the specified language.
     * Falls back to default language if the message is not found in the specified language.
     *
     * @param key The message key
     * @param language The language code (e.g., "en", "es")
     * @return The message, or the key if the message is not found
     */
    public String getMessage(String key, String language) {
        // Try to get message in specified language
        Map<String, String> langMessages = messages.get(language);
        if (langMessages != null && langMessages.containsKey(key)) {
            return langMessages.get(key);
        }

        // Fall back to default language
        Map<String, String> defaultMessages = messages.get(defaultLanguage);
        if (defaultMessages != null && defaultMessages.containsKey(key)) {
            return defaultMessages.get(key);
        }

        // Return key if message not found
        return key;
    }

    /**
     * Gets a message by its key for the default language.
     *
     * @param key The message key
     * @return The message, or the key if the message is not found
     */
    public String getMessage(String key) {
        return getMessage(key, defaultLanguage);
    }

    /**
     * Gets a message by its key with the prefix for the specified language.
     *
     * @param key The message key
     * @param language The language code
     * @return The message with prefix, or the key if the message is not found
     */
    public String getMessageWithPrefix(String key, String language) {
        String prefix = getMessage("prefix", language);
        String message = getMessage(key, language);
        return prefix + " " + message;
    }

    /**
     * Gets a message by its key with the prefix for the default language.
     *
     * @param key The message key
     * @return The message with prefix, or the key if the message is not found
     */
    public String getMessageWithPrefix(String key) {
        return getMessageWithPrefix(key, defaultLanguage);
    }

    /**
     * Gets the prefix message for the specified language.
     *
     * @param language The language code
     * @return The prefix message
     */
    public String getPrefix(String language) {
        return getMessage("prefix", language);
    }

    /**
     * Gets the prefix message for the default language.
     *
     * @return The prefix message
     */
    public String getPrefix() {
        return getPrefix(defaultLanguage);
    }

    /**
     * Gets all messages for the specified language.
     *
     * @param language The language code
     * @return A map of all messages for the specified language
     */
    public Map<String, String> getMessages(String language) {
        Map<String, String> langMessages = messages.get(language);
        return langMessages != null ? new HashMap<>(langMessages) : new HashMap<>();
    }

    /**
     * Gets all messages for the default language.
     *
     * @return A map of all messages for the default language
     */
    public Map<String, String> getMessages() {
        return getMessages(defaultLanguage);
    }

    /**
     * Gets the default language code.
     *
     * @return The default language code
     */
    public String getDefaultLanguage() {
        return defaultLanguage;
    }

    /**
     * Sets the default language code.
     *
     * @param language The language code to set as default
     */
    public void setDefaultLanguage(String language) {
        if (messages.containsKey(language)) {
            this.defaultLanguage = language;
        } else {
            plugin.getLogger().warning("Language '" + language + "' not found in messages.yml. Keeping default language: " + defaultLanguage);
        }
    }
} 