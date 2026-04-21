---
title: Permissions
nav_order: 4
---

# Permissions

## Declared permissions

| Permission | Default | Description |
|:-----------|:--------|:------------|
| `ezrtp.use` | `true` | Use `/rtp` |
| `ezrtp.reload` | `op` | Reload configuration with `/rtp reload` |
| `ezrtp.stats` | `op` | View RTP statistics with `/rtp stats` |
| `ezrtp.heatmap` | `op` | View/save heatmap with `/rtp heatmap` |
| `ezrtp.heatmap.fake` | `op` | Add fake heatmap points with `/rtp fake` |
| `ezrtp.queue.bypass` | `op` | Skip the teleport queue |
| `ezrtp.forcertp` | `op` | Force-teleport players with `/forcertp` |
| `ezrtp.setcenter` | `op` | Set or save a named RTP center |

## Additional permission usage

- `queue.yml` controls the queue bypass permission path.
- `gui.yml` and `network.yml` entries can require per-option permissions.
