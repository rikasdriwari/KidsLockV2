# KidLock

Aplikasi Android untuk membatasi penggunaan device anak dengan timer. Saat timer habis, layar overlay penuh muncul dan mengunci device — hanya bisa dibuka dengan PIN.

## Fitur
- Set PIN (disimpan ter-hash, bukan plaintext)
- Set durasi timer (menit)
- Timer berjalan di background (foreground service)
- Overlay lock full-screen saat waktu habis
- Timer/kunci tetap aktif setelah device restart
- Min Android 10 (API 29)

## Build APK via GitHub Actions (gratis, tanpa perlu Android Studio)
1. Buat repo baru di GitHub, push seluruh folder project ini ke branch `main`.
2. Buka tab **Actions** di repo → workflow "Build APK" akan otomatis jalan.
3. Setelah selesai (~3-5 menit), buka run yang sukses → bagian **Artifacts** → download `KidLock-debug-apk`.
4. Extract zip-nya, dapat file `app-debug.apk`.

Bisa juga trigger manual: tab Actions → pilih workflow "Build APK" → **Run workflow**.

## Install ke HP anak
1. Salin `app-debug.apk` ke HP (via kabel USB, Google Drive, dsb).
2. Aktifkan "Install dari sumber tidak dikenal" untuk file manager/browser yang dipakai.
3. Install APK.
4. Buka aplikasi KidLock, buat PIN, lalu izinkan permission:
   - **Tampil di atas aplikasi lain** (overlay) — akan diminta otomatis saat pertama kali Start Timer.
   - **Notifikasi** — diminta otomatis saat buka app (Android 13+).
5. Set durasi menit, tekan **Mulai Timer**.

## Cara kerja
- Timer berjalan sebagai foreground service dengan notifikasi menampilkan sisa waktu.
- Saat waktu habis, service menampilkan overlay full-screen (tidak bisa ditutup tanpa PIN benar).
- State timer/kunci tersimpan lokal, jadi kalau device restart saat sedang terkunci/timer jalan, otomatis lanjut (via BootReceiver).
- Untuk stop timer sebelum waktu habis, buka app → tombol **Hentikan Timer**.

## Keterbatasan (perlu diketahui)
- Ini bukan Device Admin / Device Owner lock, jadi anak dengan akses ke **Pengaturan → Aplikasi → Force Stop / Uninstall** secara teknis bisa menghentikan aplikasi. Untuk device yang benar-benar terkontrol penuh, disarankan kombinasikan dengan **restricted user profile** bawaan Android atau kunci akses ke Pengaturan.
- Overlay permission harus diberikan manual sekali di awal (kebijakan keamanan Android, tidak bisa di-skip otomatis).

## Struktur project
```
kidlock/
├── app/
│   ├── build.gradle.kts
│   └── src/main/
│       ├── AndroidManifest.xml
│       ├── java/com/kasbro/kidlock/
│       │   ├── MainActivity.kt
│       │   ├── LockService.kt
│       │   ├── BootReceiver.kt
│       │   ├── PinUtils.kt
│       │   └── LockPrefs.kt
│       └── res/
├── build.gradle.kts
├── settings.gradle.kts
└── .github/workflows/build-apk.yml
```
## SUPPORT our Product
'''
SAWERIA https://saweria.co/kasbro
