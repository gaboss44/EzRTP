# Development & Collaboration Guide

This guide is for contributors working on EzRTP source code, reviews, and releases.

## Project architecture (quick map)

- `ezrtp-common/`: shared teleport logic, configuration loading, abstractions, and tests.
- `ezrtp-bukkit/`: base runtime plugin packaging (produces `EzRTP-<version>.jar`).
- `ezrtp-paper/`: Paper-specific platform adapters and module plugin.
- `ezrtp-purpur/`: Purpur-specific module plugin and platform adapters.
- `ezrtp-spigot/`: Spigot-oriented module in the multi-module build.
- `src/main/resources/`: default configs/messages copied into plugin artifacts.

## Local prerequisites

- Java 17
- Maven 3.9+ (recommended)

## Typical contributor workflow

1. Create a focused branch for one fix/feature.
2. Make changes in the smallest relevant module:
   - shared behavior -> `ezrtp-common`
   - platform behavior -> `ezrtp-paper` / `ezrtp-purpur` / `ezrtp-bukkit`
3. Add or update tests for non-trivial behavior changes.
4. Run validation commands before opening a PR.
5. Document any config/message-key impact in PR notes.

## Build and test commands

Use these commands from repository root:

```bash
mvn -q -DskipTests compile
mvn -q test
```

For quick module checks during iteration:

```bash
mvn -q -pl ezrtp-common -DskipTests compile
mvn -q -pl ezrtp-paper -DskipTests compile
```

## Collaboration conventions

- Keep PRs focused; avoid unrelated refactors.
- Prefer compatibility-safe fallbacks over platform assumptions.
- Avoid blocking operations on the server main thread.
- Keep async/sync boundaries explicit around Bukkit/Paper API interactions.
- Treat teleport safety, cooldown, queueing, and economy checks as behavior-sensitive.

## Cross-module change strategy

When behavior spans modules:

1. Define/adjust shared contracts in `ezrtp-common` first.
2. Implement per-platform providers in platform modules.
3. Verify service/provider registration remains consistent.
4. Add tests where practical for both shared logic and platform adapters.

## Config and message changes

If you modify files like `config.yml`, `gui.yml`, `queue.yml`, `network.yml`, or `messages/*.yml`:

- Preserve backward compatibility where possible.
- Keep placeholders/message keys consistent.
- Call out migration or operator action in PR notes.

## Reviewing checklist

Before merging:

- Build compiles.
- Relevant tests pass (or explicitly justified if skipped).
- Behavior changes are covered by tests when practical.
- User-facing docs/config impacts are documented.
- No accidental module-mismatch instructions were introduced.
