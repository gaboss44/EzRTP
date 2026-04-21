---
title: Commands
nav_order: 3
---

# Commands

## Player command

- `/rtp`
  - Opens GUI when GUI is enabled.
  - Falls back to direct teleport when GUI is disabled or unavailable.
- `/rtp <centerName>`
  - Teleports using the named center configured under `centers.named` in `rtp.yml`.
  - Named center is applied as a center override only; the world's normal RTP settings still apply.

## RTP subcommands

- `/rtp reload`
- `/rtp stats [page]`
- `/rtp stats biomes [page]`
- `/rtp stats rare-biomes [page]`
- `/rtp heatmap [biome]`
- `/rtp heatmap save`
- `/rtp fake <amount> [world]`
- `/rtp fake clear [world]`
- `/rtp setcenter <x> <z>`
- `/rtp setcenter <world> <x> <z>`
- `/rtp addcenter <name>`
- `/rtp pregenerate [world] [radius]`

## Force command

- `/forcertp <player> [world]`
  - If world is omitted, EzRTP uses `force-rtp.yml` `default-world`.

## WorldGuard region mode (optional)

When enabled:

- `/rtp <regionId>`

This centers RTP around the specified WorldGuard region and can apply per-region overrides.

## Quick admin cheatsheet

- Reload config: `/rtp reload`
- Force player RTP: `/forcertp Steve world`
- Stats: `/rtp stats`
- Biome stats: `/rtp stats biomes`
- Heatmap map item: `/rtp heatmap`
- Save heatmap image: `/rtp heatmap save`
- Add fake points: `/rtp fake 100 world`
- Clear fake points: `/rtp fake clear world`
- Set RTP center (current world): `/rtp setcenter 0 0`
- Save a named center from your current position: `/rtp addcenter spawn`
