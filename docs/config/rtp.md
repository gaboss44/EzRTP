# rtp.yml

Default random teleport behavior and safety controls.

Main groups:

- `debug-rejection-logging`: logs candidate rejection reasons.
- `rtp.*`: global RTP flags.
- `min-y`, `max-y`, `world`, `center`, `radius`: destination bounds.
- `centers.named.*`: named RTP center presets for `/rtp <name>`.
- `search-pattern`, `max-attempts`: search strategy/performance.
- `cost`: base teleport cost.
- `countdown*`: pre-teleport delay, bossbar, and particles.
- `unsafe-blocks`, `safety.*`: landing safety rules.
- `chunk-loading.*`: chunk-load queue throttling.
- `chunky-integration.*`: optional Chunky pregeneration integration.
- `biomes.*`: include/exclude filters, pre-cache, rare biome optimization, search failover limits.
- `protection.*`: claim/region avoidance providers.
- `worldguard.region-command.*`: RTP-around-region defaults.
- `particles.*`: teleport-success particle effect.
- `on-join.*`: automatic join teleport settings.

## Safety recovery surface scan depth

`safety.recovery` includes environment-aware vertical scan controls:

- `max-surface-scan-depth` (default `20`): overwrite/default-world scan depth used for normal worlds.
- `max-surface-scan-depth-nether` (default `128`): Nether scan depth used when candidate resolution starts near the Nether roof.

This keeps overworld scans conservative for performance while allowing deeper Nether recovery to find valid footing below roof-level terrain.

## Named centers

`rtp.yml` supports reusable named centers under:

```yml
centers:
  named:
    spawn:
      world: world
      center:
        x: 0
        z: 0
```

- Use `/rtp addcenter <name>` in-game to write/update entries.
- Use `/rtp <name>` to RTP with that center override.
- If `worldguard.region-command.enabled: true`, `/rtp <regionId>` remains supported.
