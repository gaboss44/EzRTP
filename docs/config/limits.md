# limits.yml

Cooldown, usage limits, and storage backend settings.

- `rtp-limits.storage`: `yaml` or `mysql`.
- `rtp-limits.mysql.*`: JDBC connection values for MySQL storage.
- `rtp-limits.default.*`: baseline cooldown/limits/cost.
- `rtp-limits.worlds.<world>.<group>.*`: per-world and permission-group overrides.
- `rtp-limits.bypass-permissions`: permissions that bypass cooldown/limits.
- `rtp-limits.allow-gui-during-cooldown`: whether GUI open is allowed while on cooldown.
