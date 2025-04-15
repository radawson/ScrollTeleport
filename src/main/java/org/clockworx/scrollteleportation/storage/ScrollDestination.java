package org.clockworx.scrollteleportation.storage;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.configuration.ConfigurationSection;
import org.clockworx.scrollteleportation.ScrollTeleportation;
import org.clockworx.scrollteleportation.files.LanguageString;
import org.clockworx.scrollteleportation.exceptions.DestinationInvalidException;

import java.util.List;
import java.util.Optional;
import java.util.Random;
import java.util.logging.Level;

/**
 * Represents a destination for scroll teleportation.
 * This class handles different types of destinations and their validation.
 */
public class ScrollDestination {

    private final ScrollTeleportation plugin;
    private final String name;
    private DestinationType type;
    private Location location;
    private int range;
    private final Random random;
    private String locationString;

    /**
     * Creates a new ScrollDestination instance.
     * 
     * @param plugin The plugin instance
     * @param name The name of the destination
     * @param type The type of destination
     * @param location The location for fixed destinations
     * @param range The range for random destinations
     */
    public ScrollDestination(ScrollTeleportation plugin, String name, DestinationType type, Location location, int range) {
        this.plugin = plugin;
        this.name = name;
        this.type = type;
        this.location = location;
        this.range = range;
        this.random = new Random();
    }

    /**
     * Creates a ScrollDestination from a configuration section.
     * 
     * @param plugin The plugin instance
     * @param section The configuration section
     * @return The created ScrollDestination
     * @throws DestinationInvalidException if the destination is invalid
     */
    public static ScrollDestination fromConfig(ScrollTeleportation plugin, ConfigurationSection section) throws DestinationInvalidException {
        String name = section.getName();
        String typeStr = section.getString("type", "FIXED_LOCATION");
        DestinationType type = DestinationType.valueOf(typeStr.toUpperCase());

        Location location = null;
        int range = section.getInt("range", 100);

        if (type == DestinationType.FIXED_LOCATION) {
            String worldName = section.getString("world");
            if (worldName == null) {
                throw new DestinationInvalidException("World not specified for fixed location destination");
            }

            World world = Bukkit.getWorld(worldName);
            if (world == null) {
                throw new DestinationInvalidException("World " + worldName + " not found");
            }

            double x = section.getDouble("x");
            double y = section.getDouble("y");
            double z = section.getDouble("z");
            float yaw = (float) section.getDouble("yaw", 0);
            float pitch = (float) section.getDouble("pitch", 0);

            location = new Location(world, x, y, z, yaw, pitch);
        }

        return new ScrollDestination(plugin, name, type, location, range);
    }

    /**
     * Creates a ScrollDestination from a location string.
     * 
     * @param locationString The location string
     * @return The created ScrollDestination
     * @throws DestinationInvalidException if the destination is invalid
     */
    public static ScrollDestination createFromLocationString(String locationString) throws DestinationInvalidException {
        if (locationString == null || locationString.trim().isEmpty()) {
            throw new DestinationInvalidException("Location string cannot be null or empty");
        }

        ScrollDestination destination = new ScrollDestination(ScrollTeleportation.getInstance(), "", DestinationType.FIXED_LOCATION, null, 0);
        destination.locationString = locationString.toLowerCase().trim();

        if (!locationString.contains(",")) {
            // Can either be a fixed name or random location
            if (locationString.contains("random")) {
                destination.setDestinationType(DestinationType.RANDOM);
            } else {
                destination.setDestinationType(DestinationType.FIXED_NAME);
                destination.setupFixedNameLocation(locationString);
            }
        } else {
            // It can either be random radius or a fixed location
            if (locationString.contains("random_radius")) {
                destination.setDestinationType(DestinationType.RANDOM_IN_RANGE);
                destination.setupRandomRadiusLocation(locationString);
            } else {
                destination.setDestinationType(DestinationType.FIXED_LOCATION);
                destination.setupFixedLocation(locationString);
            }
        }

        return destination;
    }

    /**
     * Gets the location for this destination.
     * For random destinations, this will generate a new random location.
     * 
     * @return The location for this destination
     */
    public Location getLocation() throws DestinationInvalidException {
        Location result = switch (type) {
            case FIXED_LOCATION -> location;
            case RANDOM -> getRandomLocation();
            case RANDOM_IN_RANGE -> getRandomLocationWithRadius();
            case FIXED_NAME -> getFixedNameLocation();
        };

        if (result == null) {
            throw new DestinationInvalidException("Failed to get location for destination type: " + type);
        }

        return secureLocation(result);
    }

    /**
     * Generates a random location within the configured range.
     * 
     * @return A random location
     */
    private Location getRandomLocation() throws DestinationInvalidException {
        World world;
        if (locationString.split(" ").length > 1) {
            String worldName = locationString.split(" ")[1];
            world = Bukkit.getWorld(worldName);
            if (world == null) {
                throw new DestinationInvalidException("World not found: " + worldName);
            }
        } else {
            List<World> worlds = Bukkit.getWorlds();
            if (worlds.isEmpty()) {
                throw new DestinationInvalidException("No worlds available for random location");
            }
            world = worlds.get(new Random().nextInt(worlds.size()));
        }

        int x = getRandomCoordinate(10000);
        int y = getRandomNumberRange(1, 250);
        int z = getRandomCoordinate(10000);

        return new Location(world, x, y, z);
    }

    /**
     * Generates a random location within the configured range of the fixed location.
     * 
     * @return A random location within range
     */
    private Location getRandomLocationWithRadius() throws DestinationInvalidException {
        if (location == null) {
            throw new DestinationInvalidException("Center location not set for random radius");
        }

        int x = location.getBlockX() + getRandomCoordinate(range);
        int z = location.getBlockZ() + getRandomCoordinate(range);
        int y = location.getWorld().getHighestBlockYAt(x, z);

        return new Location(location.getWorld(), x, y, z);
    }

    /**
     * Gets a location by name from the configuration.
     * 
     * @return The location with the specified name
     */
    private Location getFixedNameLocation() throws DestinationInvalidException {
        if (location == null) {
            throw new DestinationInvalidException("Fixed name location not set");
        }
        return location;
    }

    /**
     * Secures a location to prevent suffocation or falling.
     * 
     * @param location The location to secure
     * @return The secured location
     */
    public Location secureLocation(Location location) {
        if (location == null) {
            return null;
        }

        World world = location.getWorld();
        int x = location.getBlockX();
        int y = location.getBlockY();
        int z = location.getBlockZ();

        Block feetBlock = world.getBlockAt(x, y, z);
        Block headBlock = world.getBlockAt(x, y + 1, z);
        Block groundBlock = world.getBlockAt(x, y - 1, z);

        // Check if the location is safe (air for feet and head, solid ground)
        if (!feetBlock.getType().equals(Material.AIR) ||
            !headBlock.getType().equals(Material.AIR) ||
            groundBlock.getType().equals(Material.AIR)) {
            // Find the highest safe Y coordinate
            y = world.getHighestBlockYAt(x, z);
            return new Location(world, x, y, z);
        }

        return location;
    }

    /**
     * Gets a description of the location.
     * 
     * @return The location description
     */
    public String getLocationDescription() {
        return switch (type) {
            case FIXED_LOCATION -> getFixedLocationString();
            case RANDOM -> getRandomLocationString();
            case RANDOM_IN_RANGE -> getRandomRadiusLocationString();
            case FIXED_NAME -> getFixedNameLocationString();
        };
    }

    /**
     * Gets the name of this destination.
     * 
     * @return The destination name
     */
    public String getName() {
        return name;
    }

    /**
     * Gets the type of this destination.
     * 
     * @return The destination type
     */
    public DestinationType getType() {
        return type;
    }

    /**
     * Gets the range of this destination.
     * 
     * @return The destination range
     */
    public int getRange() {
        return range;
    }

    private void setupRandomRadiusLocation(String locationString) throws DestinationInvalidException {
        if (!locationString.contains("point")) {
            throw new DestinationInvalidException("No point specified in random radius location");
        }
        if (!locationString.contains("radius")) {
            throw new DestinationInvalidException("No radius specified in random radius location");
        }

        // Extract point and radius from format: random_radius(point=world,x,y,z radius=1000)
        String info = locationString.replaceAll(".*\\(|\\).*", "");
        String[] args = info.split(" ");

        if (args.length != 2) {
            throw new DestinationInvalidException("Invalid random radius format: " + locationString);
        }

        String point = args[0].contains("point=") ? args[0].replace("point=", "") : args[1].replace("point=", "");
        String radiusStr = args[0].contains("radius=") ? args[0].replace("radius=", "") : args[1].replace("radius=", "");

        try {
            this.range = Integer.parseInt(radiusStr);
        } catch (NumberFormatException e) {
            throw new DestinationInvalidException("Invalid radius: " + radiusStr);
        }

        String[] pointInfo = point.split(",");
        if (pointInfo.length != 4) {
            throw new DestinationInvalidException("Invalid point format: " + point);
        }

        World world = Bukkit.getWorld(pointInfo[0]);
        if (world == null) {
            throw new DestinationInvalidException("World not found: " + pointInfo[0]);
        }

        try {
            int x = Integer.parseInt(pointInfo[1]);
            int y = Integer.parseInt(pointInfo[2]);
            int z = Integer.parseInt(pointInfo[3]);
            this.location = new Location(world, x, y, z);
        } catch (NumberFormatException e) {
            throw new DestinationInvalidException("Invalid coordinates in point: " + point);
        }
    }

    private void setupFixedLocation(String locationString) throws DestinationInvalidException {
        String[] args = locationString.split(",");
        if (args.length != 4) {
            throw new DestinationInvalidException("Fixed location must be in format: world,x,y,z");
        }

        World world = Bukkit.getWorld(args[0].trim());
        if (world == null) {
            throw new DestinationInvalidException("World not found: " + args[0].trim());
        }

        try {
            int x = Integer.parseInt(args[1].trim());
            int y = Integer.parseInt(args[2].trim());
            int z = Integer.parseInt(args[3].trim());
            this.location = new Location(world, x, y, z);
        } catch (NumberFormatException e) {
            throw new DestinationInvalidException("Invalid coordinates in fixed location: " + locationString);
        }
    }

    private void setupFixedNameLocation(String locationString) throws DestinationInvalidException {
        if (locationString.contains("spawn")) {
            String[] params = locationString.split(" ");
            if (params.length < 2) {
                throw new DestinationInvalidException("Spawn location must specify a world");
            }

            World world = Bukkit.getWorld(params[1]);
            if (world == null) {
                throw new DestinationInvalidException("World not found: " + params[1]);
            }

            this.location = world.getSpawnLocation();
            return;
        }

        throw new DestinationInvalidException("Unknown fixed name location: " + locationString);
    }

    private int getRandomCoordinate(int range) {
        return new Random().nextBoolean() ? 
            getRandomNumberRange(1, range) : 
            -getRandomNumberRange(1, range);
    }

    private int getRandomNumberRange(int min, int max) {
        return new Random().nextInt(max - min + 1) + min;
    }

    private String getFixedLocationString() {
        return String.format("%d, %d, %d in %s",
            location.getBlockX(),
            location.getBlockY(),
            location.getBlockZ(),
            location.getWorld().getName());
    }

    private String getRandomLocationString() {
        return locationString.contains(" ") ?
            "random location in " + locationString.split(" ")[1] :
            "random location in any world";
    }

    private String getRandomRadiusLocationString() {
        return String.format("within %d blocks of %s",
            range,
            getFixedLocationString());
    }

    private String getFixedNameLocationString() {
        return locationString;
    }

    /**
     * Gets the location string.
     * 
     * @return The location string
     */
    public String getLocationString() {
        return locationString;
    }

    /**
     * Sets the location string.
     * 
     * @param locationString The location string to set
     */
    public void setLocationString(String locationString) {
        this.locationString = locationString;
    }

    /**
     * Sets the destination type.
     * 
     * @param type The destination type to set
     */
    public void setDestinationType(DestinationType type) {
        this.type = type;
    }

    /**
     * Sets the range.
     * 
     * @param range The range to set
     */
    public void setRange(int range) {
        this.range = range;
    }

    /**
     * Sets the location.
     * 
     * @param location The location to set
     */
    public void setLocation(Location location) {
        this.location = location;
    }

    /**
     * Enum representing the different types of destinations.
     */
    public enum DestinationType {
        FIXED_LOCATION,
        RANDOM,
        RANDOM_IN_RANGE,
        FIXED_NAME
    }
} 