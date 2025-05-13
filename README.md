# YACA Carpet Rule Documentation

**Yet Another Carpet Addition (YACA)** is an extension for the Carpet Mod. It provides a variety of debugging tools,
performance optimizations, and game mechanic adjustments. Below is a full list of rules and their descriptions.

[中文版/ChineseVersion](./README-zn_ch.md)

![owo](https://count.getloli.com/@YACAC?name=YACAC&theme=3d-num&padding=7&offset=0&align=top&scale=1&pixelated=1&darkmode=auto)
---

## 🧭Main Highlight Rules

### `enableTickStepCounter` - Tick Step Counter

* **Description**: Enables a tick counter when `/tick freeze` is active, allowing you to track how many game ticks have
  advanced.
* **Use Case**: Useful for analyzing game progress while the game is frozen.

### `UnicodeArgumentSupport` - Unicode Argument Support

* **Description**: Commands now support non-English characters as arguments.

### `commandEnhance` - Command Experience Enhancement

* **Description**: Improves the usability of certain commands.

Currently supported:

• Hovering over the feedback of the `/kill` command shows what was killed and how many of each.

• Hovering over the feedback of the `/data` command shows the specific changes made to the data.

• The feedback of the `/data get` command is now more readable; clicking on the NBT key name copies the corresponding `/data modify` command.


---

## 🌍World Update Freeze Rules

### `stopCheckEntityDespawn` - Stop Entity Despawn Check

* **Description**: Prevents the game from automatically despawning entities.

### `stopTickingEntities` - Stop Entity Ticking

* **Description**: Prevents entities like mobs, animals, and players from updating each tick.

### `stopTickingBlockEntities` - Stop Block Entity Ticking

* **Description**: Freezes block entities like chests, furnaces, etc., from updating.

### `stopTickingSpawners` - Stop Spawner Ticking

* **Description**: Halts mob spawning behavior.

### `stopTickingWorldBorder` - Stop World Border Updates

* **Description**: Prevents world border state from updating every tick.

### `stopTickingWeather` - Stop Weather Updates

* **Description**: Disables weather systems like rain and thunderstorms.

### `stopTickingTime` - Stop Time Progression

* **Description**: Pauses the day/night cycle.

### `stopTickingBlocks` - Stop Block Updates

* **Description**: Freezes scheduled and random ticks such as crop growth and redstone updates.

### `stopTickingFluids` - Stop Fluid Updates

* **Description**: Prevents fluid flow updates for water, lava, etc.

### `stopTickingRaid` - Stop Raid Updates

* **Description**: Pauses the progression of village raids.

### `stopTickingChunkManager` - Stop Chunk Manager Updates

* **Description**: Stops chunk loading and unloading logic from running.

### `stopTickingBlockEvents` - Stop Block Event Updates

* **Description**: Freezes block event queues (e.g., piston behavior).

### `stopTickingDragonFight` - Stop Ender Dragon Fight Updates

* **Description**: Pauses the dragon fight sequence in the End.

---

## 🔧Command Enhancements and Utilities

### `bypassModifyPlayerDataRestriction` - Bypass Player Data Restrictions

* **Description**: Allows modification of player NBT using the `/data` command.
* **Note**: Experimental; may cause unexpected behavior.

### `bypassCrashForcibly` - Forcibly Prevent Crashes

* **Description**: Attempts to forcibly catch all exceptions during server ticks to avoid crashes.
* **Note**: May interfere with proper crash debugging.

### `enableMountPlayers` - Enable Player Mounting

* **Description**: Allows entities to ride players and vice versa.
* **Note**: Experimental; may cause crashes or weird behavior.

### `enchantCommandLimitOverwrite` - Enchant Command Level Cap Removal

* **Description**: Allows using `/enchant` with enchantment levels up to 255.

### `enchantCommandBypassItemType` - Enchant Any Item

* **Description**: Removes item-type restrictions from `/enchant`.

### `mergeSmartAndRegularCommandSuggestions` - Merge Command Suggestions

* **Description**: Merges smart and regular command suggestions for Carpet rule commands.

### `silenceTP` - Silent Teleportation

* **Description**: Temporarily sets real players to spectator mode during teleport to avoid detection.

### `commandEasyItemShadowing` - Easy Item Shadowing

* **Description**: Use `/itemshadowing` to quickly copy the main-hand item to the off-hand.

### `commandRenameItem` - Quick Rename Item

* **Description**: Use `/rename <name>` to rename the item in your hand. Pass an empty name to reset.

---

## 🧪 Dangerous Features (May Cause Crashes)

### `instantSchedule` - Instant Scheduled Ticks

* **Description**: Processes all scheduled tick events immediately.
* **Warning**: May cause crashes or world corruption.

### `instantFalling` - Instant Falling Blocks

* **Description**: Causes blocks like sand and gravel to fall instantly.

---

## 🏗Structure and Behavior Optimization

### `optimizedStructureBlock` - Optimize Structure Block

* **Description**: Improves performance of structure blocks when scanning corner regions.

### `morphMovingPiston` - Morphing Moving Piston

* **Description**: Improves accuracy of moving pistons simulating the behavior of held blocks.
* **Note**: Experimental; may behave unexpectedly.

### `movingPistonSpeed` - Moving Piston Speed

* **Description**: Controls the animation speed of piston movements.

---

## 🛏 Miscellaneous Functional Enhancements

### `bedsRecordSleeperFacing` - Beds Record Player Facing

* **Description**: Stores the direction players face when going to bed.

### `copyablePlayerMessages` - Copyable Player Messages

* **Description**: Allows chat messages to be copied (client-side enhancement).

### `moreHardCollisions` - More Hard Collisions

* **Description**: Adds physical collision boxes between more entity types.
* **Note**: May behave unexpectedly or cause performance issues.

### `farlandReintroduced` - Far Lands Reintroduced

* **Description**: Brings back the Beta-era Far Lands world generation.

### `fallingSnowLayers` - Falling Snow Layers

* **Description**: Snow layers fall under gravity like sand blocks.

### `blocksNoSelfCheck` - Blocks No Self Check
* **Description**: Disable OnBlockAdded() logic for blocks

### `blocksNoSuffocate` - Blocks No Suffocate
* **Description**: Prevents blocks from causing suffocation - Disable

### `blocksPlaceAtAnywhere` -  Blocks Place At Anywhere
* **Description**: Allows placing blocks in invalid or normally restricted locations

### `blockNoBreakParticles` - Block No Breaking Particles
* **Description**: No particles will spawn when breaking blocks


---

## 🌟Visualization Tools

### `scheduledTickVisualize` - Visualize Scheduled Ticks

* **Description**: Renders scheduled tick data in the world (Red = time to trigger, Green = priority, Blue = creation
  order).

### `hopperCooldownVisualize` - Hopper Cooldown Visualization

* **Description**: Displays the cooldown state of each hopper.

### `randomTickVisualize` - Random Tick Visualization

* **Description**: Shows positions hit by random ticks.

### `gameEventVisualize` - Game Event Visualization

* **Description**: Visualizes events like footsteps and block interactions.

### `blockEventVisualize` - Block Event Visualization

* **Description**: Visualizes the execution order of block events like pistons.

### `blockUpdateVisualize` - NC Update Visualization

* **Description**: Displays where NC (Neighbor Changed) updates occur.

### `stateUpdateVisualize` - PP Update Visualization

* **Description**: Displays where PP (Post-processing) updates occur.

### `comparatorUpdateVisualize` - Comparator Update Visualization

* **Description**: Displays where comparator updates occur.

---

## 🔦 Light Control Features

### `forceMaxLightLevel` - Force Max Light Level

* **Description**: Sets both block and sky light levels to 15 everywhere.
* **Note**: May cause rendering or gameplay issues.

### `disableLightUpdate` - Disable Light Updates

* **Description**: Prevents light propagation and recalculations, making lighting static.
* **Note**: May cause inconsistencies.

---

## Commands

### `/scheduleTick` Command Format

```
/scheduleTick <pos> <block> <time> <priority>
```

* `pos`: Position of the target block (e.g., `0 64 0`)
* `block`: Block state (e.g., `minecraft:stone`)
* `time`: Delay time (integer, ≥ 0)
* `priority`: Priority level (range: -3 to 3)

### `/blockEvent` Command Format

```
/blockEvent <pos> <block> <type> <data>
```

* `pos`: Position of the target block
* `block`: Block state
* `type`: Event type (0\~2)
* `data`: Additional data (0\~5)

### `/randomTick` Command Format

```
/randomTick <pos>
```

* `pos`: Block position

### `/gameEvent` Command Format (Supports Parameter Combinations)

```
/gameEvent <pos> <reason> [entity] [blockstate]
```

Supported combinations:

1. `<pos> <reason>` only
2. `<pos> <reason> <entity>`
3. `<pos> <reason> <blockstate>`
4. `<pos> <reason> <entity> <blockstate>`

* `pos`: Event position (3D coordinates, e.g., `0.5 64.0 0.5`)
* `reason`: Event name (string)
* `entity`: Entity triggering the event (optional)
* `blockstate`: Block state (optional)

### `/worldEvent` Command Format (Two Forms Supported)

1. With player parameter:

```
/worldEvent <player> <pos> <event> <data>
```

2. Without player parameter:

```
/worldEvent <pos> <event> <data>
```

* `player`: Player entity (optional)
* `pos`: Event location
* `event`: Event name (suggested values supported)
* `data`: Additional data (integer)

### `/rename` Command Format (For Renaming Items)

Two usage options:

1. Specify a name:

```
/rename <name>
```

* `name`: New name (string, can include spaces, must be quoted)

2. No parameters:

```
/rename
```

* Reverts to default name

---

### `/itemshadowing` Command Format

```
/itemshadowing
```

* Clones the item in the main hand to the off-hand

---

### `/waypoint` Command Format

#### Subcommand: `/waypoint save` (Save a New Waypoint)

1. **Save a waypoint at the player's current position:**

   ```
   /waypoint save <name>
   ```

   ```
   /waypoint save <name> <pos>
   ```

   * `name`: Waypoint name (string)
   * `pos`: Waypoint position (block coordinates)

#### Subcommand: `/waypoint remove` (Remove a Named Waypoint)

```
/waypoint remove <name>
```

* `name`: Name of the waypoint to remove (string)

#### Subcommand: `/waypoint tp` (Teleport to a Named Waypoint)

```
/waypoint tp <name>
```

* `name`: Waypoint name (string)

#### Subcommand: `/waypoint list` (List All Saved Waypoints)

```
/waypoint list
```

* No parameters. Displays all saved waypoint names as clickable links. Clicking a name teleports the player to that waypoint.

---

## 📱GUI Features

### Carpet Rule GUI (Default: F9)

* **Description**: GUI interface for managing Carpet rules.
  ![img.png](carpetGUI.png)
* **Note**: Requires both client and server to have YACA installed.

### Hopper Counter GUI (/counterGUI)

* **Description**: GUI interface for the hopper counter system.
  ![img.png](counterGUI.png)

* **Setup**:

    * Set the `HopperCounterDataRecorder` rule to any number other than `off` — this number defines how often (in ticks)
      hopper data is recorded.
    * Ensure the `hopperCounters` rule is enabled.

* **Warning**: This is a beta feature and may have bugs.

    * After use, disable `HopperCounterDataRecorder` by setting it to `off`.
    * *(Optional)*: Clear hopper counter data to remove stored cache.

