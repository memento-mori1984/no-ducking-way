# Bookmark — Install NoDuckingWay (resume tomorrow)

**Status (2026-07-07):** Play Protect still blocks browser/sideload install. **Next step: USB install from PC.**

---

## Quick links

| What | URL / path |
|------|------------|
| Latest APK release | https://github.com/memento-mori1984/no-ducking-way/releases/latest |
| Use this file | `NoDuckingWay-1.0.2.apk` (~10 MB) |
| Project on PC | `C:\Users\Ranzh\AndroidStudioProjects\NoDuckingWay` |
| USB install script | `.\scripts\install-via-usb.ps1` |
| Full install guide | [INSTALL-PHONE.md](INSTALL-PHONE.md) |

---

## Tomorrow — do this first (USB, bypasses Play Protect)

### Phone setup (one time)

1. **Settings → About phone** → tap **Build number** 7×.
2. **Settings → Developer options**:
   - **USB debugging** → ON
   - **Samsung:** **Install via USB** → ON
3. Plug phone into PC (data cable).
4. Tap **Allow USB debugging** on phone.

### PC — run install

```powershell
cd C:\Users\Ranzh\AndroidStudioProjects\NoDuckingWay
.\scripts\install-via-usb.ps1
```

Or tell Grok Build: **“phone connected”** and it can run this for you.

---

## If USB still fails tomorrow

1. **Play Store → Profile → Play Protect → Settings** → OFF “Scan apps with Play Protect” → install → turn back ON.
2. **Samsung:** **Settings → Security → Auto Blocker** → OFF temporarily.
3. Copy APK to phone **Download** folder via USB → open with **My Files** (not Chrome).
4. Reply with **phone model + Android version** for device-specific steps.

---

## What’s already done

- App builds and works (Mixer/Owner modes, QS tile, boot option).
- GitHub repo: https://github.com/memento-mori1984/no-ducking-way
- Releases: v1.0.0 → v1.0.2 (release-signed, not debug).
- Package ID: `com.noduckingway.app`
- Emergent architecture plan implemented.

---

## After install

1. Open **NoDuckingWay** → allow **Notifications**.
2. **Mixer** mode ON → test with Spotify + notification.
3. Add **Quick Settings** tile; **Allow unrestricted battery** on Samsung if needed.

See [TESTING.md](TESTING.md).