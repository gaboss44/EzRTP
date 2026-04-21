---
title: config.yml
nav_order: 1
parent: Config Reference
---

# config.yml

Global plugin settings — language, message prefix, and a few defaults used by
utility commands. All teleport behaviour, safety rules, and cooldowns live in
their own dedicated files.

## Settings

| Key | Default | Description |
|:----|:--------|:------------|
| `message-prefix` | `"&7[&bEzRTP&7] &r"` | Text prepended to every chat message the plugin sends. Supports `&` color codes and MiniMessage. |
| `language` | `en` | Selects which file under `messages/` is used for player-facing text. Example: `en` loads `messages/en.yml`. |
| `messages.force-legacy-colors` | `false` | Set to `true` on servers running very old clients that do not understand MiniMessage formatting. Converts all output to legacy `§` color codes. |
| `worldguard.region-command.enabled` | `false` | Allows `/rtp <regionId>` to teleport players randomly **inside** a named WorldGuard region. Requires WorldGuard. |
| `worldguard.region-command.autocomplete` | `false` | When enabled, tab-completing `/rtp` will suggest WorldGuard region names. |
| `world` | `world` | Fallback world name used by utility commands (e.g. `/rtp pregenerate`). Does not affect normal RTP — that is set in `rtp.yml`. |
| `radius.min` | `500` | Fallback minimum radius used by utility commands. Does not affect normal RTP. |
| `enable-bstats` | `true` | Sends anonymous usage statistics to [bStats](https://bstats.org/plugin/bukkit/EzRTP/27735). No personal data is collected. Set to `false` to opt out. |

## Example

```yml
message-prefix: "&7[&bEzRTP&7] &r"
language: en

messages:
  force-legacy-colors: false

worldguard:
  region-command:
    enabled: false
    autocomplete: false

enable-bstats: true
```

{: .note }
Most gameplay settings (destination bounds, safety, cooldowns, biomes) are in
`rtp.yml` and `limits.yml`, **not** here.
