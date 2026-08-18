# Unlock Count Live Wallpaper

A modernized Android live wallpaper inspired by data-driven wellbeing experiments. It tracks how many times you unlock your phone each day and renders an animated counter on your home screen.

## Screenshots

| Home | Wallpaper |
|------|-----------|
| ![Home](screenshots/home.png) | ![Wallpaper](screenshots/wallpaper.png) |

## What it does

- Counts daily unlocks via `ACTION_USER_PRESENT`
- Animates counter transitions with interpolated digit motion
- Lets you enable/disable touch interaction from settings
- Supports wallpaper preview without registering the broadcast receiver

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

## Set wallpaper

1. Open the app
2. Tap the button to launch the live wallpaper picker
3. Select **Unlock Count Live Wallpaper**

## Possible features

- Per-day history and weekly averages
- Theme colors and font choices
- Haptic feedback on unlock transitions
- Export daily unlock stats to CSV
- Bedtime dim mode

## Status

Working first version.
