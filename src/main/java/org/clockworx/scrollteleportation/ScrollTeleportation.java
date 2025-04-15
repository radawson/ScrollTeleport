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

    private MainConfig config;
    private TeleportHandler teleHandler;
    private ScrollStorage scrollStorage;

    @Override
    public void onEnable() {
        // Initialize components
        this.config = new MainConfig(this);
        this.teleHandler = new TeleportHandler(this);
        this.scrollStorage = new ScrollStorage(this);

        // Load configuration
        this.config.loadConfiguration();
        this.scrollStorage.loadScrollsFromConfig();

        // Register listeners
        registerListeners();

        // Register commands
        registerCommands();

        // Log startup message
        getLogger().info("Loaded " + scrollStorage.getLoadedScrolls().size() + " scrolls!");
        getLogger().info("Scroll Teleportation v" + getDescription().getVersion() + " has been enabled.");
    }

    @Override
    public void onDisable() {
        // Cancel all tasks
        getServer().getScheduler().cancelTasks(this);
        
        // Log shutdown message
        getLogger().info("Scroll Teleportation v" + getDescription().getVersion() + " has been disabled.");
    }

    private void registerListeners() {
        getServer().getPluginManager().registerEvents(new ActivateScrollListener(this), this);
        getServer().getPluginManager().registerEvents(new PlayerMoveListener(this), this);
        getServer().getPluginManager().registerEvents(new PlayerInvOpenListener(this), this);
    }

    private void registerCommands() {
        getCommand("scroll").setExecutor(new CommandHandler(this));
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
        return oldDisplayName.replaceAll("§[0-9a-fk-or]", "");
    }
} 