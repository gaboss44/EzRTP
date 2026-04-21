---
title: queue.yml
nav_order: 5
parent: Config Reference
---

# queue.yml

When many players use `/rtp` at the same time, the queue prevents a sudden spike
of teleport requests from overloading the server. Teleports are processed one at
a time, with a configurable pause between each one.

The queue is **disabled by default**. Enable it only if you regularly see lag when
multiple players teleport simultaneously.

## Settings

| Key | Default | Description |
|:----|:--------|:------------|
| `enabled` | `false` | Set to `true` to turn on the queue. |
| `max-size` | `0` | Maximum number of players allowed to wait in line. `0` = no cap. When the queue is full, additional requests are rejected until a slot opens. |
| `bypass-permission` | `ezrtp.queue.bypass` | Players with this permission skip the queue and teleport immediately. Useful for VIP or staff. |
| `start-delay-ticks` | `20` | How long to wait before processing the first queued request after a quiet period, in server ticks (20 ticks = 1 second). |
| `interval-ticks` | `40` | Pause between each queued teleport (20 ticks = 1 second). Setting this to `40` means at most one teleport every 2 seconds. |

## Example — busy survival server

```yml
enabled: true
max-size: 20          # up to 20 players can wait
bypass-permission: "ezrtp.queue.bypass"
start-delay-ticks: 20  # begin processing after 1 second
interval-ticks: 40     # one teleport every 2 seconds
```

{: .note }
**Tick reference:** 20 ticks = 1 second. If you want a 3-second gap between
teleports, set `interval-ticks: 60`.
