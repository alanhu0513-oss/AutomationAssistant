# Aegis (Game Window Protection)

[![Download APK](https://img.shields.io/badge/Download-Android%20APK%20(v2.2.0)-3DFFC4?style=for-the-badge&logo=android&logoColor=black)](https://github.com/aidenauu04l7/aegis/releases/latest/download/app-debug.apk)
[![Release](https://img.shields.io/github/v/release/aidenauu04l7/aegis?color=3DFFC4&label=Latest%20Release&style=for-the-badge)](https://github.com/aidenauu04l7/aegis/releases/latest)

> ⚠️ **IMPORTANT: HOW TO DOWNLOAD THE ACTUAL APP (NOT SOURCE CODE)**
> 
> * **DO NOT** click the green `<> Code` ➔ `Download ZIP` button on GitHub unless you are a software developer editing TypeScript source code.
> * **TO INSTALL THE APP ON YOUR PHONE**: Click the **[Download Android APK (v2.2.0)](https://github.com/aidenauu04l7/aegis/releases/latest/download/app-debug.apk)** button above or go to the **[Releases](https://github.com/aidenauu04l7/aegis/releases/latest)** section on the right side of this repository page and download `app-debug.apk` / `aegis-shield-v2.2.0.apk`.

---

## 📱 Quick 3-Step Phone Installation

1. **Download APK**: Tap [Download app-debug.apk](https://github.com/aidenauu04l7/aegis/releases/latest/download/app-debug.apk) on your Android device.
2. **Install**: Tap the downloaded file in your browser's download manager. When prompted, select **"Allow installation from this source"**.
3. **Turn on Shield Engine**:
   - Open **Aegis**.
   - Tap **Activate Shield** and allow the **Accessibility Service** permission under *Installed Services* / *Accessibility*.
   - Use the in-app **Quick Fix** utility to exempt Aegis from Android Battery Optimizations.

---

## 🌟 Features

- 🎯 **Universal Popup Dismissal Engine** — Automatic detection and instant simulated `Back` dismissal for surface-level OEM game timer popups (Xiaomi, vivo, OPPO, Samsung).
- ⚡ **Quick Fix Battery Optimization Utility** — Direct Android Intent shortcuts to exclude the app from background sleep and Doze mode.
- 🎮 **Per-Game Reaction Control** — Custom debouncing profiles (Normal 400ms, Gentle 1,000ms, Strict 100ms).
- 🧪 **Interactive Web Simulation Suite & WebAPK** — Test all window event state transitions and install as a standalone home screen app.
- 🛡️ **Zero Root & 100% Privacy** — Operates strictly in standard user-space with no analytics, no ads, and no network transmission.

---

## 📦 Automated APK CI Builds

Every GitHub release and push to `main` automatically compiles a signed debug `.apk` binary using GitHub Actions (`.github/workflows/build-apk.yml`). The compiled binaries are published under [GitHub Releases](https://github.com/aidenauu04l7/aegis/releases).

---

## 💻 Web Development

```bash
npm install
npm run dev
```
