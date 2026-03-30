# queue.yml

Teleport request queue tuning.

- `enabled`: enables queue mode.
- `max-size`: queue size cap (`0` = unlimited).
- `bypass-permission`: queue bypass permission.
- `start-delay-ticks`: delay before first queued teleport.
- `interval-ticks`: delay between queued teleports.
