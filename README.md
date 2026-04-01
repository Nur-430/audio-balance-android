# 🎧 Audio Balance

Aplikasi Android untuk mengontrol keseimbangan audio kiri/kanan dengan cepat, lengkap dengan **Quick Settings Tile** di Control Center.

## ✨ Fitur

- 🎚️ Slider balance audio Kiri ↔ Kanan
- 🔁 Toggle ON/OFF (OFF = reset ke 50/50)
- ⚡ Quick Settings Tile di Control Center
- 🎯 Preset cepat (Kiri +20%, +30%, Tengah, dll)
- 💾 Simpan setting terakhir otomatis
- 🌙 Dark/Light mode otomatis (Material You)

## 📋 Persyaratan

- Android 8.0+ (API 26)
- Permission `WRITE_SECURE_SETTINGS` (via ADB atau Shizuku)

## 🚀 Cara Build

### Dengan Android Studio (PC)
1. Clone repo ini
2. Buka di Android Studio
3. Build & Run

### Cara Grant Permission (WAJIB)

**Opsi A — ADB dari PC:**
```bash
adb shell pm grant com.yourname.audiobalance android.permission.WRITE_SECURE_SETTINGS
```

**Opsi B — Shizuku (tanpa PC):**
1. Install [Shizuku](https://play.google.com/store/apps/details?id=moe.shizuku.privileged.api)
2. Aktifkan via Wireless Debugging (Android 11+)
3. Grant permission lewat Shizuku Manager

## 📁 Struktur File

```
AudioBalance/
├── app/
│   ├── src/main/
│   │   ├── AndroidManifest.xml
│   │   ├── java/com/yourname/audiobalance/
│   │   │   ├── MainActivity.kt          ← UI utama
│   │   │   ├── AudioBalanceTheme.kt     ← Material 3 Theme
│   │   │   └── BalanceTileService.kt    ← Quick Settings Tile
│   │   └── res/
│   │       ├── drawable/
│   │       │   ├── ic_balance.xml       ← Icon tile
│   │       │   └── ic_launcher.xml      ← Icon app
│   │       └── values/
│   │           ├── strings.xml
│   │           └── themes.xml
│   ├── build.gradle.kts
│   └── proguard-rules.pro
├── gradle/wrapper/
│   └── gradle-wrapper.properties
├── build.gradle.kts
├── settings.gradle.kts
├── gradle.properties
└── .gitignore
```

## 📱 Cara Pakai

1. Install APK
2. Grant permission (lihat di atas)
3. Buka app → atur slider balance
4. Toggle ON untuk aktifkan
5. Tambah tile di Control Center

## 🛠️ Tech Stack

- **Kotlin** + Jetpack Compose
- **Material 3** (Material You / Dynamic Color)
- Android Quick Settings Tile API
- SharedPreferences untuk penyimpanan
