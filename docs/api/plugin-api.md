---
title: Developer API
nav_order: 10
---

# Developer API

EzRTP exposes a small public API so other plugins can trigger random teleports
programmatically — including custom destinations, radius overrides, and
success/failure callbacks.

---

## Adding the dependency

The API is published as a separate lightweight artifact (`ezrtp-api`) that
contains only the classes you need. You do **not** need to depend on the full
plugin jar.

### Maven

```xml
<dependency>
  <groupId>com.skyblockexp</groupId>
  <artifactId>ezrtp-api</artifactId>
  <version>3.0.1</version>
  <scope>provided</scope>
</dependency>
```

### Gradle

```groovy
compileOnly 'com.skyblockexp:ezrtp-api:3.0.1'
```

Declare EzRTP as a soft or hard dependency in your `plugin.yml` so Bukkit
loads it before your plugin:

```yml
# hard dependency — your plugin will not load if EzRTP is absent
depend: [EzRTP]

# soft dependency — your plugin loads with or without EzRTP
softdepend: [EzRTP]
```

---

## Quick start

```java
import com.skyblockexp.ezrtp.api.EzRtpAPI;

// Check that EzRTP is running before calling anything
if (EzRtpAPI.isAvailable()) {
    EzRtpAPI.rtpPlayer(player);
}
```

That's it for the simplest case. The teleport uses all settings from the
server's `rtp.yml`, charges any configured cost, and respects cooldowns.

---

## API reference

All methods are static on `EzRtpAPI`.

### `isAvailable()`

```java
boolean available = EzRtpAPI.isAvailable();
```

Returns `true` if EzRTP is loaded and its service is registered. Call this
before any other method if EzRTP is a soft dependency.

---

### `rtpPlayer(Player player)`

Teleport a player using the server's default RTP settings.

```java
EzRtpAPI.rtpPlayer(player);
```

- Uses `TeleportReason.COMMAND` — cooldowns, costs, and limits apply normally.
- Fire-and-forget. No way to know if it succeeded.

---

### `rtpPlayer(Player player, Object settings)`

Teleport with custom settings (e.g. a different world or radius).

```java
import com.skyblockexp.ezrtp.config.RandomTeleportSettings;
import org.bukkit.configuration.file.YamlConfiguration;

YamlConfiguration cfg = new YamlConfiguration();
cfg.set("world", "world_the_end");
cfg.set("radius.min", 1000);
cfg.set("radius.max", 5000);

RandomTeleportSettings settings = RandomTeleportSettings.fromConfiguration(cfg, getLogger());
EzRtpAPI.rtpPlayer(player, settings);
```

`RandomTeleportSettings.fromConfiguration()` reads a `ConfigurationSection`
exactly like EzRTP reads `rtp.yml`. Any key you omit uses the built-in default.

---

### `rtpPlayer(Player player, Object settings, Consumer<Boolean> callback)`

Teleport with custom settings **and** a callback that fires when the teleport
completes or fails.

```java
EzRtpAPI.rtpPlayer(player, settings, success -> {
    if (success) {
        player.sendMessage("You were teleported!");
    } else {
        player.sendMessage("No safe location found.");
    }
});
```

The `Boolean` passed to the callback is:

- `true` — teleport completed successfully.
- `false` — teleport failed (no valid location found, player moved during countdown, etc.).

The callback is invoked on the main server thread.

---

### `getTeleportService()`

Returns the raw `TeleportService` instance for advanced use cases.

```java
import com.skyblockexp.ezrtp.api.TeleportService;
import com.skyblockexp.ezrtp.teleport.TeleportReason;

TeleportService service = EzRtpAPI.getTeleportService();
if (service != null) {
    service.teleportPlayer(player, TeleportReason.JOIN);
}
```

Use this when you need to pass a specific `TeleportReason` or call a variant
not exposed by the static helpers.

---

## `TeleportReason` enum

`TeleportReason` tells EzRTP why the teleport is happening. This affects which
cost and cooldown rules are applied.

| Value | When to use |
|:------|:------------|
| `COMMAND` | Player triggered the teleport via a command or button. Cooldowns and costs apply. |
| `JOIN` | Player joined the server and was auto-teleported. Uses the `on-join` cost/cooldown rules. |

---

## `TeleportService` interface

The full interface for advanced callers:

```java
public interface TeleportService {
    // Simple teleport, no callback
    void teleportPlayer(Player player, TeleportReason reason);

    // Custom settings, no callback
    void teleportPlayer(Player player, Object settings, TeleportReason reason);

    // Simple teleport with callback
    void teleportPlayer(Player player, TeleportReason reason, Consumer<Boolean> callback);

    // Custom settings with callback
    void teleportPlayer(Player player, Object settings, TeleportReason reason, Consumer<Boolean> callback);
}
```

---

## `RandomTeleportSettings` — full YAML reference

`RandomTeleportSettings.fromConfiguration(ConfigurationSection, Logger)` reads
any YAML section. Below are the keys you can set:

| Key | Type | Example | Description |
|:----|:-----|:--------|:------------|
| `world` | String | `world_the_end` | World to teleport into. `auto` = player's current world. |
| `center.x` / `center.z` | int | `0` | Search centre coordinates. |
| `radius.min` | int | `500` | Minimum distance from centre. |
| `radius.max` | int | `3000` | Maximum distance from centre. |
| `radius.use-world-border` | bool | `true` | Use world border edge as the maximum radius. |
| `min-y` / `max-y` | int | `54` / `320` | Vertical bounds for destinations. |
| `max-attempts` | int | `20` | Candidate attempts before failing. |
| `cost` | double | `5.0` | Economy cost per teleport (Vault). |
| `countdown-seconds` | int | `3` | Countdown before teleport. `0` = instant. |
| `search-pattern` | String | `circle` | Coordinate search shape. See [Search Patterns](../config/search-patterns). |
| `biomes.include` | list | `[FOREST, PLAINS]` | Require one of these biomes. |
| `biomes.exclude` | list | `[OCEAN]` | Reject these biomes. |
| `protection.avoid-claims` | bool | `true` | Skip locations inside protected claims. |

```java
YamlConfiguration cfg = new YamlConfiguration();
cfg.set("world", "world");
cfg.set("radius.min", 2000);
cfg.set("radius.max", 8000);
cfg.set("search-pattern", "circle");
cfg.set("biomes.include", List.of("FOREST", "BIRCH_FOREST"));
cfg.set("cost", 10.0);

RandomTeleportSettings settings = RandomTeleportSettings.fromConfiguration(cfg, getLogger());
EzRtpAPI.rtpPlayer(player, settings, success -> {
    if (!success) player.sendMessage("Could not find a forest location. Try again.");
});
```

---

## Null safety

- If EzRTP is not installed or not yet enabled, `getTeleportService()` returns
  `null` and all `rtpPlayer` helpers silently do nothing (or invoke the callback
  with `false`).
- Always guard with `EzRtpAPI.isAvailable()` when EzRTP is a soft dependency.
- The API is safe to call from any thread; EzRTP handles its own async/sync
  boundaries internally.
