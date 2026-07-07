# No Ducking Way

Android app that reduces **audio ducking** — when background music (Spotify, YouTube Music, etc.) gets quieter because notifications, navigation voice, or other sounds play.

No Ducking Way runs a lightweight foreground service that keeps a silent audio stream active so media can stay at full volume. Results vary by phone manufacturer and Android version; see [TESTING.md](TESTING.md).

## Features

- **Mixer mode (default)** — silent sonification stream; does not fight Spotify for media focus
- **Owner mode** — holds `AUDIOFOCUS_GAIN` when Mixer is not enough on aggressive OEM ROMs
- **Quick Settings tile** — one-tap toggle
- **Optional start on boot**
- **Audio route recovery** — restarts silent playback when headphones plug/unplug
- **Battery optimization prompt** — helps the service survive on Samsung/Xiaomi-style ROMs

## Requirements

- Android 7.0+ (API 24); tested target SDK 36
- Physical device recommended (emulators have unreliable audio focus)
- Android Studio with JDK 17+ (bundled JBR works)

## Build

```bash
git clone https://github.com/memento-mori1984/no-ducking-way.git
cd no-ducking-way
./gradlew assembleDebug
```

APK output: `app/build/outputs/apk/release/app-release.apk`

**Install on your phone:** [Latest release APK](https://github.com/memento-mori1984/no-ducking-way/releases/latest) — see [INSTALL-PHONE.md](INSTALL-PHONE.md) if install fails.

On Windows:

```powershell
.\gradlew.bat assembleDebug
```

Open in Android Studio: **File → Open** → select the project folder.

## Install & quick test

1. Enable **Developer options** and **USB debugging** on your phone
2. Connect USB → Run from Android Studio, or sideload `app-debug.apk`
3. Grant **Notifications** when prompted (Android 13+)
4. Toggle **Protect music volume** ON
5. Play music in another app → trigger a notification or Maps voice
6. If ducking persists, switch to **Owner** mode in the app

Full test matrix: [TESTING.md](TESTING.md)

## Permissions (why each exists)

| Permission | Purpose |
|------------|---------|
| `FOREGROUND_SERVICE` / `FOREGROUND_SERVICE_MEDIA_PLAYBACK` | Keep protection running while music plays |
| `POST_NOTIFICATIONS` | Show required foreground-service notification (Android 13+) |
| `RECEIVE_BOOT_COMPLETED` | Optional auto-start after reboot (user opt-in) |
| `MODIFY_AUDIO_SETTINGS` | Audio routing / focus management |
| `WAKE_LOCK` | Keep silent playback thread alive during brief CPU sleep |
| `REQUEST_IGNORE_BATTERY_OPTIMIZATIONS` | User-initiated exemption so OEMs do not kill the service |

Security details: [SECURITY.md](SECURITY.md)

## Architecture

```
MainActivity ──toggle──► NoDuckingService (foreground)
                              ├── silent AudioTrack (Mixer or Owner attrs)
                              ├── optional AudioFocusRequest (Owner mode)
                              └── AudioRouteWatcher (headset / becoming noisy)

NoDuckingTileService ──QS tile──► start/stop service
BootReceiver ──if pref──► start service after BOOT_COMPLETED
```

## Limitations

- **Not a system-wide guarantee.** Some OEMs override audio policy or kill background services.
- **Calls and alarms** may still lower music volume (expected Android behavior).
- **Play Store gray zone:** persistent media foreground services must be disclosed honestly in store listings.

## Documentation

- [TESTING.md](TESTING.md) — device test matrix, logcat, troubleshooting
- [SECURITY.md](SECURITY.md) — permissions, data handling, threat model, reporting

## License

Copyright (c) 2026 Zachary H. Roberts. See [LICENSE](LICENSE).

## Credits

Architecture review coordinated via Grok Link and Emergent during initial development.