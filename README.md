# Automation Assistant

Minimal Jetpack Compose app that exposes an `AccessibilityService` used as a UI-automation
aid: it watches `TYPE_WINDOW_STATE_CHANGED` events and, when a window from a configured
package or class appears, performs `GLOBAL_ACTION_BACK` (falling back to
`GLOBAL_ACTION_HOME`) so the app under test is never left covered.

No root. No network. No screen content is read, stored or transmitted.

## Project layout

```
AutomationAssistant/
├── .github/workflows/build-apk.yml      CI: builds + publishes the APK
├── gradle/wrapper/                      Gradle 8.9 wrapper
├── settings.gradle.kts
├── build.gradle.kts                     AGP 8.7.3 · Kotlin 2.0.21
└── app/
    ├── build.gradle.kts                 minSdk 26 · targetSdk 35 · JDK 17
    └── src/main/
        ├── AndroidManifest.xml          declares the AccessibilityService
        ├── java/com/example/automationassistant/
        │   ├── MainActivity.kt          single-activity Compose host
        │   ├── automation/OverlayAutomationService.kt
        │   └── ui/AssistantScreen.kt    Material 3 dark single-screen UI
        └── res/
            ├── xml/accessibility_service_config.xml
            ├── values/{strings,themes,colors}.xml
            ├── drawable/ic_launcher_foreground.xml
            └── mipmap-anydpi-v26/ic_launcher.xml
```

## Configuring dismiss targets

Targets ship **empty** — the service is inert until you fill them in. Edit the two sets at
the top of the companion object in
`app/src/main/java/com/example/automationassistant/automation/OverlayAutomationService.kt`:

```kotlin
private val dismissTargetPackages: Set<String> = emptySet()   // e.g. "com.example.blocker"
private val dismissTargetClasses: Set<String> = emptySet()     // e.g. "android.app.Dialog"
```

The status card in the UI reports how many targets are configured. Matching is debounced
by 400 ms so a dismissal can never feed back into itself, and the service never acts on
its own package.

## Build locally

```bash
./gradlew assembleDebug
# → app/build/outputs/apk/debug/app-debug.apk
```

Requires JDK 17 and an Android SDK with `platforms;android-35` +
`build-tools;35.0.0`. Set `sdk.dir` in a local `local.properties` (git-ignored).

## Build on GitHub Actions

Pushes to `main` run `.github/workflows/build-apk.yml`:

1. **build** — JDK 17 + `./gradlew assembleDebug`, uploads `AutomationAssistant-debug-apk`.
2. **release** — runs only for tags matching `v*`, attaches the APK to a GitHub Release.

The repository is private, so downloading the APK to a phone requires signing in to
github.com in the mobile browser first (both Actions artifacts and release assets are
auth-gated). Create a downloadable link with:

```bash
git tag v1.0.0 && git push origin v1.0.0
```

## Install on a device

1. Download `app-debug.apk` from the release or workflow run.
2. Settings → enable *Install unknown apps* for your browser → install.
3. Open **Automation Assistant** → **Open Accessibility Settings** → find
   *Automation Assistant* → toggle **On** → confirm the system dialog.
4. The status card flips to **Active**.
