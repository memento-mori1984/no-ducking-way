# Security — No Ducking Way

This document describes what the app can access, what it does **not** do, and how to report security issues.

## Threat model

No Ducking Way is a **local-only utility**. It does not connect to the internet, authenticate users, or sync data to a server.

| Asset | Risk if compromised | Mitigation |
|-------|---------------------|------------|
| User music listening context | Low — app only holds audio focus | No network; no audio recording |
| Foreground service persistence | Medium — battery drain if abused | User must explicitly enable; visible notification |
| Boot auto-start | Medium — surprise background activity | Off by default; requires preference opt-in |
| Local preferences | Low — mode + boot flag only | `SharedPreferences` on device; no secrets stored |

## Data collection & privacy

**The app does not collect, transmit, or sell personal data.**

| Data type | Stored? | Sent off-device? |
|-----------|---------|------------------|
| Account credentials | No | No |
| Location | No | No |
| Audio content / microphone | No | No |
| Music metadata | No | No |
| Analytics / crash telemetry | No (unless you add Firebase later) | No |

Local storage (`noducking_prefs`):

- Protection mode: `mixer` or `owner`
- `start_on_boot` boolean
- `battery_prompt_shown` boolean

Uninstalling the app removes this data.

## Permissions — security rationale

### Normal / expected

- **`FOREGROUND_SERVICE`** / **`FOREGROUND_SERVICE_MEDIA_PLAYBACK`** — Required to run continuous audio protection with a visible notification. Declared type must match actual behavior (silent media playback).
- **`POST_NOTIFICATIONS`** — Android 13+ requirement to show the foreground notification. User can deny; service cannot start without it.
- **`MODIFY_AUDIO_SETTINGS`** — Adjust audio routing for silent track playback. Does not change user volume sliders without user action.
- **`WAKE_LOCK`** — Prevents silent playback thread from stalling during brief idle periods. Not a full screen-on lock.

### Sensitive — user controlled

- **`RECEIVE_BOOT_COMPLETED`** — Only starts service if user enabled **Start automatically after reboot**. Receiver is exported (required by Android) but performs a single guarded action.
- **`REQUEST_IGNORE_BATTERY_OPTIMIZATIONS`** — Opens system dialog only when user taps the in-app button. Does not auto-grant.

### Not requested (by design)

- Internet / network state
- Microphone / record audio
- Contacts, SMS, call log
- Location
- Storage (beyond app-private prefs)
- Accessibility service
- Device admin

## Exported components

| Component | Exported | Notes |
|-----------|----------|-------|
| `MainActivity` | Yes | Launcher entry only |
| `NoDuckingTileService` | Yes | Protected by `BIND_QUICK_SETTINGS_TILE` — only system can bind |
| `BootReceiver` | Yes | Standard boot broadcast; gated by user preference |
| `NoDuckingService` | No | Started only by app / tile / boot receiver |

No deep links, no `android:exported="true"` content providers, no arbitrary intent handlers.

## Supply chain

- Dependencies: AndroidX, Material Components, Kotlin — pin versions in `gradle/libs.versions.toml`
- Build from source; verify Gradle wrapper checksum when cloning
- Do not commit `local.properties` (contains machine-specific SDK path)

Recommended maintainer hygiene:

```bash
./gradlew dependencyUpdates   # if plugin added
```

Review Dependabot alerts on GitHub after publish.

## Foreground service transparency

Google Play and users expect honesty about persistent services:

- The notification clearly states the app is active
- **Stop** action ends the service immediately
- Silent audio is intentional and documented — not disguised tracking or ad fraud

Misrepresenting foreground service use can cause policy removal; this app’s stated purpose matches its implementation.

## Known limitations (not vulnerabilities)

- Cannot prevent all OEM-specific audio overrides without root
- Owner mode may cause third-party music apps to pause — user-selectable trade-off
- Battery exemption reduces OS protections — user must opt in

## Reporting a vulnerability

If you believe you found a security issue in **this repository**:

1. **Do not** open a public issue for exploitable details
2. Email or private message the maintainer with:
   - Description and impact
   - Affected version / commit
   - Reproduction steps
   - Suggested fix (optional)
3. Allow reasonable time for a patch before disclosure

For dependency vulnerabilities in AndroidX/Material, also check [GitHub Security Advisories](https://github.com/advisories) for upstream fixes.

## Security checklist for contributors

- [ ] No API keys, tokens, or `local.properties` in commits
- [ ] New permissions require README + SECURITY.md updates
- [ ] Exported components must have intent filters reviewed
- [ ] No `usesCleartextTraffic` unless strictly necessary
- [ ] ProGuard/R8 rules reviewed before release builds