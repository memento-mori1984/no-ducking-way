# Install on your phone

**Latest release:** https://github.com/memento-mori1984/no-ducking-way/releases/latest

Use **v1.0.5** or newer (proper release signing — not debug).

---

## Normal install

1. Open the release link in **Chrome** on your phone.
2. Expand **Assets** → download **NoDuckingWay-1.0.5.apk** (~10 MB).
3. Open **Downloads** → tap APK → **Install**.
4. Open **NoDuckingWay** → allow **Notifications** → turn protection **ON**.

---

## "App blocked to protect your device" (Play Protect)

This is **Google Play Protect** blocking apps not from the Play Store. No Ducking Way is your own open-source build — safe to install if you trust the GitHub repo.

### Option A — Install anyway (try first)

When you see **App blocked to protect your device**:

1. Tap **More details** or **Details** (wording varies).
2. Tap **Install anyway** or **Install without scanning**.

On some phones the button is hidden under **⋮** menu → **Install anyway**.

### Option B — Pause Play Protect briefly

1. Open **Google Play Store**.
2. Tap your **profile icon** (top right).
3. Tap **Play Protect**.
4. Tap **Settings** (gear).
5. Turn **OFF** “Scan apps with Play Protect” (or “Improve harmful app detection”).
6. Install the APK from Downloads.
7. Turn Play Protect **back ON** after install.

### Option C — Samsung phones

Samsung may show **Install blocked** twice (Play Protect + Samsung):

1. Follow Option A or B above.
2. If still blocked: **Settings → Security and privacy → Auto Blocker** → turn off temporarily, or allow unknown apps.
3. **Settings → Apps → ⋮ → Special access → Install unknown apps** → enable for **Chrome** or **My Files**.

### Option D — USB install from PC (best when still blocked)

**On phone:**

1. **Settings → About phone** → tap **Build number** 7 times.
2. **Settings → Developer options** → **USB debugging** ON.
3. **Samsung:** also **Install via USB** ON (same menu).
4. Plug USB cable → tap **Allow** on the debugging prompt.

**On PC** (project folder):

```powershell
.\scripts\install-via-usb.ps1
```

### Option E — Copy APK via USB, install with My Files

1. On PC, copy `NoDuckingWay-1.0.5.apk` to the phone **Download** folder (USB file transfer).
2. On phone: **My Files → Downloads** → tap APK (not Chrome).
3. Turn off **Auto Blocker** and Play Protect scanning temporarily (Option B/C).

---

## "App not installed"

1. **Uninstall** any old NoDucking / noducking app first.
2. Re-download — file must be **~10 MB**, not a few KB.
3. Free **100 MB** storage; need **Android 7.0+**.

---

## After install

- Add **Quick Settings** tile for one-tap toggle.
- Tap **Allow unrestricted battery use** on Samsung/Xiaomi if the service stops in background.
- See [TESTING.md](TESTING.md) for the full test guide.