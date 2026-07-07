# Install on your phone

Use release **v1.0.1** or newer:  
https://github.com/memento-mori1984/no-ducking-way/releases/latest

## Steps

1. On your phone, open the release link in **Chrome** (not in-app GitHub browsers if possible).
2. Expand **Assets**.
3. Tap **NoDuckingWay-1.0.1.apk** and wait for the full download (~19 MB).
4. Open **Downloads** → tap the APK → **Install**.
5. If prompted, allow **Install unknown apps** for Chrome or **Files**.
6. Open **NoDuckingWay** → allow **Notifications** → turn protection **ON**.

## If you see "App not installed"

Try these in order:

### 1. Remove any old copy

**Settings → Apps** → search **NoDucking** or **noducking** → **Uninstall** if present (including failed/partial installs).

### 2. Re-download (file often corrupt on mobile)

- Do **not** open the APK from a preview; use **Assets → download**.
- Confirm file size is about **19 MB**, not a few KB.
- Or download on your PC, copy to the phone via USB/Google Drive, then install from **Files**.

### 3. Play Protect

When installing, if Google warns you → **Install anyway** / **More details** → **Install anyway**.

### 4. Storage & Android version

- Free at least **100 MB** storage.
- Requires **Android 7.0+** (released ~2016). Older phones cannot install.

### 5. Still failing?

Install via USB from a PC (most reliable):

```powershell
& "$env:LOCALAPPDATA\Android\Sdk\platform-tools\adb.exe" install -r NoDuckingWay-1.0.1.apk
```

Enable **USB debugging** on the phone first.