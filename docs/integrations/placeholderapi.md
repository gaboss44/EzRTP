---
title: PlaceholderAPI
nav_order: 2
parent: Integrations
---

# PlaceholderAPI Integration

Use this integration when you want dynamic values (player name, rank, stats, etc.) in GUI icon text.

## What EzRTP supports

EzRTP resolves PlaceholderAPI placeholders in GUI icon names and lore when PlaceholderAPI is installed.

Typical examples:
- `%player_name%`
- `%vault_eco_balance_formatted%`
- placeholders from your rank/stats plugins

If PlaceholderAPI is not installed, GUI still works; placeholders remain unresolved text.

## Where to configure it

File: `plugins/EzRTP/gui.yml`

Edit icon fields under `worlds.<id>.icon`:

```yml
worlds:
  overworld:
    icon:
      name: "<green><bold>%player_name%'s RTP</bold></green>"
      lore:
        - "<gray>Balance: <white>%vault_eco_balance_formatted%</white></gray>"
```

Why here: PlaceholderAPI support is currently tied to GUI presentation fields.

## Practical setup checklist

1. Install PlaceholderAPI.
2. Install required placeholder expansions for the plugins you reference.
3. Restart or reload plugins.
4. Open `/rtp` GUI and confirm placeholders render for players.

## Common pitfalls

- **Raw `%placeholder%` shown in GUI:** missing expansion or typo in placeholder key.
- **Works for ops only:** placeholder provider plugin may enforce permissions.
