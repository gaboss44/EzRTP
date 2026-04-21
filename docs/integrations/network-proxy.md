---
title: Network & Proxy
nav_order: 5
parent: Integrations
---

# Network / Proxy Integration (BungeeCord/Velocity-style server selector)

Use this integration when your `/rtp` GUI should also present cross-server destinations.

## What this integration does

EzRTP can show configured network servers in GUI entries, including status/ping text and connect actions.

This is typically used on lobby/hub servers in multi-server networks.

## Where to configure it

File: `plugins/EzRTP/network.yml`

```yml
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
```

### Key settings

- `enabled`: global network integration switch.
- `lobby`: set `true` on lobby/hub servers where GUI server transfer should be active.
- `servers.<id>.bungee-server`: proxy-registered target name used for transfer.
- `servers.<id>.host` / `port`: status ping target.
- `slot`: GUI slot placement.
- `hide-when-offline` / `allow-when-offline`: controls offline display/click behavior.

Why here: cross-server destinations are separate from world RTP logic and belong in dedicated network config.

## Deployment model for networks

- **Lobby server:** usually `enabled: true`, `lobby: true`.
- **Gameplay servers:** usually `enabled: true`, `lobby: false` so normal RTP remains primary.
- Keep server IDs and proxy target names consistent with your proxy configuration.

## Common pitfalls

- **Server appears offline permanently:** wrong `host/port` ping target.
- **Click does nothing / wrong destination:** `bungee-server` name mismatch with proxy.
- **Duplicate/confusing GUI entries:** overlapping world GUI slots and network slots.
