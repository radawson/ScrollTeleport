package org.clockworx.scrollteleportation;

import org.clockworx.scrollteleportation.commands.CommandHandler;
import org.clockworx.scrollteleportation.files.MainConfig;
import org.clockworx.scrollteleportation.listeners.ActivateScrollListener;
import org.clockworx.scrollteleportation.listeners.PlayerInvOpenListener;
import org.clockworx.scrollteleportation.listeners.PlayerMoveListener;
import org.clockworx.scrollteleportation.storage.ScrollStorage;
import org.clockworx.scrollteleportation.teleporthandler.TeleportHandler;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.plugin.java.JavaPlugin;

public class ScrollTeleportation extends JavaPlugin {

    private static ScrollTeleportation instance;
    private MainConfig config;
    private TeleportHandler teleHandler;
    private ScrollStorage scrollStorage;

    @Override
    public void onEnable() {
        instance = this;
        
        // Initialize components with error handling
        boolean configLoaded = initializeConfig();
        boolean teleportHandlerInitialized = initializeTeleportHandler();
        boolean listenersRegistered = registerListeners();
        boolean commandsRegistered = registerCommands();

        // Log startup message with status
        if (configLoaded && scrollStorage != null) {
            getLogger().info("Loaded " + scrollStorage.getLoadedScrolls().size() + " scrolls!");
        } else {
            getLogger().warning("Some scrolls may not have loaded correctly.");
        }
        
        // Log overall status
        if (configLoaded && teleportHandlerInitialized && listenersRegistered && commandsRegistered) {
            getLogger().info("Scroll Teleportation v" + getPluginMeta().getVersion() + " has been enabled successfully.");
        } else {
            getLogger().warning("Scroll Teleportation v" + getPluginMeta().getVersion() + " has been enabled with some issues. Check the logs above for details.");
        }
    }

    @Override
    public void onDisable() {
        // Cancel all tasks
        getServer().getScheduler().cancelTasks(this);
        
        // Log shutdown message
        getLogger().info("Scroll Teleportation v" + getPluginMeta().getVersion() + " has been disabled.");
    }

    /**
     * Initializes the configuration with error handling.
     * 
     * @return true if initialization was successful, false otherwise
     */
    private boolean initializeConfig() {
        try {
            this.config = new MainConfig(this);
            this.scrollStorage = this.config.getScrollStorage();
            return true;
        } catch (Exception e) {
            getLogger().severe("Failed to initialize configuration: " + e.getMessage());
            e.printStackTrace();
            return false;
        }
    }
    
    /**
     * Initializes the teleport handler with error handling.
     * 
     * @return true if initialization was successful, false otherwise
     */
    private boolean initializeTeleportHandler() {
        try {
            this.teleHandler = new TeleportHandler(this);
            return true;
        } catch (Exception e) {
            getLogger().severe("Failed to initialize teleport handler: " + e.getMessage());
            e.printStackTrace();
            return false;
        }
    }

    /**
     * Registers listeners with error handling.
     * 
     * @return true if all listeners were registered successfully, false otherwise
     */
    private boolean registerListeners() {
        boolean success = true;
        
        try {
            getServer().getPluginManager().registerEvents(new ActivateScrollListener(this), this);
        } catch (Exception e) {
            getLogger().severe("Failed to register ActivateScrollListener: " + e.getMessage());
            e.printStackTrace();
            success = false;
        }
        
        try {
            getServer().getPluginManager().registerEvents(new PlayerMoveListener(this), this);
        } catch (Exception e) {
            getLogger().severe("Failed to register PlayerMoveListener: " + e.getMessage());
            e.printStackTrace();
            success = false;
        }
        
        try {
            getServer().getPluginManager().registerEvents(new PlayerInvOpenListener(this), this);
        } catch (Exception e) {
            getLogger().severe("Failed to register PlayerInvOpenListener: " + e.getMessage());
            e.printStackTrace();
            success = false;
        }
        
        return success;
    }

    /**
     * Registers commands with error handling.
     * 
     * @return true if commands were registered successfully, false otherwise
     */
    private boolean registerCommands() {
        try {
            if (getCommand("scrolltp") != null) {
                getCommand("scrolltp").setExecutor(new CommandHandler(this));
                return true;
            } else {
                getLogger().severe("Failed to register commands: 'scrolltp' command not found in plugin.yml");
                return false;
            }
        } catch (Exception e) {
            getLogger().severe("Failed to register commands: " + e.getMessage());
            e.printStackTrace();
            return false;
        }
    }

    public static ScrollTeleportation getInstance() {
        return instance;
    }

    public MainConfig getMainConfig() {
        return config;
    }

    public TeleportHandler getTeleportHandler() {
        return teleHandler;
    }

    public ScrollStorage getScrollStorage() {
        return scrollStorage;
    }

    /**
     * Removes color codes from a display name for comparison.
     * @param oldDisplayName Display name to fix
     * @return A string without color codes
     */
    public String fixName(String oldDisplayName) {
        return oldDisplayName.replace("§", "&");
    }
} 