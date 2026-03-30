# EzRTP Messages & Localization

EzRTP messages are controlled by `messages/<language>.yml`.

Default language file:

- `messages/en.yml`

## Formatting

- Supports MiniMessage tags.
- Supports placeholders used by each message key (for example `<world>`, `<x>`, `<z>`, `<seconds>`, `<cost>`, `<position>`, `<server>`).

## Common message groups

- Teleport lifecycle (`teleporting`, `teleport-success`, `teleport-failed`, fallback variants)
- Queue (`queue-queued`, `queue-full`)
- Countdown (`countdown-start`, `countdown-tick`)
- Limits/cooldown (`cooldown`, `limit-daily`, `limit-weekly`)
- GUI (`gui-cache-filter-info`, `gui-no-destinations`, `gui-cooldown`)
- Network (`network-service-unavailable`, `network-server-offline`)
- Command feedback (`command-*`)
- Force RTP (`forcertp-*`)
- Heatmap/fake/stats helper messages

## Creating another language

1. Copy `messages/en.yml` to `messages/<your-language>.yml`.
2. Translate values while keeping keys and placeholders intact.
3. Set `language: <your-language>` in `config.yml`.
4. Run `/rtp reload`.
