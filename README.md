# Universal Gaming Shield

**Stop surface-level game-time popups before they end your match.**

Universal Gaming Shield (in-app name: **Gaming Shield**) is a premium Android
companion that watches your screen's window stack with a lean Accessibility
service and — while one of your protected games is in the foreground — closes
sudden system overlays the instant they appear, by simulating a single system
`Back` command.

Midnight-slate glass interface. Neon-green pulse. Zero root. No screen content
is ever read, stored, or transmitted.

---

## Features

- 🎯 **Universal popup dismissal** — classifies windows by *type* (system
  dialogs, accessibility overlays) and runtime system-app status, then
  dismisses them over your protected games. Works across Samsung, Xiaomi,
  vivo, OPPO, OnePlus, Pixel and every other standard Android OEM — with
  **zero vendor package lists** in the source.
- 🎮 **One-tap game protection** — a clean visual list of your installed apps;
  flip the switch next to each title you want shielded. Icons and names only —
  no package names, no technical inputs.
- ✨ **Premium glass UI** — Material 3 dark theme, frosted glass cards, a
  pulsing neon aura that shifts from *Engine Standby* to *Shield Operational*,
  and a spring-loaded shield toggle.
- 🚀 **Onboarding walkthrough** — a three-slide intro that grants the one
  permission the app truly needs and shows you how to lock the app in memory.
- 🔔 **Honest by design** — a permanent **System Capabilities & Limits** card
  right on the dashboard tells you exactly what the shield does perfectly and
  where Android draws the line. No overpromises, ever.
- 📡 **Silent update check** — polls GitHub Releases and shows a card only when
  a newer version actually exists. Fails soft; never blocks the UI.
- 🔒 **Privacy-first** — the accessibility service observes *window metadata
  only*. No screen content, keystrokes or personal data are captured, stored,
  or sent anywhere. The only network call is the anonymous release check.

## How It Works

The decision logic is pure Kotlin and fully unit-tested; the Android layer is
a thin adapter around it:

| Module | Role |
|---|---|
| `automation/OverlayRules.kt` | Window classification, foreground adoption, dismissal decisions |
| `automation/OverlayEngine.kt` | Immediate + coalesced trailing fire, 400 ms self-feedback debounce |
| `automation/OverlayAutomationService.kt` | Accessibility event adapter, foreground notification, error surfacing |
| `automation/TargetStore.kt` | Persisted protected-app set behind a testable key-value seam |

Dispatch is deliberately conservative: `GLOBAL_ACTION_BACK` only, never at the
user's home launcher, never at the incoming-call dialer, never at the keyboard.

## 📱 Supported Devices

| | |
|---|---|
| **OS** | Android 8.0 (API 26) → Android 15 (targetSdk 35) |
| **UI** | Material 3, dark glass theme |
| **Root** | Not required — standard Accessibility APIs only |

## ⚡ Quick Installation

1. Download `app-debug.apk` from the [latest release](../../releases/latest)
   and install it (allow *Install unknown apps* for your browser first).
2. Launch **Gaming Shield** and swipe through the three intro slides to the
   **Let's Play!** button.
3. **Step 1: Grant Access** — tap the button, find *Gaming Shield* under
   **Installed Services / Accessibility**, and switch it on. This is the one
   permission the shield needs to observe and dismiss popups; everything else
   below is optional.
4. **Step 2: Lock the Shield** — open your phone's multitasking menu, press
   and hold the app card, and tap the **Lock** padlock so task killers can't
   stop the service mid-game.
5. Back on the dashboard, flip the switches next to the games you want
   protected — the status card turns to **Shield Operational**.
6. *(Recommended)* Tap **Keep protection running** and exempt the app from
   battery optimization so OEM battery savers leave it alone.

That's it — the next time a manufacturer timer pops over your game, it closes
itself.

## ⚠️ Disclaimer & Technical Limitations

We would rather be transparent than popular. **No third-party app can
guarantee a 100% bypass rate**, and this one does not claim to. Here is the
honest engineering picture:

**What the shield handles perfectly.**
Surface-level manufacturer time-limit overlays — the standard VIVO, Xiaomi and
OPPO popup timers, routine permission prompts, and similar window-level
interruptions — are detected and dismissed instantly, in the milliseconds after
they appear. This is the overwhelming majority of in-game interruptions, and
for those the shield is effectively seamless.

**Why a 100% guarantee is impossible.**
Android is a security-first operating system, and its protections are
deliberately layered *below* what any ordinary app can reach:

- **Kernel-level process freezing.** When the OS (or an aggressive platform
  tool such as Google Family Link, or an OEM task killer) freezes background
  third-party processes at the kernel level, no application code runs at all —
  including ours. Android's own security model enforces this.
- **Accessibility-layer deactivation.** If the platform temporarily
  deactivates or revokes the Accessibility layer, the shield is blind by
  design. It cannot (and should not) claw that back.
- **Evolving security patches.** Each Android security patch and OEM update
  may move these blocks deeper. An exploit-style bypass would break both the
  OS security model and this app's no-root promise — so we deliberately do not
  attempt one.

Deep, kernel-level locks and aggressive platform blocks are therefore outside
the app's reach. If the system fully commits to blocking you, the system wins.

**The 1% rule.**
If the phone is severely lagging or low on RAM while running a heavy title like
*Delta Force*, the shield might need a fraction of a second longer to react.
Close background apps, keep the phone charged, and reaction times stay in the
instant range.

**Honest expectation:** think of the shield as an elite surface-level defense,
not an omnipotent override. It handles the common popup war so you can play in
peace — and tells you plainly where the wall is.

The same limits are summarized in-app, permanently, under
**System Capabilities & Limits** on the dashboard.

## Tests & Quality

```bash
./gradlew test        # 54 JVM unit tests
./gradlew lintDebug   # 0 errors
```

CI (GitHub Actions) runs tests, lint and an APK build on every push; lint
errors fail the pipeline, so a green badge means a healthy build.

## Build Locally

```bash
./gradlew assembleDebug
# → app/build/outputs/apk/debug/app-debug.apk
```

Requires JDK 17 and an Android SDK with `platforms;android-35` +
`build-tools;35.0.0`. Set `sdk.dir` in a git-ignored `local.properties`.

Pushes to `main` build via `.github/workflows/build-apk.yml`; tags matching
`v*` attach the APK to a GitHub Release:

```bash
git tag v2.0.0 && git push origin v2.0.0
```

---

*Universal Gaming Shield — premium protection, honest boundaries.*
