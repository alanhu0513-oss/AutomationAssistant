# Automation Assistant

Jetpack Compose app that exposes an `AccessibilityService` used as a UI-automation aid:
it watches window events and, when an overlay from a configured **interrupter package**
appears over a configured **protected app**, performs `GLOBAL_ACTION_BACK` so the app
under test is never left covered.

No root. No screen content is read, stored or transmitted. The only network use is the
update check (GitHub Releases), which you can watch fail-soft in the app.

## What is new in 1.1.0

- **In-app application selector** — searchable list of installed launcher apps
  (`PackageManager` + a `<queries>` MAIN/LAUNCHER intent; no `QUERY_ALL_PACKAGES`).
- **SharedPreferences configuration** — no more editing source:
  - `target_protected_app` — app that must be in the foreground before anything is dismissed
  - `interrupter_package_name` — overlay package whose appearance triggers the dismiss
- **Diagnostics on the status card** — foreground package plus the last-seen window
  (`package/class`) the service observed.
- **Update notifier** — checks the public GitHub Releases feed and links out to the
  release page in the browser (no in-app install, no `REQUEST_INSTALL_PACKAGES`).
- Battery-optimization shortcut so the service is less likely to be killed in the background.

## Configuration

Open the app, pick a value for each row:

| Row | Meaning |
| --- | --- |
| **Protected app** | Optional. When set, dismissals only fire while this app is the foreground window. |
| **Interrupter package** | Required. The overlay package that gets dismissed with Back. |

Search in the selector, tap a row to assign it to the highlighted row (the row with the
border tells you which slot you are filling), then disable the selector with **Done**.
Picking the same package for both slots clears the other one.

The service stays inert until an interrupter package is configured, and the master
switch on the status card gates all dismiss actions.

## How dismissal works

Matching, foreground tracking and dispatch timing live in pure, unit-tested classes:

- `automation/OverlayRules.kt` — which events are tracked, when the foreground is adopted
  (never the interrupter overlay, never our own package) and when a trigger may fire.
- `automation/OverlayEngine.kt` — immediate first fire, then a single coalesced trailing
  fire re-verified against the last observed window, debounced by 400 ms so a dismissal
  can never feed back into itself. `GLOBAL_ACTION_BACK` only — no HOME fallback.
- `automation/TargetStore.kt` — the two persisted keys plus a `KeyValueStore` seam that
  unit tests replace with an in-memory implementation.
- `automation/OverlayAutomationService.kt` — thin adapter: Android event in,
  `EngineAction` out, status published through `AutomationState`.
- `data/UpdateRules.kt` + `data/ReleaseFeed.kt` — pure tag comparison and feed parsing.

The Compose layer never touches the service class; it only reads `AutomationState`
and `TargetStore` flows.

## Tests

```bash
./gradlew test
```

JVM unit tests cover foreground/trigger rules, dispatch scheduling, app search,
release-feed parsing, version comparison and preference storage. CI runs them before
every APK build; lint runs too and fails the build on errors (currently 0).

## Build locally

```bash
./gradlew assembleDebug
# → app/build/outputs/apk/debug/app-debug.apk
```

Requires JDK 17 and an Android SDK with `platforms;android-35` +
`build-tools;35.0.0`. Set `sdk.dir` in a local `local.properties` (git-ignored).

## Build on GitHub Actions

Pushes to `main` run `.github/workflows/build-apk.yml`:

1. **build** — JDK 17, `./gradlew test assembleDebug`, lint report, uploads
   `AutomationAssistant-debug-apk`.
2. **release** — runs only for tags matching `v*`, attaches the APK to a GitHub Release.

The repository is public, so the phone can poll the update feed anonymously:

```
GET https://api.github.com/repos/alanhu0513-oss/AutomationAssistant/releases/latest
```

Create a downloadable link with:

```bash
git tag v1.1.0 && git push origin v1.1.0
```

## Install on a device

1. Download `app-debug.apk` from the release page.
2. Settings → enable *Install unknown apps* for your browser → install.
3. Open **Automation Assistant** → pick the protected app and the interrupter package.
4. **Open Accessibility Settings** → find *Automation Assistant* → toggle **On** →
   confirm the system dialog.
5. Optionally tap **Battery optimization settings** and exempt the app.
6. The status card flips to **Active**.
