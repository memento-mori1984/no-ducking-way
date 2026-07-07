# Install NoDuckingWay on a phone connected via USB (bypasses Play Protect UI block).
param(
    [string]$ApkPath = ""
)

$ErrorActionPreference = "Stop"
$root = Split-Path $PSScriptRoot -Parent

if (-not $ApkPath) {
    $ApkPath = Join-Path $root "NoDuckingWay-1.0.5.apk"
    if (-not (Test-Path $ApkPath)) {
        $ApkPath = Join-Path $root "app\build\outputs\apk\release\app-release.apk"
    }
}

if (-not (Test-Path $ApkPath)) {
    Write-Error "APK not found. Build first: .\gradlew.bat assembleRelease"
}

$adb = Join-Path $env:LOCALAPPDATA "Android\Sdk\platform-tools\adb.exe"
if (-not (Test-Path $adb)) {
    $adb = "C:\Users\Ranzh\AppData\Local\Android\Sdk\platform-tools\adb.exe"
}
if (-not (Test-Path $adb)) {
    Write-Error "adb not found. Install Android SDK platform-tools or open Android Studio once."
}

Write-Host "=== NoDuckingWay USB Install ===" -ForegroundColor Cyan
Write-Host "APK: $ApkPath" -ForegroundColor Gray

& $adb kill-server 2>$null | Out-Null
Start-Sleep -Milliseconds 800
& $adb start-server | Out-Null

$devices = & $adb devices | Select-Object -Skip 1 | Where-Object { $_ -match "device$" }
if (-not $devices) {
    Write-Host ""
    Write-Host "No phone detected. On your phone:" -ForegroundColor Yellow
    Write-Host "  1. Settings -> About phone -> tap Build number 7 times"
    Write-Host "  2. Settings -> Developer options -> USB debugging ON"
    Write-Host "  3. Samsung: also turn ON 'Install via USB' in Developer options"
    Write-Host "  4. Plug in USB cable -> tap Allow on the phone"
    Write-Host ""
    Write-Host "Then run this script again." -ForegroundColor Yellow
    exit 1
}

Write-Host "Device found. Installing..." -ForegroundColor Green
& $adb install -r $ApkPath
if ($LASTEXITCODE -eq 0) {
    Write-Host "Installed successfully. Open NoDuckingWay on your phone." -ForegroundColor Green
} else {
    Write-Host "Install failed. Try: unlock phone, accept USB debugging, use a data-capable USB cable." -ForegroundColor Red
    exit $LASTEXITCODE
}