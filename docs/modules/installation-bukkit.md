# Bukkit / CraftBukkit Module Installation

## Required jar(s)

- `EzRTP-<version>.jar`

The Bukkit/CraftBukkit path uses the base EzRTP runtime and does not require an additional platform module jar.

## Install flow

1. Stop the server.
2. Place `EzRTP-<version>.jar` in `plugins/`.
3. Start once to generate `plugins/EzRTP/` defaults.
4. Stop, edit configuration files, then start again.

## Module notes

- Maven module: `ezrtp-bukkit`
- Typical output jar name: `EzRTP-<version>.jar`
