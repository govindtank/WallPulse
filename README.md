# Unlock Count Live Wallpaper

A modernized Android live wallpaper that tracks how many times you unlock your phone each day and renders an animated counter on your home screen.

## Features

- Daily unlock counter with animated digit transitions
- Particle burst effect on unlock transitions
- Counter/background color customization support
- Animation speed preference support
- Launcher dashboard with today's count and quick stats
- Settings activity for preferences

## How to use

1. Install the app
2. Open it to see today's unlock count and quick stats
3. Tap **Set Wallpaper** to choose **Unlock Count Live Wallpaper**
4. Open **Settings** to customize colors and effects

## Tech

- Kotlin
- `WallpaperService` + `SurfaceHolder`
- `SharedPreferences` for daily counter state
- `PreferenceFragmentCompat` for settings
- Modern AndroidX dependencies

## Setup

```bash
git clone https://github.com/govindtank/unlock-count-live-wallpaper.git
cd unlock-count-live-wallpaper
./gradlew assembleDebug
adb install -r app/build/outputs/apk/debug/app-debug.apk
```

## Roadmap

- Per-day history and weekly averages
- Haptic feedback on unlock transitions
- Export daily unlock stats to CSV
- Bedtime dim mode
- Multiple font choices
- Additional counter styles: minimal, detailed, glow

## Status

Working first version with basic customization support.

## License

MIT — see [LICENSE](LICENSE)
