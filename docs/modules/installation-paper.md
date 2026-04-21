---
title: Paper
nav_order: 3
parent: Platform Modules
---

# Paper Module Installation

## Required jar(s)

- `EzRTP-<version>.jar`
- `ezrtp-paper-<version>.jar`

Paper requires both the base EzRTP plugin and the Paper platform module.

## Install flow

1. Stop the server.
2. Place both jars in `plugins/`.
3. Start once to generate `plugins/EzRTP/` defaults.
4. Stop, update config as needed, then start again.

## Module notes

- Maven modules: `ezrtp-bukkit` (base runtime) + `ezrtp-paper` (platform adapters)
- Keep both jars on the same version.
- Do not install `ezrtp-purpur-<version>.jar` on Paper.
