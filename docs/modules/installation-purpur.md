---
title: Purpur
nav_order: 4
parent: Platform Modules
---

# Purpur Module Installation

## Required jar(s)

- `EzRTP-<version>.jar`
- `ezrtp-purpur-<version>.jar`

Purpur requires the base EzRTP plugin and the Purpur platform module.

## Install flow

1. Stop the server.
2. Place both jars in `plugins/`.
3. Start once to generate `plugins/EzRTP/` defaults.
4. Stop, edit configuration files, then start again.

## Module notes

- Maven modules: `ezrtp-bukkit` (base runtime) + `ezrtp-purpur` (Purpur adapters)
- Keep both jars on the same version.
- Do not install `ezrtp-paper-<version>.jar` on Purpur.
