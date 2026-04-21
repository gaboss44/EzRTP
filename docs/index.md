---
title: Home
layout: home
nav_order: 1
---

# EzRTP

EzRTP is a production-focused random teleport plugin for Minecraft servers running
Bukkit, Spigot, Paper, or Purpur. It provides safety-first teleportation,
cross-platform compatibility, and configuration-driven control for server owners.

## Highlights

- **Safety-first teleportation** — configurable unsafe-block lists, surface recovery, and claim avoidance.
- **Platform modules** — Bukkit base + optional Paper or Purpur adapters for native async APIs.
- **Economy integration** — Vault-compatible cost system with per-world and per-group pricing.
- **Queue throttling** — request queues prevent load spikes on busy servers.
- **Biome filtering** — include/exclude filters, pre-cache, and rare-biome optimization.
- **GUI destination selector** — inventory GUI with per-world icons, permissions, and network entries.
- **Network/proxy support** — cross-server destinations in GUI for multi-server networks.

---

## For Server Owners & Admins

| | |
|:---|:---|
| [Getting Started](overview-installation) | Install EzRTP, choose a platform module, verify your setup |
| [Commands](commands) | Player and admin command reference |
| [Permissions](permissions) | Permission nodes and recommended role assignments |
| [Messages & Localization](messages-localization) | Customize messages, add languages |
| [Operations & Troubleshooting](operations-troubleshooting) | Tuning profiles, common issues |

## Configuration Reference

| | |
|:---|:---|
| [config.yml](config/config) | Global plugin settings and language |
| [rtp.yml](config/rtp) | RTP bounds, safety, biomes, chunky, and countdown |
| [limits.yml](config/limits) | Cooldowns, usage limits, cost overrides, and storage backend |
| [gui.yml](config/gui) | Inventory GUI layout and destination entries |
| [queue.yml](config/queue) | Teleport queue throttling |
| [network.yml](config/network) | Cross-server proxy destination settings |
| [force-rtp.yml](config/force-rtp) | Admin `/forcertp` command defaults |
| [Search Patterns](config/search-patterns) | All `search-pattern` values explained |

## Platform Modules

| | |
|:---|:---|
| [Bukkit / CraftBukkit](modules/installation-bukkit) | Base plugin only |
| [Spigot](modules/installation-spigot) | Base plugin only |
| [Paper](modules/installation-paper) | Base + `ezrtp-paper` module |
| [Purpur](modules/installation-purpur) | Base + `ezrtp-purpur` module |

## Integrations

| | |
|:---|:---|
| [Vault / Economy](integrations/vault-economy) | Charge players for RTP |
| [PlaceholderAPI](integrations/placeholderapi) | Dynamic placeholders in GUI |
| [Chunky](integrations/chunky) | World pre-generation |
| [WorldGuard & GriefPrevention](integrations/protection-worldguard-griefprevention) | Claim & region avoidance |
| [Network & Proxy](integrations/network-proxy) | Cross-server GUI entries |

## For Developers

| | |
|:---|:---|
| [Developer API](api/plugin-api) | Trigger RTP programmatically from other plugins |
| [Developer Guide](development-collaboration) | Architecture, build workflow, contributor conventions |
