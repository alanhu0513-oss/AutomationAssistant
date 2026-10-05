# Aegis (Web Edition)

**Stop surface-level game-time popups before they end your match.**

Aegis is a web companion & simulation suite for Android game protection that implements intelligent window-stack monitoring and overlay dismissal logic.

Midnight-slate glass interface. Neon-green pulse. Zero root. No personal data or screen content is ever read, stored, or transmitted.

---

## Features Ported to React & TypeScript

- 🎯 **Universal Popup Dismissal Rules Engine** — pure TypeScript port of `OverlayRules` and `OverlayEngine`, classifying window types, resolving dynamic system levels, foreground adoption, and per-game debounce.
- 🎮 **Interactive Game Protection** — one-tap toggling for protected games with per-title reaction levels (Normal 400 ms, Gentle 1,000 ms, Strict 100 ms).
- 🧪 **Live Shield Simulator Sandbox** — test real-time window events, popup dismissal rules, debouncing, and preview mode right in your browser.
- ✨ **Signature Midnight Glass UI** — Material 3 midnight-slate dark theme, frosted glass cards with gradient hairline borders, and pulsing aura animations.
- 🚀 **Onboarding Walkthrough** — three-slide interactive setup walkthrough with neon pagination pills and action steps.
- 🛡️ **Device Survival Guide** — brand-specific battery guides (Xiaomi / MIUI / HyperOS, OPPO / ColorOS, Samsung One UI, vivo / Funtouch, Stock Android) with interactive brand selector.
- 📜 **Shield Activity Proof-of-Work Log** — capped, local-first persistent activity log with timestamp, preview badges, and clear action.
- 🔔 **Honest by Design (Capabilities & Limits)** — permanent transparency card explaining standard popup dismissal, OS security boundaries, and the 1% latency rule.
- 👁️ **Preview Mode** — log would-be dismissals without executing the back action.
- 🌍 **Bilingual Support** — complete English & 简体中文 localization with instant language switching.
- 📡 **GitHub Release Checker** — silently checks for updates against GitHub Releases using semantic version comparison.

---

## Project Structure

- `src/automation/` — ported decision rules, debounced state machine, persistent target store, capped log, and state flows.
- `src/data/` — OEM battery guides, game catalog & custom app manager, search filter, GitHub release checker.
- `src/components/` — GlassCard, PulsingAura, StatusCard, ShieldToggle, GamesSection, DeviceHealthCard, InteractiveSimulator, ShieldLogCard, Onboarding, etc.
- `src/i18n/` — translations for English and 简体中文.

---

## Development

```bash
npm install
npm run dev
```
