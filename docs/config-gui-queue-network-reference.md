# EzRTP GUI, Queue, and Network Config Reference

This file documents `gui.yml`, `queue.yml`, and `network.yml`.

## `queue.yml`

- `enabled`
- `max-size` (0 = unlimited)
- `bypass-permission`
- `start-delay-ticks`
- `interval-ticks`

Use queueing to spread RTP load across ticks and reduce lag on busy servers.

## `gui.yml`

### Core GUI

- `enabled`
- `title`
- `rows`
- `no-permission-message`
- `cache-filter-info`
- `no-destinations`
- `disable-cache-filtering`
- `admin-only-cache-info`

### Rare biome GUI rules

- `rare_biomes.enabled`
- `rare_biomes.list`
- `rare_biomes.require-cache.minimum-cached`

### Filler slot item

- `filler.enabled`
- `filler.material`
- `filler.name`

### World entries (`worlds.<id>`)

Per entry:

- `slot`
- `permission`
- `icon.material`
- `icon.name`
- `icon.lore[]`
- `settings.world`
- optional biome and cache overrides inside `settings`

PlaceholderAPI placeholders in GUI icon text are supported when PlaceholderAPI is installed.

## `network.yml`

### Top-level

- `enabled`
- `lobby`
- `ping-interval-ticks`
- `ping-timeout-millis`

### Server entries (`servers.<id>`)

- `bungee-server`
- `host`
- `port`
- `slot`
- `permission`
- `display-name`
- `hide-when-offline`
- `allow-when-offline`
- `connect-message`
- `offline-message`
- `icon.material`
- `icon.name`
- `icon.lore[]`
