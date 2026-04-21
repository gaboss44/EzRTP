---
title: Operations & Troubleshooting
nav_order: 6
---

# Operations, Tuning, and Troubleshooting

## Runtime behavior notes

- Cooldown and usage tracking are applied after successful RTPs.
- GUI-enabled servers can route `/rtp` into destination selection before teleport.
- Cache fallback can recover failed searches when enabled and data exists.
- Admin and bypass permissions can skip cooldown/limit checks.

## Recommended profiles

### Low-memory SMP

- Keep conservative `max-attempts`.
- Enable `chunk-loading` throttles.
- Use queue mode if traffic spikes.
- Leave heavy rare-biome background scans disabled.

### Biome exploration server

- Enable biome pre-cache.
- Enable rare-biome optimization.
- Tune cache sizes upward gradually while monitoring memory and TPS.

### Network/lobby server

- Enable GUI + network entries.
- Use permission-gated destinations.
- Verify proxy registration names match `bungee-server` values.

## Troubleshooting

### No safe location found

- Increase `max-attempts`.
- Adjust radius/center.
- Review `unsafe-blocks`.
- Enable fallback to cache.

### Biome RTP fails frequently

- Enable biome pre-cache.
- Enable rare-biome optimization.
- Increase biome attempt/safeguard budgets.

### RTP causes lag

- Enable queue mode.
- Lower chunk loads per tick.
- Increase interval ticks for chunk loading.

### GUI shows no options

- Verify entry permissions.
- Check cache filtering settings.
- Validate world names in each GUI entry.

### Network entries show offline

- Validate host/port and ping timeout.
- Confirm proxy-side server naming.
- Confirm plugin messaging path and network mode settings.
