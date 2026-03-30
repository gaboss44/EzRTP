# Vault / Economy Integration

Use this integration when you want RTP to cost money (for economy sinks, progression pacing, or VIP balancing).

## What EzRTP uses from economy plugins

EzRTP hooks into Vault-compatible economy providers (for example EzEconomy, EssentialsX Economy, CMI economy bridges, etc.).

If Vault or an economy provider is missing, RTP still works; cost checks are skipped and teleports behave as free.

## Where to configure economy behavior

### 1) Base RTP price (`rtp.yml`)

File: `plugins/EzRTP/rtp.yml`

```yml
cost: 0.0
```

- Set this to a positive value to charge all RTP teleports by default.
- Keep `0.0` for fully free teleports.

Why here: this is the global fallback price for the RTP workflow.

### 2) Per-world / per-group prices (`limits.yml`)

File: `plugins/EzRTP/limits.yml`

Use `rtp-limits` entries to override cost by world and permission group.

Why here: limits and pricing are coupled in the same policy model as cooldown/daily limits, so you can tune economy costs alongside RTP throttling.

## Suggested rollout for production servers

1. Start with `cost: 0.0` while validating teleport safety and throughput.
2. Add a small base price in `rtp.yml`.
3. Move advanced pricing into `limits.yml` by world/group (for example cheaper overworld, higher cost end-game worlds).
4. Announce the price model in your server changelog/messages.

## Common pitfalls

- **Vault installed but no economy provider installed:** charges will not apply.
- **Unexpected free RTP:** verify `cost` and any per-group overrides that may set cost to `0`.
- **Players charged too much:** check both global `rtp.yml` cost and `limits.yml` overrides.
