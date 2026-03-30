## Advanced Random Teleport Plugin

![Minecraft Versions](https://ezbanners.org/shields/plugins/e5661a39-5da1-4f4e-aabb-8d93e769b646/minecraft-versions.png)
![Downloads](https://ezbanners.org/shields/plugins/e5661a39-5da1-4f4e-aabb-8d93e769b646/downloads.png)
![Server Software](https://ezbanners.org/shields/plugins/e5661a39-5da1-4f4e-aabb-8d93e769b646/server-software.png)

**Production-ready random teleport plugin for modern Minecraft servers**

**Latest release:** See the GitHub project for the current release and up-to-date documentation: https://github.com/ez-plugins/EzRTP
Java **17+**, Bukkit API baseline **1.13+**, actively packaged for Bukkit/Paper/Spigot/Purpur.

[📥 Modrinth](https://modrinth.com/plugin/ezplugins-ezrtp) • [📥 SpigotMC](https://www.spigotmc.org/resources/1-19-1-21-%E2%AC%85%EF%B8%8F-ezrtp-%E2%9E%A1%EF%B8%8F-highly-configurable-rtp-plugin-for-minecraft-servers.129828/) • [📥 Hangar](https://hangar.papermc.io/EzPlugins/EzRTP) • [📕 GitHub (latest releases & docs)](https://github.com/ez-plugins/EzRTP) • [💬 Discord Support](https://discord.gg/yWP95XfmBS) • [🐛 Report Issues](https://discord.gg/yWP95XfmBS)


[![ez rtp plugin documentation button](https://i.ibb.co/dskCvgLP/documentation-button-1.png)](https://github.com/ez-plugins/EzRTP/blob/main/README.md)
[![ez rtp plugin support button](https://i.ibb.co/Wpy2w1cH/support-button-1.png)](https://discord.gg/yWP95XfmBS)

---

## ✨ Key Features

<div align="center">

| 🚀 Performance | 🛡️ Safety | 🎨 Customization | 💰 Economy | 🌐 Multi-World |
|:--------------:|:---------:|:----------------:|:----------:|:--------------:|
| Lightning-fast RTP with intelligent caching | World-border aware with biome filtering | MiniMessage chat output | Vault integration | Permission-gated GUI selector |

</div>

### 🎯 Core Capabilities

- **🏠 Safe Random Teleportation**: Finds solid ground within world borders, avoids unsafe blocks, reports coordinates with rich formatting
- **🌍 Multi-World Support**: Curated destinations for overworld, nether, end, resource worlds with GUI selection
- **🏞️ Advanced Biome Selection**: Target specific biomes or exclude unwanted areas for tailored exploration
- **👥 Join Protection**: Automatic teleportation on join with configurable delays and bypass permissions
- **💰 Economy Integration**: Charge for teleports with Vault, refund on failures, localized messaging
- **⚡ Performance Queue**: Throttle usage with configurable queues and bypass permissions
- **📊 Comprehensive Analytics**: RTP statistics, biome metrics, heatmap visualization, and performance monitoring

### 🛠️ Technical Excellence

- **🔧 Plugin Compatibility**: Paper, Purpur, SpigotMC 1.19+
- **☕ Java Requirements**: Java 17 or newer
- **💾 Storage Options**: YAML or MySQL for usage limits and statistics
- **🌐 Network Ready**: BungeeCord/Velocity support for multi-server networks
- **🔒 Security**: Protection plugin integration (WorldGuard, GriefPrevention)
- **📈 Metrics**: bStats integration for anonymous usage statistics

---

## 🚀 Quick Start Guide

> **Prerequisites**: Bukkit/Paper/Spigot/Purpur, Java 17+

### Installation Steps

1. **Download** `EzRTP.jar` from [Modrinth](https://modrinth.com/plugin/ezplugins-ezrtp), [SpigotMC](https://www.spigotmc.org/resources/1-19-1-21-%E2%AC%85%EF%B8%8F-ezrtp-%E2%9E%A1%EF%B8%8F-highly-configurable-rtp-plugin-for-minecraft-servers.129828/), [Hangar](https://hangar.papermc.io/EzPlugins/EzRTP) or the project's **GitHub Releases** (https://github.com/ez-plugins/EzRTP/releases)
2. **Install** by dropping into your `plugins/` directory
3. **Restart** your server to generate configuration files
4. **Configure** `config.yml` with your world settings, teleport parameters, and preferences
5. **Customize** messages in `messages/{language}.yml` (for example `messages/en.yml`) using MiniMessage formatting
6. **Enable** GUI in `gui.yml` for visual world selection
7. **Test** with `/rtp` command

### Expansions / Modules

EzRTP ships as a core runtime plus optional platform modules and integrations. For production servers:

Platform modules and other expansion jars (GUI/analytics helpers) are available from the project's GitHub Releases and the platform package pages listed above. See the module installation notes on GitHub: https://github.com/ez-plugins/EzRTP/blob/main/docs/overview-installation.md

### Basic Configuration

```yaml
# Essential settings in config.yml
world: world
center:
  x: 0
  z: 0
radius:
  min: 256
  max: 2048
  use-world-border: true
max-attempts: 32
cost: 0.0  # Free teleports
```

### Commands & Permissions

| Command | Description | Permission | Default |
|---------|-------------|------------|---------|
| `/rtp` | Random teleport to safe location | `ezrtp.use` | ✅ All players |
| `/rtp reload` | Reload configuration | `ezrtp.reload` | ❌ Operators only |
| `/rtp stats` | View RTP performance metrics | `ezrtp.stats` | ❌ Operators only |
| `/rtp stats biomes` | Detailed biome statistics | `ezrtp.stats` | ❌ Operators only |
| `/rtp heatmap [size] [world]` | Generate RTP distribution map | `ezrtp.heatmap` | ❌ Operators only |
| `/forcertp <player> [world]` | Admin teleport override | `ezrtp.forcertp` | ❌ Operators only |
| `/rtp fake <amount>` | Simulate teleports for testing | `ezrtp.heatmap.fake` | ❌ Operators only |

**Additional Permissions:**
- `ezrtp.queue.bypass` - Skip teleport queues
- `ezrtp.bypass.cooldown` - Ignore cooldowns
- `ezrtp.bypass.limit` - Unlimited daily/weekly usage
- `ezrtp.gui.*` - Access specific GUI world options

**Permissions Reference:**
- `ezrtp.use`: Execute `/rtp` (default: all players)
- `ezrtp.reload`: Reload configuration (default: ops)
- `ezrtp.stats`: View RTP statistics and metrics (default: ops)
- `ezrtp.heatmap`: Generate heatmaps (default: ops)
- `ezrtp.forcertp`: Force RTP for a player (default: ops)
- `ezrtp.heatmap.fake`: Simulate teleports for testing (default: ops)
- `ezrtp.queue.bypass`: Bypass teleport queue (default: false)
- `ezrtp.bypass.cooldown`: Bypass cooldowns (default: false)
- `ezrtp.bypass.limit`: Bypass daily/weekly limits (default: false)
- `ezrtp.gui.*`: Per-option GUI access (use `gui.yml` to configure)
- `ezrtp.admin`: Administrative UI/cache visibility and advanced actions (default: ops)

Tip: Add bypass nodes to the `rtp-limits.bypass-permissions` list in `config.yml` to grant global bypass behavior.

---

## 🎮 Feature Showcase

---

## 📊 Advanced Analytics & Monitoring

<div align="center">

#### Biome Statistics Dashboard
View comprehensive biome-specific metrics with `/rtp stats biomes`

![Biome Statistics](https://i.ibb.co/YBy2z8vZ/image.png)

#### Heatmap Visualization
Generate professional RTP distribution maps with configurable grid sizes

![RTP Heatmap](https://i.ibb.co/wZWfpn7B/image.png)

*Professional yellow-to-red gradient • Auto-scaling • Configurable resolution (16-4096 blocks)*

</div>

**Usage Examples:**
```bash
/rtp stats              # Overall performance metrics
/rtp stats biomes       # Per-biome success rates & timing
/rtp heatmap 64 world   # Generate 64-block grid heatmap
/rtp fake 1000          # Simulate 1000 teleports for testing
```

### 🎨 Interactive GUI System

<div align="center">

#### World & Biome Selector
Permission-gated inventory interface with custom icons and real-time status

![World Selector GUI](https://i.ibb.co/fGCZwJFL/image.png)

*Custom icons • Permission controls • Real-time cooldown display*

</div>

**Configuration:**
```yaml
gui:
  enabled: true
  title: "<gradient:#00b4db:#0083b0><bold>Random Teleport</bold></gradient>"
  worlds:
    overworld:
      slot: 11
      permission: ""
      icon:
        material: GRASS_BLOCK
        name: "<green>🌍 Overworld</green>"
```

### 🛡️ Safety & Performance Features

#### Smart Location Validation
- **Block Safety**: Configurable unsafe block filtering
- **Chunk Management**: Automatic loading/unloading with memory optimization
- **Biome Awareness**: Include/exclude specific biomes
- **World Border**: Respects configured borders automatically

#### Intelligent Caching System
- **Biome Pre-caching**: Cache safe locations per biome for instant teleports
- **Rare Biome Optimization**: Weighted algorithms for efficient rare biome targeting
- **Hotspot Tracking**: Maintains performance data for optimal search patterns

### 💰 Economy Integration

#### Vault-Compatible Pricing
- **Flexible Costs**: Per-world, per-group pricing with overrides
- **Refund System**: Automatic refunds on teleport failures
- **Localized Messages**: Multi-language support for economy feedback

### 🌐 Network & Multi-World Support

#### BungeeCord/Velocity Integration
- **Server Transfers**: GUI options for cross-server teleports
- **Status Monitoring**: Real-time ping and player count display
- **Permission Controls**: Granular access control per server

---

## ⚙️ Advanced Configuration

### 📁 Configuration Files Overview

EzRTP uses multiple configuration files for comprehensive customization:

- **`config.yml`** - Core teleport settings, safety, performance, and economy
- **`messages/{language}.yml`** - MiniMessage formatted chat messages and localization
- **`gui.yml`** - Interactive world/biome selector GUI configuration
- **`queue.yml`** - Teleport queue management and throttling
- **`network.yml`** - BungeeCord/Velocity multi-server integration
- **`force-rtp.yml`** - Admin override settings

### 🔧 Core Configuration (`config.yml`)

<details>
<summary>📋 Complete config.yml Example</summary>

```yaml
# EzRTP teleport configuration
# Set to true to enable debug logging for rejected random teleport locations (for troubleshooting safe location logic).
debug-rejection-logging: false
# Enable or disable bStats metrics (https://bstats.org/plugin/bukkit/EzRTP/27735)
# These anonymous metrics help track plugin usage and performance across servers.
enable-bstats: true
# Optional: restrict random teleport Y-levels (inclusive). If omitted, world min/max height is used.
# min-y: 64
# max-y: 120

# Target world for RTP
world: world
# Center coordinates for RTP search area
center:
  x: 0
  z: 0
# Teleport radius bounds
radius:
  min: 256
  # max: 2048
  use-world-border: true
# Maximum attempts to find a safe location
max-attempts: 32
# Cost per teleport (0 = free)
cost: 0.0
# Optional countdown before teleport (0 = disabled)
countdown-seconds: 0

# Countdown configuration with bossbar and particles
countdown:
  bossbar:
    enabled: false
    title: "<yellow>Teleporting in <white><seconds></white> seconds...</yellow>"
    color: YELLOW
    style: SOLID
  particles:
    enabled: true
    type: ENCHANTMENT_TABLE
    points: 12
    radius: 1.2
    height-offset: 0.8
    extra: 0.0
    force: false
    secondary-particle: PORTAL
    secondary-count: 6
    secondary-offset: 0.35

# Blocks to avoid during location validation
unsafe-blocks:
  - WATER
  - LAVA
  - MAGMA_BLOCK
  - POWDER_SNOW
  - FIRE
  - CAMPFIRE
  - SOUL_FIRE

# Biome filtering and optimization
biomes:
  # Optional: list of allowed biomes. Leave empty to allow all.
  include: []
  # Optional: list of biomes to avoid.
  exclude: []
  # Pre-caching configuration for biome-filtered RTPs
  pre-cache:
    enabled: true
    max-per-biome: 50
    warmup-size: 20
    expiration-minutes: 10
  # Rare biome optimization
  rare-biome-optimization:
    enabled: true
    rare-biomes: []
    use-weighted-search: true
    enable-hotspot-tracking: true

# Protection plugin integration
protection:
  avoid-claims: false
  providers:
    - worldguard
    - griefprevention

# Landing particle effects
particles:
  enabled: false
  type: PORTAL
  count: 40
  offset:
    x: 0.5
    y: 1.0
    z: 0.5
  extra: 0.0
  force: false

# Join teleportation settings
on-join:
  enabled: false
  only-first-join: false
  bypass-permission: ""
  delay-ticks: 40

# RTP cooldowns and usage limits
rtp-limits:
  allow-gui-during-cooldown: true
  default:
    cooldown-seconds: 300   # Cooldown between uses
    daily-limit: 10         # Max uses per day
    weekly-limit: 50        # Max uses per week
    cost: 0.0               # Cost override
  worlds:
    world:
      disable-daily-limit: false
      default:
        cooldown-seconds: 300
        daily-limit: 10
        weekly-limit: 50
        cost: 0.0
      group.vip:
        cooldown-seconds: 60
        daily-limit: 50
        weekly-limit: 200
        cost: 0.0
  bypass-permissions:
    - ezrtp.bypass.cooldown
    - ezrtp.bypass.limit
  storage: yaml # or mysql
  mysql:
    url: jdbc:mysql://localhost:3306/mc
    user: root
    password: ""
```

</details>

### 💬 Message Configuration (`messages/{language}.yml`)

<details>
<summary>📋 Complete messages/{language}.yml Example</summary>

```yaml
# EzRTP MiniMessage templates
# Placeholders: <world>, <x>, <z>, <position>, <server>, <cost>, <target-biome>, <cached-locations>
teleporting: "<gray>Searching for a safe location...</gray>"
teleport-success: "<green>Teleported to <white><x></white>, <white><z></white> in <white><world></white>.</green>"
teleport-failed: "<red>Unable to find a safe location. Please try again.</red>"
teleport-failed-biome: "<red>No valid biome was found. Please try again or try a different biome filter.</red>"
world-missing: "<red>The configured world '<white><world></white>' is not available.</red>"
join-searching: "<gray>Finding you a safe place to explore...</gray>"
insufficient-funds: "<red>You need <white><cost></white> to use random teleport.</red>"
queue-queued: "<gray>You joined the random teleport queue. Position: <white><position></white>.</gray>"
queue-full: "<red>The random teleport queue is currently full. Please try again soon.</red>"

# Cooldown and usage limit messages
cooldown: "<red>You must wait <white><hours></white> <white><minutes></white> <white><seconds></white> before using /rtp again.</red>"
limit-daily: "<red>You have reached your daily /rtp limit for this world.</red>"
limit-weekly: "<red>You have reached your weekly /rtp limit for this world.</red>"

# Countdown messages
countdown-start: "<yellow>Teleporting in <white><seconds></white> seconds...</yellow>"
countdown-tick: "<gray><seconds>...</gray>"
```

</details>

### 🎨 GUI Configuration (`gui.yml`)

<details>
<summary>📋 Complete gui.yml Example</summary>

```yaml
# EzRTP GUI configuration
enabled: false
title: "<gradient:#00b4db:#0083b0><bold>Random Teleport</bold></gradient>"
rows: 3
no-permission-message: "<red>You do not have permission to travel there.</red>"
# Only show cache info (biome counts) to players with ezrtp.admin permission
admin-only-cache-info: false
# Disable cache filtering entirely (show all biomes regardless of cache status)
disable-cache-filtering: true
# Supports PlaceholderAPI placeholders in item names and lore (e.g., %player_name%)
filler:
  enabled: true
  material: BLACK_STAINED_GLASS_PANE
  name: "<dark_gray> </dark_gray>"

worlds:
  overworld:
    slot: 13
    permission: ""
    icon:
      material: GRASS_BLOCK
      name: "<gradient:#7ed957:#2ecc71><bold>Overworld</bold></gradient>"
      lore:
        - "<gray>Explore the vast overworld safely!</gray>"
        - "<dark_gray>Click to begin your adventure.</dark_gray>"
    settings:
      world: world
      radius:
        min: 256
        use-world-border: true

  nether:
    slot: 11
    permission: "ezrtp.gui.nether"
    icon:
      material: NETHERRACK
      name: "<gradient:#ff6b6b:#ee5a52><bold>Nether</bold></gradient>"
      lore:
        - "<gray>Dangerous dimension with rare resources.</gray>"
        - "<red>Requires permission to access.</red>"
    settings:
      world: world_nether
      radius:
        min: 128
        max: 512

  end:
    slot: 15
    permission: "ezrtp.gui.end"
    icon:
      material: END_STONE
      name: "<gradient:#a29bfe:#6c5ce7><bold>The End</bold></gradient>"
      lore:
        - "<gray>The mysterious end dimension.</gray>"
        - "<dark_purple>Face the Ender Dragon!</dark_purple>"
    settings:
      world: world_the_end
      radius:
        min: 64
        max: 256
```

</details>

### 📋 Queue Configuration (`queue.yml`)

<details>
<summary>📋 Complete queue.yml Example</summary>

```yaml
# EzRTP teleport queue configuration
enabled: false
max-size: 0
bypass-permission: "ezrtp.queue.bypass"
start-delay-ticks: 20
interval-ticks: 40
```

</details>

### 🌐 Network Configuration (`network.yml`)

<details>
<summary>📋 Complete network.yml Example</summary>

```yaml
# EzRTP proxy / network configuration
enabled: false
lobby: false
ping-interval-ticks: 200
ping-timeout-millis: 1500

servers:
  skyblock:
    bungee-server: "skyblock"
    host: "127.0.0.1"
    port: 25566
    slot: 4
    permission: ""
    display-name: "Skyblock"
    hide-when-offline: false
    allow-when-offline: false
    connect-message: "<gray>Connecting you to <white><server></white>...</gray>"
    offline-message: "<red><server></red> is currently unavailable."
    icon:
      material: ENDER_PEARL
      name: "<gold><server></gold>"
      lore:
        - "<gray>Status: <status></gray>"
        - "<gray>Ping: <white><ping></white>ms</gray>"
        - "<gray>Players: <white><online></white>/<white><max></white></gray>"
```

</details>

---

## 🔧 Advanced Features & Configuration

### 🚀 Performance & Caching

#### Smart Biome Pre-Caching
Automatically cache pre-validated safe locations per biome to dramatically improve success rates and reduce lag when biome filters are active.

**Configuration:** `biomes.pre-cache` section in `config.yml`
- `enabled: true` - Toggle pre-caching on/off
- `max-per-biome: 50` - Maximum cached locations per biome
- `warmup-size: 20` - Locations pre-generated on startup
- `expiration-minutes: 10` - Cache validity duration

#### Rare Biome Optimization
Intelligent search strategies prioritize rare biome hotspots while maintaining randomness with configurable weighted algorithms.

**Configuration:** `biomes.rare-biome-optimization` section in `config.yml`
- `enabled: true` - Toggle optimization on/off
- `rare-biomes: []` - Custom rare biome list (empty = auto-detect)
- `use-weighted-search: true` - Favor hotspots for rare biomes
- `enable-hotspot-tracking: true` - Track and maintain hotspots

#### Chunky World Pre-Generation Integration
Automatically integrate with Chunky plugin for intelligent world pre-generation, ensuring RTP locations are always available in generated chunks.

**Key Features:**
- **Automatic Detection**: Detects Chunky plugin and integrates seamlessly
- **Smart Pre-Generation**: Triggers chunk generation for RTP target areas when needed
- **Queue Management**: Handles pre-generation requests without blocking RTP operations
- **Performance Optimized**: Asynchronous processing to maintain server performance

**Commands:**
- `/rtp pregenerate [world] [radius]` - Manually trigger pre-generation for RTP areas
- `/rtp pregenerate status` - Check pre-generation queue and progress

**Configuration:** `chunky-integration` section in `config.yml`
- `enabled: true` - Toggle Chunky integration on/off
- `pregenerate-radius: 256` - Radius for pre-generation around RTP targets
- `max-queue-size: 10` - Maximum queued pre-generation tasks
- `priority: NORMAL` - Pre-generation task priority (LOWEST, LOW, NORMAL, HIGH, HIGHEST)

**Requirements:** [Chunky plugin](https://modrinth.com/plugin/chunky) must be installed and configured on the server.

### 📊 Analytics & Monitoring

#### RTP Statistics & Monitoring
Track RTP performance with comprehensive metrics showing success rates, cache hit rates, per-biome metrics, and failure causes.

**Commands:**
- `/rtp stats` - Overall performance metrics
- `/rtp stats biomes` - Detailed biome-specific statistics
- `/rtp stats rare-biomes` - Rare biome optimization analytics

**Configuration:** Statistics are automatically collected and displayed. No configuration required.

#### Heatmap Visualization
Generate in-game maps showing RTP location distribution with grid-based statistical analysis to verify randomness and detect clustering.

**Usage:** `/rtp heatmap [gridSize] [world]`

**Configuration:** Grid size configurable from 16-4096 blocks. Specify world parameter to analyze different worlds.

### 👑 Administration & Management

#### Reload Subcommand
Hot-swap config changes without kicking players or restarting the server for seamless configuration updates.

**Usage:** `/rtp reload`  
**Permissions:** `ezrtp.reload` (default: op)

#### ForceRTP Admin Override
Force random teleportation on specific players while bypassing cooldowns and limits for administrative control.

**Usage:** `/forcertp <player> [world]`  
**Permissions:** `ezrtp.forcertp` (default: op)  
**Configuration:** Configure force-rtp settings in `force-rtp.yml`

### ⏱️ Player Experience Enhancements

#### Configurable Teleport Countdown
Add an optional countdown before teleportation with bossbar and particle ring visuals for enhanced user experience.

**Configuration:** Set `countdown-seconds` in `config.yml` or use the detailed `countdown` block for bossbar and particle customization.

#### Human-Readable Cooldown Messages
Display cooldowns in readable format like "1h 30m 45s" instead of raw seconds using dedicated placeholders.

**Configuration:** Use `<hours>`, `<minutes>`, and `<seconds>` placeholders in cooldown messages in `messages/{language}.yml`.

#### Visible Cooldown Indicators
Show cooldown timers in GUI item lore so players can see remaining wait times immediately.

**Configuration:** Cooldown indicators are automatically enabled when GUI cooldown behavior is configured.

#### Flexible GUI Cooldown Behavior
Allow GUI opening during cooldowns with disabled options instead of blocking GUI access entirely.

**Configuration:** Set `allow-gui-during-cooldown: true` in `rtp-limits` section of `config.yml`.

### 🛡️ Advanced Safety & Controls

#### Y-Level and Debug Controls
Restrict random teleports to a min/max Y-level range and enable detailed debug logging for troubleshooting safe-location logic.

**Configuration:** Set `min-y` and `max-y` in `config.yml` for height restrictions. Enable `debug-rejection-logging` for detailed logging.

#### Protection-Aware Search
Optionally avoid protected regions/claims by integrating with popular protection plugins like WorldGuard or GriefPrevention.

**Configuration:** Set `protection.avoid-claims: true` and list protection providers in `config.yml`.

#### Join-Delay Scheduler
Wait a configurable number of ticks after login before teleporting so players can load resource packs or tutorials first.

**Configuration:** Set `on-join.delay-ticks` in `config.yml` and enable `on-join.enabled` for automatic teleportation on player join.

### 🎛️ Usage Limits & Economy

#### Per-Player Cooldowns, Limits, & Costs
Configure per-world, per-group cooldowns, daily/weekly usage limits, and optional cost overrides with permission-based bypass.

**Configuration:** Edit the `rtp-limits` section in `config.yml` with world-specific and group-specific settings. Supports YAML or MySQL storage.

#### Per-World Daily Limit Controls
Disable daily and weekly RTP limits per world for unlimited usage in specific worlds while maintaining limits elsewhere.

**Configuration:** Set `disable-daily-limit: true` in world-specific configurations within `rtp-limits.worlds` section.

### 🔌 Third-Party Integrations

#### Chunky World Pre-Generation
Automatic integration with Chunky plugin for intelligent world pre-generation, ensuring RTP locations are always available in generated chunks.

**Features:** Automatic detection, smart pre-generation, queue management, performance optimized  
**Commands:** `/rtp pregenerate [world] [radius]`, `/rtp pregenerate status`  
**Requirements:** [Chunky plugin](https://modrinth.com/plugin/chunky) installed and configured

#### PlaceholderAPI Integration
Use dynamic placeholders like `%player_name%` in GUI item names and lore for personalized teleport menus.

**Configuration:** Install PlaceholderAPI plugin and use placeholders in `gui.yml` item names and lore. No additional configuration needed.

#### Multi-Language Support
Language-specific message files with automatic fallback to English, supporting both MiniMessage and legacy color codes.

**Configuration:** Create language-specific files like `messages/en.yml`, `messages/de.yml` in the plugin folder for different languages.

#### Admin-Only Cache Visibility
Control biome cache information display to show stats only to administrators or all players.

**Configuration:** Set `admin-only-cache-info` in `gui.yml` to restrict cache information to players with appropriate permissions.


## Configuration Overview

<details>
<summary>config.yml</summary>

Candidate coordinate search pattern options: `random`, `circle`, `square`, `triangle`, `diamond`.
Configure with `search-pattern: <option>` in `config.yml` (for example `search-pattern: diamond`).

```yml
# EzRTP teleport configuration
# Set to true to enable debug logging for rejected random teleport locations (for troubleshooting safe location logic).
debug-rejection-logging: false
# Enable or disable bStats metrics (https://bstats.org/plugin/bukkit/EzRTP/27735)
# These anonymous metrics help track plugin usage and performance across servers.
enable-bstats: true
# Optional: restrict random teleport Y-levels (inclusive). If omitted, world min/max height is used.
# min-y: 64
# max-y: 120
world: world
center:
  x: 0
  z: 0
radius:
  min: 256
  # max: 2048
  use-world-border: true
max-attempts: 32
cost: 0.0
countdown-seconds: 0 # Optional countdown before teleport (0 = disabled)
countdown:
  # Optional: override countdown seconds here instead of countdown-seconds.
  # seconds: 5
  bossbar:
    enabled: false
    title: "<yellow>Teleporting in <white><seconds></white> seconds...</yellow>"
    color: YELLOW
    style: SOLID
  particles:
    enabled: true
    # Primary Bukkit particle to use for the countdown ring.
    type: ENCHANTMENT_TABLE
    # Number of points in the ring around the player.
    points: 12
    # Ring radius in blocks.
    radius: 1.2
    # Height offset from the player's feet.
    height-offset: 0.8
    # Additional speed/extra parameter passed to the particle effect.
    extra: 0.0
    # Force the particles to render for players at long distances (1.13+ servers).
    force: false
    # Optional secondary sparkle particle.
    secondary-particle: PORTAL
    secondary-count: 6
    secondary-offset: 0.35
unsafe-blocks:
  - WATER
  - LAVA
  - MAGMA_BLOCK
  - POWDER_SNOW
  - FIRE
  - CAMPFIRE
  - SOUL_FIRE
biomes:
  # Optional: list of allowed biomes. Leave empty to allow all.
  include: []
  # Optional: list of biomes to avoid.
  exclude: []
  # Pre-caching configuration for biome-filtered RTPs
  # When enabled, the server will pre-generate and cache safe locations for biome filters,
  # significantly improving RTP success rates and reducing failed attempts.
  pre-cache:
    # Toggle pre-caching on/off. Enabled by default for better RTP performance with biome filters.
    enabled: true
    # Maximum number of locations to cache per biome (higher = better hit rate but more memory)
    max-per-biome: 50
    # Number of locations to pre-generate during server startup/reload for each configured biome
    warmup-size: 20
    # How long (in minutes) cached locations remain valid before expiring
    expiration-minutes: 10
  # Rare biome optimization for improved search efficiency
  # When enabled, uses intelligent strategies to prioritize rare biome hotspots
  rare-biome-optimization:
    # Toggle rare biome optimization on/off
    enabled: true
    # Custom list of biomes considered "rare" (leave empty for built-in list)
    rare-biomes: []
    # Enable weighted search that favors hotspots for rare biomes
    use-weighted-search: true
    # Track and maintain hotspots for rare biomes
    enable-hotspot-tracking: true
protection:
  avoid-claims: false
  providers:
    - worldguard
    - griefprevention
particles:
  enabled: false
  type: PORTAL
  count: 40
  offset:
    x: 0.5
    y: 1.0
    z: 0.5
  extra: 0.0
  force: false
on-join:
  enabled: false
  only-first-join: false
  bypass-permission: ""
  delay-ticks: 40
# RTP cooldowns and usage limits
rtp-limits:
  # Allow opening the GUI even if the current world has an active cooldown
  # When true, GUI opens but worlds with active cooldowns are disabled in the GUI
  # When false, GUI won't open if any world has an active cooldown (current behavior)
  allow-gui-during-cooldown: true
  default:
    cooldown-seconds: 300   # Cooldown in seconds between /rtp uses
    daily-limit: 10         # Max uses per day
    weekly-limit: 50        # Max uses per week
    cost: 0.0               # Optional cost override for this group
  worlds:
    world:
      # Disable daily limit for this world (overrides daily-limit and weekly-limit)
      disable-daily-limit: false
      default:
        cooldown-seconds: 300
        daily-limit: 10
        weekly-limit: 50
        cost: 0.0
      group.vip:
        cooldown-seconds: 60
        daily-limit: 50
        weekly-limit: 200
        cost: 0.0
    world_nether:
      disable-daily-limit: false
      default:
        cooldown-seconds: 600
        daily-limit: 5
        weekly-limit: 20
        cost: 0.0
      group.staff:
        cooldown-seconds: 0
        daily-limit: -1   # -1 = unlimited
        weekly-limit: -1
        cost: 0.0
  bypass-permissions:
    - ezrtp.bypass.cooldown
    - ezrtp.bypass.limit
  storage: yaml # or mysql
  mysql:
    url: jdbc:mysql://localhost:3306/mc
    user: root
    password: ""
```
</details>

<details>
<summary>messages/{language}.yml</summary>

```yml
# EzRTP MiniMessage templates
# Placeholders: <world>, <x>, <z>, <position>, <server>, <cost>, <target-biome>, <cached-locations>
teleporting: "<gray>Searching for a safe location...</gray>"
teleport-success: "<green>Teleported to <white><x></white>, <white><z></white> in <white><world></white>.</green>"
teleport-failed: "<red>Unable to find a safe location. Please try again.</red>"
teleport-failed-biome: "<red>No valid biome was found. Please try again or try a different biome filter.</red>"
world-missing: "<red>The configured world '<white><world></white>' is not available.</red>"
join-searching: "<gray>Finding you a safe place to explore...</gray>"
insufficient-funds: "<red>You need <white><cost></white> to use random teleport.</red>"
queue-queued: "<gray>You joined the random teleport queue. Position: <white><position></white>.</gray>"
queue-full: "<red>The random teleport queue is currently full. Please try again soon.</red>"
# Cooldown and usage limit messages
cooldown: "<red>You must wait <white><hours></white> <white><minutes></white> <white><seconds></white> before using /rtp again.</red>"
limit-daily: "<red>You have reached your daily /rtp limit for this world.</red>"
limit-weekly: "<red>You have reached your weekly /rtp limit for this world.</red>"
# Countdown messages
countdown-start: "<yellow>Teleporting in <white><seconds></white> seconds...</yellow>"
countdown-tick: "<gray><seconds>...</gray>"
```
</details>

<details>
<summary>queue.yml</summary>

```yml
# EzRTP teleport queue configuration
enabled: false
max-size: 0
bypass-permission: "ezrtp.queue.bypass"
start-delay-ticks: 20
interval-ticks: 40
```
</details>

<details>
<summary>gui.yml</summary>

```yml
# EzRTP GUI configuration
enabled: false
title: "<gold>Select a destination</gold>"
rows: 1
no-permission-message: "<red>You do not have permission to teleport there.</red>"
# Only show cache info (biome counts) to players with ezrtp.admin permission
admin-only-cache-info: false
# Disable cache filtering entirely (show all biomes regardless of cache status)
disable-cache-filtering: true
# Supports PlaceholderAPI placeholders in item names and lore (e.g., %player_name%)
filler:
  enabled: true
  material: GRAY_STAINED_GLASS_PANE
  name: "<gray> </gray>"
worlds:
  overworld:
    # slot: 0
    permission: ""
    icon:
      material: GRASS_BLOCK
      name: "<green>Overworld</green>"
      lore:
        - "<gray>Teleport to a random location in the overworld.</gray>"
    settings:
      world: world
      radius:
        min: 256
        use-world-border: true
  resource:
    slot: 1
    permission: "ezrtp.gui.resource"
    icon:
      material: IRON_PICKAXE
      name: "<gold>Resource World</gold>"
      lore:
        - "<gray>Gather materials without ruining the main world.</gray>"
    settings:
      world: resource
      radius:
        min: 512
        max: 4096
        use-world-border: false
```
</details>

<details>
<summary>network.yml</summary>

```yml
# EzRTP proxy / network configuration
enabled: false
lobby: false
ping-interval-ticks: 200
ping-timeout-millis: 1500
servers:
  skyblock:
    bungee-server: "skyblock"
    host: "127.0.0.1"
    port: 25566
    slot: 4
    permission: ""
    display-name: "Skyblock"
    hide-when-offline: false
    allow-when-offline: false
    connect-message: "<gray>Connecting you to <white><server></white>...</gray>"
    offline-message: "<red><server></red> is currently unavailable."
    icon:
      material: ENDER_PEARL
      name: "<gold><server></gold>"
      lore:
        - "<gray>Status: <status></gray>"
        - "<gray>Ping: <white><ping></white>ms</gray>"
```
</details>

## Requirements
- Java 17 or newer
- Paper, Purpur, or SpigotMC 1.19+ server build
- Optional: Vault + an economy plugin if you want to charge for teleports
- Optional: A permissions plugin (LuckPerms, etc.) if you want granular control over `/rtp reload`, queue bypass, GUI option access, or cost bypasses

## Support & Links
- Need help? [Join our Discord](https://discord.gg/yWP95XfmBS) and open a ticket under the EzRTP category.
- Share seeds, biome rules, or config presets on the discussion tab so other admins can jumpstart their setup.
- Pair EzRTP with EzSpawners, EzAuction, and the rest of the Ez-series utilities for a cohesive network toolkit.
- [Get EzEconomy – Modern Vault Economy Plugin](https://www.spigotmc.org/resources/1-21-ezeconomy-modern-vault-economy-plugin-for-minecraft-servers.130975/)

---

**Ready to launch players into adventure?**

Install EzRTP and give explorers a safe, stylish way to discover your world-across every dimension!

[![Try the other Minecraft plugins in the EzPlugins series](https://i.ibb.co/PzfjNjh0/ezplugins-try-other-plugins.png)](https://modrinth.com/collection/Q98Ov6dA)