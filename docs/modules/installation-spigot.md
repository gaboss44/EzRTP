---
title: Spigot
nav_order: 2
parent: Platform Modules
---

# Spigot Module Installation

## Required jar(s)

- `EzRTP-<version>.jar`

For Spigot servers, install the base EzRTP jar. Spigot runs through the Bukkit-compatible runtime path.

## Install flow

1. Stop the server.
2. Place `EzRTP-<version>.jar` in `plugins/`.
3. Start once to generate `plugins/EzRTP/` defaults.
4. Stop, adjust configuration files, then start again.

## Module notes

- Maven module in the multi-module build: `ezrtp-spigot`
- Runtime install jar for server owners remains `EzRTP-<version>.jar`
