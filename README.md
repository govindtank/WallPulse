# WallPulse

**WallPulse** is a trend-forward, privacy-first live wallpaper app built by **Govind Tank**.  
No data leaves your device. Everything runs locally with customizable modes, live previews, and production-ready UI/UX.

---

## Features

- **16 Live Modes** — from classic counter to matrix-name reveal, particle waves, aurora, cyber grid, and more
- **Live Previews** — see each mode animate directly in the picker before applying
- **Deep Customization** — colors, effects, fonts, spacing, timing, and glow per mode
- **Fun Picker UX** — glassmorphism grid with Random and Trending selection
- **Dark Mode** — full Material You DayNight theme support
- **Offline & Private** — all data stays on-device; no analytics, no network calls

## Modes

| Category | Modes |
|---|---|
| **Data-Driven** | Classic, Time Flow, Particle Wave, Gradient Pulse, Data Stream |
| **Visual Effects** | Aurora, Matrix Rain, Cosmic Dust, Liquid Metal, Cyber Grid, Ocean Depth |
| **Name Reveal** | Name Reveal — matrix rain decodes your custom name character by character |
| **Info Display** | DateTime, Battery, Steps, Notifications |

### Name Reveal
Matrix-style falling characters rain across the screen, then the center text decodes from random glyphs into your custom name, holds for a configurable duration, and loops.

## Setup

1. Install the app
2. Open **WallPulse** and choose a mode from the grid
3. Tap **Set Wallpaper**, or use **Random** / **Trending** for quick selection
4. Long-press a mode or open its settings to customize colors, fonts, spacing, effects, and timing

## Privacy

- Unlock counts and history are stored locally only
- No analytics, no ads, no network calls
- No permissions beyond wallpaper and optional settings storage

## Build

```bash
cd datalivewallpaper
ANDROID_HOME=$ANDROID_HOME JAVA_OPTS="-Djava.net.preferIPv4Stack=true" ./gradlew assembleDebug
```

## Repo

- **GitHub:** https://github.com/govindtank/unlock-count-live-wallpaper
- **Package:** `com.govindtank.wallpulse`
- **Author:** Govind Tank

## License

MIT
