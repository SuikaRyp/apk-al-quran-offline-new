<p align="center">
  <img src="app/src/main/res/drawable/ic_launcher_playstore.png" width="108" height="108" alt="Al-Qur'an Offline Logo" style="border-radius: 22px;">
</p>

<h1 align="center">Al-Qur'an Offline</h1>

<p align="center">
  Aplikasi Android Al-Qur'an, Hadits, Bacaan Sholat, dan Doa Harian yang dirancang murni <i>offline-first</i>.<br>
  Fokus pada ketenangan tilawah, tipografi Rasm Utsmani presisi, antarmuka modern Material 3, dan privasi penuh tanpa akses internet.
</p>

<p align="center">
  <a href="https://github.com/KyuuX444/alquran-offline/releases"><img src="https://img.shields.io/badge/Download-APK%20v1.0-0F6E4D?style=flat-square&logo=android&logoColor=white" alt="Download APK"></a>
  <a href="https://github.com/KyuuX444/alquran-offline"><img src="https://img.shields.io/badge/GitHub-Repository-24292e?style=flat-square&logo=github&logoColor=white" alt="GitHub Repo"></a>
  <a href="https://github.com/KyuuX444/alquran-offline/actions"><img src="https://img.shields.io/badge/Build-Passing-34A853?style=flat-square&logo=githubactions&logoColor=white" alt="Build Status"></a>
  <img src="https://img.shields.io/badge/Android-5.0+_(API_21--34)-3DDC84?style=flat-square&logo=android&logoColor=white" alt="Android Support">
  <img src="https://img.shields.io/badge/Kotlin-1.9.22-7F52FF?style=flat-square&logo=kotlin&logoColor=white" alt="Kotlin">
  <img src="https://img.shields.io/badge/Compose-Material_3-4285F4?style=flat-square&logo=jetpackcompose&logoColor=white" alt="Jetpack Compose">
  <a href="LICENSE"><img src="https://img.shields.io/badge/License-MIT-2E7D32.svg?style=flat-square" alt="License: MIT"></a>
</p>

---

## Deskripsi

**Al-Qur'an Offline** adalah aplikasi Android open-source yang memuat seluruh mushaf Al-Qur'an (114 Surah, 30 Juz, 6.236 Ayat), perpustakaan 38.100+ Hadits Kutubut Tis'ah dan Arba'in Nawawi, panduan 15 bacaan sholat fardhu sahih, serta 32 doa harian bersumber dari Al-Qur'an dan As-Sunnah secara mandiri di dalam perangkat.

Aplikasi ini tidak memiliki izin akses jaringan (`android.permission.INTERNET`). Tidak ada server pengumpul data, tidak ada pelacakan analitik, dan tidak ada sistem login. Seluruh data teks, terjemahan, penanda baca (bookmark), dan preferensi tersimpan di penyimpanan lokal perangkat.

---

## Fitur Utama

- **Al-Qur'an 30 Juz Lengkap**  
  114 Surah dan 6.236 Ayat dengan teks Rasm Utsmani berstandar internasional dan terjemahan bahasa Indonesia resmi Kementerian Agama RI.

- **Koleksi Hadits Lengkap (38.100+ Hadits)**  
  Kutubut Tis'ah (Shahih Bukhari, Shahih Muslim, Sunan Abu Daud, Sunan At-Tirmidzi, Sunan An-Nasa'i, Sunan Ibnu Majah, Muwatha' Malik, Sunan Ad-Darimi, Musnad Ahmad) dan Arba'in Nawawi dengan filter pemilih kitab, teks Arab berharakat, dan navigasi detail.

- **Panduan Sahih Bacaan Sholat**  
  15 rangkaian bacaan sholat dari Niat 5 waktu, Takbiratul Ihram, Doa Iftitah, Ruku', I'tidal, Sujud, Duduk di Antara Dua Sujud, Tasyahud Awal, Tasyahud Akhir & Shalawat Ibrahimiyah, Salam, hingga Dzikir Ba'da Sholat lengkap dengan dalil hadits shahih.

- **Kumpulan Doa Harian**  
  32 doa harian untuk berbagai aktivitas (sebelum & bangun tidur, sebelum & sesudah makan, masuk & keluar rumah, masuk & keluar masjid, wudhu, safar, menuntut ilmu, rezeki berkah, kedua orang tua, doa sapu jagat, sayyidul istighfar, ketenangan, musibah, hujan, petir, dll) dengan teks Arab, transliterasi latin, arti, dan rujukan periwayatan.

- **Pencarian Cepat Offline**  
  Mencari surah, ayat, hadits, dan doa secara instan tanpa koneksi internet melalui indexing database lokal SQLite.

- **Penanda Baca (Bookmark) & Lanjut Baca (Last Read)**  
  Simpan ayat atau hadits penting dan lanjutkan tilawah secara otomatis dari posisi terakhir.

- **Antarmuka Modern & Mode Gelap**  
  Tema Material 3 dengan palet warna Islamic Emerald dan Warm Gold, kenyamanan tipografi Arab, serta dukungan tema Terang dan Gelap.

- **100% Offline First**  
  Nol koneksi internet, tanpa API eksternal, hemat daya, dan menjaga privasi pengguna sepenuhnya.

---

## Arsitektur Offline First

```
                     Koneksi Internet
                            |
                            v
                      [ TIDAK ADA ]
                            |
   +------------------------+------------------------+
   |                   PERANGKAT ANDROID             |
   |                                                 |
   |  Al-Qur'an 30 Juz (Room DB / SQLite) --------+  |
   |  Koleksi Hadits 38.100+ (Room DB) -----------+  |
   |  Panduan Sholat & Doa (Local Assets) --------+  |
   |  Riwayat Baca & Pengaturan (DataStore) ------+  |
   |  Bookmark (Room DB) -------------------------+  |
   |                                                 v
   |                                      Jetpack Compose UI
   +-------------------------------------------------+
```

---

## Sumber Data & Atribusi

1. **Teks Al-Qur'an (Rasm Utsmani)**  
   Bersumber dari [Tanzil Project](https://tanzil.net), terverifikasi dengan standar mushaf Utsmani internasional.  
   *Lisensi:* [Creative Commons Attribution 3.0 Unported (CC BY 3.0)](https://creativecommons.org/licenses/by/3.0/).

2. **Terjemahan Bahasa Indonesia**  
   Kementerian Agama Republik Indonesia (Kemenag RI).

3. **Koleksi Hadits**  
   38.144 Hadits dari 10 Kitab Induk (Kutubut Tis'ah dan Arba'in An-Nawawi) dengan matan Arab dan terjemahan bahasa Indonesia.

4. **Bacaan Sholat**  
   Kitab Shifat Shalat Nabi shallallahu 'alaihi wasallam dan rujukan hadits shahih (Shahih Bukhari, Shahih Muslim, Sunan Abu Daud, Sunan At-Tirmidzi, Sunan An-Nasa'i, Sunan Ibnu Majah).

5. **Doa Harian**  
   Kitab Hisnul Muslim (Kumpulan Doa dari Al-Qur'an dan As-Sunnah Ash-Shahihah) karya Syaikh Sa'id bin Ali bin Wahf Al-Qahthani dan Kitab Al-Adzkar karya Imam An-Nawawi.

---

## Spesifikasi Teknologi

- **Bahasa:** Kotlin 1.9.22 (Coroutines, StateFlow)
- **UI Toolkit:** Jetpack Compose + Material Design 3
- **Database:** Room Persistence Library di atas SQLite berindeks
- **Preferences:** AndroidX DataStore Preferences
- **Compatibility:** Android 5.0 (API 21) sampai Android 14 (API 34)
- **Build System:** Gradle Kotlin DSL dengan R8 minification
- **CI/CD:** GitHub Actions (Validasi dataset, unit tests, APK signing v1+v2+v3)

---

## Instalasi APK

1. Buka halaman [Releases](https://github.com/KyuuX444/alquran-offline/releases).
2. Unduh berkas `alquran-offline-v1.apk`.
3. Pasang berkas APK pada perangkat Android Anda.

### Verifikasi Checksum SHA-256

```bash
sha256sum -c alquran-offline-v1.apk.sha256
```

---

## Kompilasi dari Source Code

```bash
# 1. Clone repository
git clone https://github.com/KyuuX444/alquran-offline.git
cd alquran-offline

# 2. Verifikasi dataset
python3 scripts/validate_dataset.py
python3 scripts/validate_hadith_dataset.py
python3 scripts/validate_prayer_dataset.py
python3 scripts/validate_daily_prayer_dataset.py

# 3. Jalankan unit test
./gradlew testDebugUnitTest

# 4. Bangun APK
./gradlew assembleDebug
```

---

## Lisensi & Provenance

- **Kode Sumber:** [MIT License](LICENSE).
- **Teks Agama:** Al-Qur'an, Hadits, Bacaan Sholat, dan Doa disajikan untuk literasi umat dan dakwah Islam.
- **Project Provenance ID:** `quran-offline-kyuu-2026-c9f2a87b`
- **Pengembang:** **Kyuryn (S.D.Y)** ([@KyuuX444](https://github.com/KyuuX444))

---

<p align="center">
  <sub>Al-Qur'an Offline · Open Source · 100% Offline First · © 2026 Kyuryn</sub>
</p>

## Upgrade tambahan (Suika build)

- Menambahkan halaman **Amalan & Tasbih** yang bisa dibuka dari menu Beranda.
- Tasbih digital dengan target 33, 99, atau 100; tombol tambah/kurangi/reset; penghitung tersimpan lokal di perangkat.
- Checklist amalan harian dengan progres dan penyimpanan lokal per tanggal.
- Fitur utama yang sudah ada (Al-Qur'an offline, pencarian, bookmark, hadits, doa, bacaan sholat, Asmaul Husna, tema, pengaturan notifikasi dan waktu sholat) tetap dipertahankan.

Catatan: fitur pengingat amalan bukan fatwa atau penilaian ibadah. Pastikan rujukan agama diperiksa dari sumber tepercaya.

## Update besar - fitur Amalan

Pembaruan source menambahkan peningkatan di halaman Amalan:
- Pilihan label dzikir: Subhanallah, Alhamdulillah, Allahu Akbar, dan Istighfar; pilihan tersimpan lokal.
- Ringkasan konsistensi amalan beruntun dan total centang selama 7 hari terakhir.
- Ekspor/bagikan ringkasan amalan, progres khatam, target harian, hafalan, murajaah, puasa, kebaikan, catatan kajian, dan jurnal syukur melalui menu berbagi Android.
- Ringkasan statistik dijelaskan sebagai catatan kebiasaan pribadi, bukan penilaian kesalehan.

Build APK belum diverifikasi pada lingkungan ini karena Gradle Wrapper perlu mengunduh distribusi Gradle dan koneksi jaringan tidak tersedia.


## Suika update v1.2 (local tools)

- Islamic Tools screen available from Settings: sensor compass heading, Qibla bearing calculated from last-known device location, Hijri date (Android 7+), and a list of commonly observed Islamic dates. Hijri dates can differ by local moon-sighting decisions.
- Offline audio player lets the user choose an audio file already on-device through Android's document picker, then play/pause, stop/seek and view playback progress. No murottal audio is bundled in this source.
- Existing Backup & Restore screen remains available from Settings.
- Existing prayer-time location and notification preferences remain in Settings.
- Version bumped to 1.2 (versionCode 3).

Build verification note: `./gradlew assembleDebug --offline` could not start because the Gradle 8.5 distribution is not cached and network access to services.gradle.org was unavailable.


## V11 build fixes and accessibility
- Fixed `PaddingValues` named arguments in `MediaLibraryScreen.kt` (`bottom` compile error).
- Added a speaker action to each Quran ayah to read the Indonesian translation with Android TextToSpeech. This is synthetic speech for the translation, not a tajwid-accurate Quran recitation.
- Added a link to a CC0 child-praying reference photo on Wikimedia Commons. The link is an image reference, not an animated asset.
- Version bumped to 1.7 (versionCode 8).


### Audio murottal V1.8
Tombol speaker pada ayat kini memutar rekaman murattal Al-Husary (Hafs) per ayat dari EveryAyah, bukan suara Text-to-Speech. Streaming memerlukan koneksi internet; teks Al-Qur'an dan fitur lokal tetap tersedia offline. Audio hosted pihak ketiga dapat berubah atau tidak tersedia.


## Murottal offline (v1.9)

- Reciter: Mahmoud Khalil Al-Husary, murattal 128 kbps, Hafs an Asim, per ayah.
- Source audio: EveryAyah (`https://everyayah.com/data/Husary_128kbps/`), using the documented `SSSAAA.mp3` naming convention. The app downloads audio into its private internal storage; it does not bundle third-party recordings into this source archive.
- In the reader, choose **Unduh semua murottal agar offline** and confirm the download. It downloads 6,236 ayah recordings and can require around 1 GB or more, a stable connection, and considerable time. Existing valid files are skipped so a failed download can be resumed by pressing download again.
- After completion, the speaker button plays local files and does not need internet. Before downloading, a tapped ayah is streamed from EveryAyah.
- Audio source and metadata reference: https://github.com/Mushaf-Learning/quran-audio . The GitHub project's MIT license applies to its metadata/scripts, not automatically to the hosted audio; verify the audio provider's terms before redistributing recordings.

## V14 — pilihan qari dan kompas kiblat

- Pilihan qari: Mahmoud Khalil Al-Husary, Mishary Rashid Alafasy, Abdul Basit Abdus-Samad, Abdurrahman As-Sudais, dan Mohamed Siddiq Al-Minshawi. Rekaman audio diambil dari direktori EveryAyah dan setiap qari memiliki cache offline terpisah.
- Foto profil qari dimuat dari URL sumber yang dicantumkan di kode dan disimpan di penyimpanan privat aplikasi setelah berhasil diambil. Jika foto gagal dimuat atau belum pernah di-cache, aplikasi menampilkan avatar inisial. Foto dari pihak ketiga tetap tunduk pada lisensi/hak pemilik masing-masing.
- Kompas kiblat menampilkan arah relatif pada dial, menggunakan sensor rotation-vector dan koreksi deklinasi magnetik jika lokasi tersedia. Kalibrasi sensor dan kualitas perangkat tetap memengaruhi akurasi.
- Sumber referensi implementasi: `https://github.com/Bsracc/daily-quran` (MIT, multiple reciters/cache); `https://github.com/hassaanjamil/hj-qibla-compass` (Apache-2.0/MIT, Compose compass); `https://github.com/katsugtgz/shollu-android` (MIT, Qibla calculator/sensor fusion). Referensi ini dipelajari untuk pendekatan, bukan disalin sebagai modul biner.
