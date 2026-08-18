# Unlock Count Live Wallpaper

A modernized Android live wallpaper that tracks how many times you unlock your phone each day and renders an animated counter on your home screen.

## Features

- Daily unlock counter with animated digit transitions
- Particle burst effect on unlock transitions
- Customizable counter and background colors
- Adjustable animation speed
- Simple launcher screen with one-tap wallpaper setting
- Modern AndroidX setup with Gradle 8.7

## How to use

1. Install the app
2. Open it and tap **CLICK HERE**
3. Select **Unlock Count Live Wallpaper** from the live wallpaper picker
4. Set it as your home screen wallpaper

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

## Possible features

- Per-day history and weekly averages
- Haptic feedback on unlock transitions
- Export daily unlock stats to CSV
- Bedtime dim mode
- Additional font choices

## Status

Working first version.
