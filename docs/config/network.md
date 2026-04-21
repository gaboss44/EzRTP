---
title: network.yml
nav_order: 6
parent: Config Reference
---

# network.yml

Adds cross-server entries to the RTP GUI. When a player clicks a network entry
they are transferred to another server on your BungeeCord or Velocity network
and their RTP runs there.

This file is meant to be placed on a **lobby server** that acts as the selector.
Leave it disabled on game servers unless they also show the selection GUI.

---

## Top-level settings

| Key | Default | Description |
|:----|:--------|:------------|
| `enabled` | `false` | Set to `true` to enable network entries in the GUI. |
| `lobby` | `false` | Set to `true` on the server that hosts the GUI (typically the lobby). |
| `ping-interval-ticks` | `200` | How often EzRTP pings each configured server to check if it’s online, in ticks (200 = 10 seconds). |
| `ping-timeout-millis` | `1500` | How long to wait for a ping response before marking the server as offline (milliseconds). |

---

## Adding a server entry

Each entry under `servers` becomes a clickable icon in the GUI — identical to a
normal world entry but it transfers the player instead of teleporting them.

```yml
servers:
  skyblock:
    bungee-server: "skyblock"         # must match the server name in your proxy config
    host: "127.0.0.1"                  # used for pinging (can be 127.0.0.1 on the same machine)
    port: 25566
    slot: 4
    permission: ""                     # leave blank = everyone can click
    display-name: "Skyblock"
    hide-when-offline: false           # hide the icon when the server is down
    allow-when-offline: false          # prevent clicking when the server is down
    connect-message: "<gray>Connecting you to <white><server></white>...</gray>"
    offline-message: "<red><server></red> is currently unavailable."
    icon:
      material: ENDER_PEARL
      name: "<gold><server></gold>"
      lore:
        - "<gray>Status: <status></gray>"
        - "<gray>Ping: <white><ping></white>ms</gray>"
```

### Server entry fields

| Field | Description |
|:------|:------------|
| `bungee-server` | Server name as defined in your proxy’s `config.yml` (`servers` section). |
| `host` / `port` | Address used to ping the server and check if it’s online. |
| `slot` | GUI slot (0–53). |
| `permission` | Permission required to use this entry. Leave blank for all players. |
| `hide-when-offline` | Hide the icon entirely when the server is unreachable. |
| `allow-when-offline` | Allow clicking even when the server is unreachable (the transfer will still fail at the proxy level). |
| `connect-message` | Chat message sent before the transfer. `<server>` is replaced with `display-name`. |
| `offline-message` | Message shown when the player tries to connect to an offline server. |
| `icon.lore` placeholders | `<status>` → Online/Offline, `<ping>` → latency in ms, `<server>` → display name. |

---

{: .note }
Network transfers use the plugin messaging channel (`BungeeCord` / `Velocity`).
Make sure your proxy is configured to allow transfers from game servers and that
`bungeecord: true` (or Velocity forwarding) is set in the game server’s
`spigot.yml` / `paper-global.yml`.
