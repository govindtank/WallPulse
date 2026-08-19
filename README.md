# Unlock Count Live Wallpaper

A minimal, private live wallpaper that turns your unlock events into visual patterns. No data leaves your device.

## Modes

- **Classic** — animated counter with particles and customization.
- **Time Flow** — 24-hour arc with unlock markers for a rhythmic daily view.

## Setup

1. Install the app.
2. Open it and choose a mode.
3. Tap **Set Wallpaper**.
4. Customize colors, dark mode, and animation from the mode settings.

## Privacy

Unlock counts and history are stored locally only. No analytics, no network calls.

## Build

```bash
cd datalivewallpaper
ANDROID_HOME=$ANDROID_HOME JAVA_OPTS="-Djava.net.preferIPv4Stack=true" ./gradlew assembleDebug
```
