# Changelog

All notable changes to EzRTP are documented here.

Format follows [Keep a Changelog](https://keepachangelog.com/en/1.0.0/).
Versions follow [Semantic Versioning](https://semver.org/spec/v2.0.0.html).
Release tags use the `v` prefix (e.g. `v3.0.2`).

---

## [Unreleased]

### Added

### Changed

### Fixed

### Removed

---

## [3.1.0] - 2026-05-09

### Added

- **Message suppression via config** (`config.yml`):
  - `messages.suppress-player`: when `true`, silences all teleport-related messages to players globally (searching, countdown, queue position, success, failure, cost).
  - `messages.suppress-console`: when `true`, silences the executor notification that `/forcertp` sends to the command sender globally.
- **`--skip-message` command flag**: can be appended to `/rtp`, `/rtp <center|region>`, `/forcertp <player> [world]`, and `/rtp forcertp <player> [world]` to suppress both player-facing and executor messages for that single invocation. Tab-completion suggests the flag.

---

## [3.0.2] - 2026-05-09

### Added

- Initial changelog entry. See repository history for prior changes.

---
