# AGENTS.md — EzRTP Development Guide

This document defines repository-wide guidance for contributors and coding agents working in `EzRTP`.

## Scope
- Applies to the entire repository rooted at this file.

## Goals
- Keep behavior stable for server owners across Bukkit/Paper/Spigot/Purpur.
- Prefer compatibility and safe fallbacks over platform-specific assumptions.
- Make changes easy to review, test, and reason about.

## Repository layout
- `src/main/java/...`: primary plugin implementation and shared runtime wiring.
- `src/test/java/...`: unit/integration-style tests (including compatibility shims/mocks).
- `ezrtp-common/`: platform abstraction interfaces and shared adapters.
- `ezrtp-bukkit/`, `ezrtp-paper/`, `ezrtp-spigot/`, `ezrtp-purpur/`: platform provider modules.
- `src/main/resources/*.yml`: default config and user-visible messages.

## Build & test expectations
- Java version: **17**.
- Preferred verification flow before submitting:
  1. `mvn -q -DskipTests compile`
  2. `mvn -q test`
- If a full test run is too expensive, run targeted tests for touched areas and explicitly note what was skipped.

## Code quality standards
- Keep classes focused; favor small methods with clear names.
- Avoid silent behavior changes in teleport safety, cooldowns, and economy checks.
- Do not introduce blocking operations on the main server thread.
- Preserve cross-platform abstractions:
  - Add platform-specific behavior in platform modules.
  - Keep shared logic in common/core modules.
- Prefer configuration-driven behavior over hardcoded values.
- Add/adjust tests for bug fixes or non-trivial logic changes.

## Configuration & messages changes
- When changing defaults in `config.yml`, `messages/*.yml`, `gui.yml`, `queue.yml`, or `network.yml`:
  - Keep backward compatibility in mind.
  - Document migration impacts in PR notes.
  - Ensure placeholder keys and message keys remain consistent.

## Performance & safety
- Avoid repeated expensive world/biome lookups when cacheable.
- Guard async/sync boundaries carefully when interacting with Bukkit/Paper APIs.
- Treat teleport destination validation as safety-critical; prefer conservative fallbacks.

## PR readiness checklist
- Code compiles.
- Relevant tests pass (or justified exceptions documented).
- New/changed behavior is covered by tests when practical.
- User-facing config/message changes are clearly described.
- No unrelated refactors mixed into focused fixes.
