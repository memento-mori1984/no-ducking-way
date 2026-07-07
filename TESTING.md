# Testing Guide — No Ducking Way

## Before you start

| Requirement | Why |
|-------------|-----|
| **Physical phone** | Emulators do not model real audio focus or OEM ducking |
| **Music app** | Spotify, YouTube Music, or Plexamp |
| **Second audio source** | Google Maps navigation, WhatsApp voice note, or system notification |
| **Headphones (optional)** | Validates route-change recovery |

Install debug build:

```powershell
adb install -r app\build\outputs\apk\debug\app-debug.apk
```

Or **Run** from Android Studio with USB debugging enabled.

---

## First-run checklist

1. Open **NoDuckingWay**
2. Accept **Notifications** permission (Android 13+)
3. Leave **Mixer (recommended)** selected
4. Turn **Protect music volume** ON
5. Confirm persistent notification: *"NoDuckingWay is active"*
6. (Optional) Add **Quick Settings tile**: edit QS panel → drag *NoDuckingWay* tile in
7. (Optional) Tap **Allow unrestricted battery use** on Samsung/Xiaomi if service stops in background

---

## Core test scenarios

### 1. Notification ducking (Mixer mode)

| Step | Action | Expected |
|------|--------|----------|
| 1 | Start music at ~70% volume | Music plays normally |
| 2 | Enable NoDuckingWay (Mixer) | Notification appears; music continues |
| 3 | Receive SMS / chat notification sound | Music volume **stays** near previous level |
| 4 | Disable protection | Music may duck again on next notification |

### 2. Navigation voice (Mixer mode)

| Step | Action | Expected |
|------|--------|----------|
| 1 | Music playing | — |
| 2 | Start Google Maps driving directions with voice | TTS plays **over** or beside music; music does not drop to whisper volume |
| 3 | Repeat with protection OFF | Compare — ducking may be obvious |

### 3. Owner mode fallback

If Mixer fails on your device:

1. Turn protection **OFF**
2. Select **Owner** mode
3. Turn protection **ON**
4. Repeat scenarios 1–2

Owner holds media focus; some apps pause Spotify on aggressive ROMs — note behavior in your bug report.

### 4. Quick Settings tile

| Step | Expected |
|------|----------|
| Tile shows OFF when service stopped | Gray/inactive |
| Tap tile ON | Service starts; tile active |
| Tap tile OFF | Service stops; notification gone |

### 5. Headphone route change

| Step | Expected |
|------|----------|
| Protection ON, music playing | — |
| Unplug wired headphones (or disconnect BT) | Service stays active; re-test notification ducking |
| Plug headphones back in | Protection still active after route change |

### 6. Stop from notification

| Step | Expected |
|------|----------|
| Tap **Stop** on foreground notification | Service ends; switch in app shows OFF when reopened |

### 7. Start on boot (optional)

1. Enable **Start automatically after reboot**
2. Enable protection, then reboot phone
3. Unlock device — within ~10s service should run if battery settings allow

---

## Recommended device matrix

| Priority | Device class | Android | Notes |
|----------|--------------|---------|-------|
| P0 | Google Pixel | 14–15 | Baseline AOSP behavior |
| P0 | Samsung Galaxy | One UI 6+ | Aggressive battery + audio policy |
| P1 | Xiaomi / Redmi | MIUI 14+ | Service kill risk — test battery exemption |
| P1 | Motorola / stock-ish | 13–14 | Common US mid-range |
| P2 | Android Auto head unit | — | Navigation ducking is primary use case |

---

## Collecting logcat (bug reports)

Filter audio-related logs while reproducing:

```bash
adb logcat -c
adb logcat -s NoDuckingService AudioManager AudioTrack NoDuckingWay
```

Or broader:

```bash
adb logcat | findstr /i "AudioFocus duck noducking"
```

Include in reports:

- Phone model and Android version
- Music app used
- Mixer vs Owner mode
- Whether battery optimization was disabled for this app
- Steps to reproduce
- Logcat snippet (30–60 seconds around the ducking event)

---

## Troubleshooting

| Symptom | Things to try |
|---------|----------------|
| Service stops after screen off | Battery optimization → allow unrestricted; disable adaptive battery for this app |
| Music pauses instead of staying loud | Switch from Owner → Mixer (or vice versa) |
| No notification / service won't start | Grant notification permission in system Settings |
| Works once then dies | OEM killed FGS — see [dontkillmyapp.com](https://dontkillmyapp.com) for your brand |
| `ForegroundServiceTypeException` on Android 14+ | Rebuild latest code; manifest must declare `foregroundServiceType="mediaPlayback"` |

Samsung path (example): **Settings → Apps → NoDuckingWay → Battery → Unrestricted**

---

## Automated tests

Unit/instrumentation stubs exist (`ExampleUnitTest`, `ExampleInstrumentedTest`). Audio focus cannot be meaningfully unit-tested on JVM; rely on manual matrix above until a device farm or mocked `AudioManager` layer is added.

Run existing tests:

```bash
./gradlew test
./gradlew connectedAndroidTest   # requires device/emulator
```

---

## Pass / fail criteria for v1.0

**Pass** on a device if:

- Service starts and survives 15+ minutes with screen off (with battery exemption granted)
- At least one ducking trigger (notification **or** Maps voice) shows **audibly less** ducking with protection ON vs OFF in Mixer mode
- Quick Settings tile toggles service reliably
- No crash on headphone plug/unplug

**Fail** if:

- Service crashes on start (check logcat for `ForegroundServiceTypeException`)
- Protection ON makes music behavior **worse** (unexpected pause) with no workaround in Owner/Mixer toggle