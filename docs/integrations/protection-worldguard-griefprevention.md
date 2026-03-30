# WorldGuard / GriefPrevention Protection Integration

Use this integration when you want EzRTP to avoid protected regions/claims.

## What this integration does

When enabled, EzRTP validates candidate RTP locations against configured protection providers and rejects protected points.

Supported provider names in config:
- `worldguard`
- `griefprevention`

If a provider plugin is not installed, EzRTP continues with available providers (safe fallback behavior).

## Where to configure it

File: `plugins/EzRTP/rtp.yml`

```yml
protection:
  avoid-claims: false
  providers:
    - worldguard
    - griefprevention
```

### Key settings

- `protection.avoid-claims`
  - `true`: reject locations inside supported protected areas.
  - `false`: ignore protection checks.
- `protection.providers`
  - Priority list of providers EzRTP can query.
  - Keep only providers you actually use to simplify troubleshooting.

Why here: claim/region safety is part of RTP destination validation, so it belongs in `rtp.yml`.

## Optional WorldGuard region command mode

You can also enable region-centric command routing:

File: `plugins/EzRTP/rtp.yml` (and compatibility mirror in `config.yml`)

```yml
worldguard:
  region-command:
    enabled: false
    autocomplete: false
```

- Enable this to allow `/rtp <regionId>` behavior.
- Useful for servers with curated gameplay regions.

## Recommended settings by server type

- **Survival / Towny-like protection-heavy servers:** set `avoid-claims: true`.
- **Minigame / event servers with custom handling:** keep `avoid-claims: false` unless conflicts occur.
- **Mixed networks:** enable only on servers where claim boundaries matter.
