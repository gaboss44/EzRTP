---
title: gui.yml
nav_order: 4
parent: Config Reference
---

# gui.yml

Configures the optional inventory GUI that lets players pick an RTP destination
by clicking an item. Each "world" entry becomes a clickable icon in the chest
inventory.

---

## Top-level settings

| Key | Default | Description |
|:----|:--------|:------------|
| `enabled` | `true` | Set to `false` to disable the GUI entirely. Players use `/rtp` only as a plain command. |
| `title` | `<dark_aqua><bold>Random Teleport</bold></dark_aqua>` | Title shown at the top of the inventory. Supports MiniMessage formatting. |
| `rows` | `5` | Number of rows in the chest GUI (1–6). Each row has 9 slots, so 5 rows = 45 slots total (0–44). |
| `no-permission-message` | `<red>You do not have permission...</red>` | Message shown when a player clicks a destination they don’t have permission for. |
| `no-destinations` | *(see default)* | Message shown when no destinations are available to the player. |
| `filler.enabled` | `true` | Fill empty slots with a decorative item. |
| `filler.material` | `GLASS_PANE` | Material used for filler items. |
| `filler.name` | `<dark_gray> </dark_gray>` | Display name for filler items (a space makes it invisible in most themes). |

---

## Slot numbering

Slots are numbered left-to-right, top-to-bottom, starting at `0`:

```
Row 1:  0  1  2  3  4  5  6  7  8
Row 2:  9 10 11 12 13 14 15 16 17
Row 3: 18 19 20 21 22 23 24 25 26
Row 4: 27 28 29 30 31 32 33 34 35
Row 5: 36 37 38 39 40 41 42 43 44
```

For a 5-row GUI the centre slot of row 3 is **slot 22** (the default Overworld position).

---

## Adding a destination entry

Each entry under `worlds` becomes one clickable icon. The key name (e.g.
`overworld`) is just an internal identifier — it is never shown to players.

```yml
worlds:
  overworld:
    slot: 22                     # which slot to put this icon in
    permission: ""               # leave blank = everyone can click it
    icon:
      material: GRASS_BLOCK
      name: "<green><bold>Overworld</bold></green>"
      lore:
        - "<gray>Explore the surface world.</gray>"
        - "<dark_gray>Click to teleport.</dark_gray>"
    settings:
      world: world               # must match the world folder name
```

### Entry fields

| Field | Description |
|:------|:------------|
| `slot` | Inventory slot (0–53). Omit to place entries in order. |
| `permission` | Permission node required to click. Leave blank for no restriction. |
| `icon.material` | Any valid Bukkit/Minecraft item ID (e.g. `GRASS_BLOCK`, `NETHERRACK`). |
| `icon.name` | Display name. Supports MiniMessage. Supports PlaceholderAPI if installed. |
| `icon.lore` | List of lore lines. Supports MiniMessage and PlaceholderAPI. |
| `settings.world` | World to teleport into. Use `auto` to teleport within the player’s current world. |
| `settings.*` | Any `rtp.yml` setting can be overridden here (radius, biomes, cost, etc.). |

---

## Biome-restricted entry example

To add a button that only sends players to forest biomes:

```yml
worlds:
  forest:
    slot: 11
    permission: "ezrtp.gui.forest"
    icon:
      material: OAK_SAPLING
      name: "<dark_green><bold>Forest</bold></dark_green>"
      lore:
        - "<gray>Land in a forest biome.</gray>"
    settings:
      world: world
      biomes:
        include:
          - FOREST
          - BIRCH_FOREST
          - DARK_FOREST
        pre-cache:
          enabled: true
          max-per-biome: 30
```

---

## “Current world” entry

A special entry that teleports the player within whichever world they are already
in. Useful on multi-world servers.

```yml
worlds:
  current-world:
    slot: 13
    permission: ""
    icon:
      material: COMPASS
      name: "<aqua><bold>Current World</bold></aqua>"
      lore:
        - "<gray>Teleport within your current world.</gray>"
    settings:
      world: auto
```

---

## Cache filtering

When biome pre-caching is enabled, EzRTP can hide destinations that have no
pre-cached locations ready, so players are never shown an option that would make
them wait.

| Key | Default | Description |
|:----|:--------|:------------|
| `disable-cache-filtering` | `false` | Set to `true` to always show all destinations regardless of cache state. |
| `admin-only-cache-info` | `false` | When `true`, the “only showing cached options” notice is only shown to players with admin permissions. |
| `rare_biomes.enabled` | `true` | Apply stricter cache requirements to rare biomes listed in `rtp.yml`. |
| `rare_biomes.require-cache.minimum-cached` | `1` | Minimum pre-cached locations needed before a rare-biome destination is shown. |
