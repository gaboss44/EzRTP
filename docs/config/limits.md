---
title: limits.yml
nav_order: 3
parent: Config Reference
---

# limits.yml

Controls how often players can use `/rtp`, how many times per day/week, and what
it costs. You can set different rules per world and per permission group (e.g. VIP
gets a shorter cooldown than regular players).

## Default limits

The `rtp-limits.default` block applies to all players and worlds unless a more
specific override exists.

| Key | Default | Description |
|:----|:--------|:------------|
| `cooldown-seconds` | `300` | Seconds a player must wait between teleports. `0` = no cooldown. |
| `daily-limit` | `10` | Maximum number of RTP uses per calendar day. `-1` = unlimited. |
| `weekly-limit` | `50` | Maximum number of RTP uses per calendar week. `-1` = unlimited. |
| `cost` | `0.0` | Economy cost per teleport (requires Vault). `0.0` = free. |

## Per-world and per-group overrides

Under `rtp-limits.worlds` you can override any of the four keys above for a
specific world, and optionally narrow it further to a permission group.

Group keys start with `group.` followed by the group name as reported by your
permissions plugin (e.g. `group.vip`, `group.staff`).

```yml
rtp-limits:
  default:
    cooldown-seconds: 300
    daily-limit: 10
    weekly-limit: 50
    cost: 0.0
  worlds:
    world:               # overworld rules
      default:           # applies to everyone
        cooldown-seconds: 300
        daily-limit: 10
        weekly-limit: 50
        cost: 0.0
      group.vip:         # VIP players get less cooldown and more uses
        cooldown-seconds: 60
        daily-limit: 50
        weekly-limit: 200
        cost: 0.0
    world_nether:        # stricter limits in the Nether
      default:
        cooldown-seconds: 600
        daily-limit: 5
        weekly-limit: 20
        cost: 0.0
      group.staff:       # staff have no restrictions
        cooldown-seconds: 0
        daily-limit: -1
        weekly-limit: -1
        cost: 0.0
```

## Bypass permissions

Players with any of the listed permissions skip cooldowns and usage limits
entirely, regardless of world or group configuration.

```yml
rtp-limits:
  bypass-permissions:
    - ezrtp.bypass.cooldown
    - ezrtp.bypass.limit
```

## Other settings

| Key | Default | Description |
|:----|:--------|:------------|
| `allow-gui-during-cooldown` | `true` | When `true`, players on cooldown can still open the destination GUI — they just cannot confirm a teleport until the cooldown expires. Set to `false` to hide the GUI entirely. |

## Storage backend

Cooldown and usage data can be stored in flat files or MySQL.

```yml
rtp-limits:
  storage: yaml      # options: yaml | mysql
  mysql:
    host: localhost
    port: 3306
    database: ezrtp
    username: ""
    password: ""
```

Use `mysql` on networks where multiple servers share player data (e.g. a survival
server and a resource world that both enforce the same weekly limit).
