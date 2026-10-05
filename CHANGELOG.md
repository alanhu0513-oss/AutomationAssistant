# Changelog

All notable changes to Universal Gaming Shield. Versions follow
[semantic versioning](https://semver.org/); releases are tagged `v*` and
built automatically by GitHub Actions.

## 2.1.0

### Added
- **Shield Activity log** — every dismissal recorded locally (what popped up,
  over which game, when), capped at 100 entries, with a clear action.
- **Device survival guide** — detects the manufacturer family (MIUI/HyperOS,
  ColorOS, One UI, Funtouch/OriginOS, stock) and shows the exact battery-menu
  path to keep the service alive.
- **Offline watchdog banner** — appears when Android or an OEM battery manager
  silently disables the accessibility service; one tap re-opens settings.
- **Quick Settings tile** — shield status at a glance, tap to jump to the app
  or accessibility settings.
- **Preview mode** — log every would-be dismissal without pressing Back.
- **Per-game reaction levels** — Normal / Gentle / Strict debounce per title,
  cycled from the chip on each protected game row.
- **Simplified Chinese (简体中文)** — full interface localization.

### Changed
- Release builds now use R8 code shrinking (release APK ≈ 1.2 MB).
- `OverlayAutomationService` split into focused modules: `ShieldNotification`
  and `WindowResolvers`; game-picker logic extracted to `AppSelection` /
  `GamesSection` with new unit tests.
- Store hydration moved to a single `Application` class so activity, service
  and tile always share one store.
- Backup/device-transfer rules explicitly disabled; lint warnings addressed.

### Fixed
- README installation steps now match the actual onboarding flow.

## 2.0.0

### Added
- **Universal brand-agnostic detection** — window-type classification plus a
  runtime system-app check replace all vendor package lists; launcher and
  dialer resolved dynamically and always exempt.
- **Premium glassmorphism UI** — midnight-slate + neon theme, `GlassCard`,
  pulsing status aura, spring shield toggle.
- **Renewed onboarding** — three slides ending in *Let's Play!*.
- **Foreground-service notification** (`specialUse`, API 34+; `dataSync`
  fallback) and the Android 13+ `POST_NOTIFICATIONS` prompt.
- **System Capabilities & Limits** — permanent in-app transparency card.
- **Structured error handling** — engine/app-load failures surface as
  snackbars instead of crashing.

### Changed
- App rebranded to **Gaming Shield**; README rewritten as *Universal Gaming
  Shield* with an explicit disclaimer section.
