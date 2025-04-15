# Scroll Teleportation

A modern Minecraft plugin that allows players to use scrolls for teleportation with various features and customization options.

## Features

- **Scroll-based Teleportation**: Create and use scrolls to teleport to different locations
- **Multiple Destination Types**:
  - Fixed locations
  - Random locations
  - Random locations within a radius
  - Named locations (like spawn)
- **Customizable Scrolls**:
  - Custom display names and lore
  - Configurable teleport delay
  - Limited or infinite uses
  - Optional potion effects on teleportation
  - Hidden destinations for mystery scrolls
- **Safety Features**:
  - Cancel teleportation on movement
  - Safe teleportation to prevent suffocation
  - Permission-based access control
  - World and region restrictions

## Requirements

- Paper/Spigot 1.21 or higher
- Java 17 or higher

## Installation

1. Download the latest release from the [releases page](https://github.com/Clockworx/ScrollTeleportation/releases)
2. Place the JAR file in your server's `plugins` folder
3. Restart your server or use a plugin manager to load the plugin
4. Configure the plugin in `plugins/ScrollTeleportation/config.yml`

## Commands

- `/scrolltp help` - Shows help information
- `/scrolltp reload` - Reloads the plugin configuration
- `/scrolltp give <scroll> [player]` - Gives a scroll to a player
- `/scrolltp create <name> <displayName> <delay> <uses>` - Creates a new scroll
- `/scrolltp set <var> <scroll> <result>` - Sets a scroll variable

## Permissions

- `scrollteleportation.teleport` - Allows a player to use scrolls
- `scrollteleportation.give` - Allows a player to give scrolls
- `scrollteleportation.walkbypass` - Players with this permission can walk while teleports are being casted
- `scrollteleportation.invbypass` - Players with this permission can open inventories while teleports are being casted
- `scrollteleportation.delaybypass` - Players with this permission can bypass delays
- `scrollteleportation.usesbypass` - Players with this permission can bypass uses
- `scrollteleportation.potioneffectbypass` - Players with this permission can bypass potion effects
- `scrollteleportation.set` - Allows a player to set scroll variables
- `scrollteleportation.create` - Allows a player to create scrolls
- `scrollteleportation.reload` - Allows a player to reload config files

## Configuration

The plugin can be configured in `plugins/ScrollTeleportation/config.yml`. Here's an example configuration:

```yaml
Scroll:
  material: PAPER

Scrolls:
  spawn_scroll:
    name: "&6Scroll of Spawn"
    lores:
      - "&7Teleports you to spawn"
      - ""
      - "&7Common scroll"
    destination: "spawn world"
    destination_hidden: false
    delay: 3
    cancel_on_move: true
    uses: 1
    effects:
      - "BLINDNESS 3"
```

## License

This project is licensed under the MIT License - see the [LICENSE](LICENSE) file for details.

## Contributing

Contributions are welcome! Please feel free to submit a Pull Request. For more information, see the [CONTRIBUTING](CONTRIBUTING.md) file. 