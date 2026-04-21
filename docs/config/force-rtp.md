---
title: force-rtp.yml
nav_order: 7
parent: Config Reference
---

# force-rtp.yml

Controls the `/forcertp <player>` command that lets admins and moderators teleport
another player to a random location. This file lets you set the default world and
choose which normal restrictions are bypassed when the command is used.

---

## Settings

| Key | Default | Description |
|:----|:--------|:------------|
| `default-world` | `world` | World used when the command is run without specifying a world. Set to `auto` to send the target player to a random location in their current world. |
| `bypass.cooldown` | `true` | When `true`, `/forcertp` ignores the target player’s cooldown. Recommended — admins should never be blocked by a cooldown on a moderation tool. |
| `bypass.permission` | `true` | When `true`, `/forcertp` ignores any per-destination permission checks on the target player. |
| `bypass.safety` | `false` | When `true`, the destination does **not** have to pass safety checks — players can be placed on lava, inside water, etc. Leave this `false` unless you have a specific reason. |

---

## Example

```yml
default-world: world

bypass:
  cooldown: true     # don't wait for the player's cooldown
  permission: true   # ignore per-destination permissions on the target
  safety: false      # still enforce safe landing
```

---

{: .note }
The `/forcertp` command itself still requires the `ezrtp.admin.forcertp` permission.
The bypass settings only control what restrictions are waived for the **target** player.
