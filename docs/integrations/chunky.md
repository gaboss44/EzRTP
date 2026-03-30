# Chunky Integration (World Pre-Generation)

Use this integration when you want faster/more reliable RTP in large unexplored worlds by pre-generating terrain.

## What EzRTP uses Chunky for

EzRTP can orchestrate Chunky-assisted pre-generation for RTP search areas.
This helps reduce chunk generation spikes during teleport attempts.

If Chunky is not installed, EzRTP falls back to normal RTP chunk loading behavior.

## Where to configure it

File: `plugins/EzRTP/rtp.yml`

```yml
chunky-integration:
  enabled: false
  auto-pregenerate: false
  shape: circle
  pattern: concentric
  memory-safety:
    enabled: true
    min-free-memory-mb: 256
    max-coordinator-entries: 5000
    low-memory-retention-minutes: 10
```

### Key settings and why they matter

- `enabled`: master switch for Chunky orchestration.
- `auto-pregenerate`: lets EzRTP trigger pre-generation automatically for workflows that support it.
- `shape` / `pattern`: controls generation geometry/order.
- `memory-safety.*`: guards server memory pressure during pregeneration coordination.

Why here: pregeneration directly influences RTP location availability/performance.

## Operational workflow

- Configure RTP world/radius first.
- Enable Chunky integration.
- Use `/rtp pregenerate [world] [radius]` for controlled pre-generation runs.
- Monitor TPS/memory and tune `memory-safety` thresholds.

## Recommended baseline

- Start with `enabled: true`, `auto-pregenerate: false`.
- Trigger pregeneration manually during low player activity.
- Enable `auto-pregenerate` only after validating memory behavior on your hardware.
