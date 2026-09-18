# 📦 MorseGO - Official Releases

This document maintains the official release history of **MorseGO**, including Google Play Store metadata, release deliverables, test validation, and release notes.

Official release assets can also be directly downloaded from the [GitHub Releases](https://github.com/dirtybug/morsego/releases) page.

---

<!-- RELEASES_LIST_START -->
## [v1.0.1] - 2026-09-18

### 📱 Google Play Store Metadata
- **Version Name (`versionName`):** `1.0.1`
- **Version Code (`versionCode`):** `10001`
- **Application ID:** `com.morsego.app`
- **Target SDK:** Android 34 (Android 14) / **Min SDK:** Android 24 (Android 7.0)

### 🧪 Tests and Quality
- **Test Suite:** 100% Passed (Unit Tests & Behavior Tests)
- **Hermetic Build Environment:** Docker (OpenJDK 17 + Android SDK 34)

### 📥 Release Deliverables
| Artifact | File | Description |
| :--- | :--- | :--- |
| **AAB Bundle** | [`morseGO-v1.0.1.aab`](https://github.com/dirtybug/morsego/releases/tag/v1.0.1) | Signed production Android App Bundle for Google Play Store |
| **APK Release** | [`morseGO-v1.0.1.apk`](https://github.com/dirtybug/morsego/releases/tag/v1.0.1) | Signed production Android APK for direct device installation |
| **Test Reports** | [`morseGO-v1.0.1-test-reports.zip`](https://github.com/dirtybug/morsego/releases/tag/v1.0.1) | Full HTML test execution reports and summaries |
| **Screenshots** | [`morseGO-v1.0.1-screenshots.zip`](https://github.com/dirtybug/morsego/releases/tag/v1.0.1) | Visual UI behavior test screenshots across orientations |

### 🚀 Key Improvements in v1.0.1
- **Updated Default Timings:** Default 10 WPM, 720ms letter spacing (6 dits), 1560ms word spacing (13 dits).
- **Expanded Spacing Controls:** Letter spacing slider configurable from 2 to 12 dits; word spacing slider configurable from 5 to 20 dits.
- **Flattened Release Artifacts:** Clean, non-nested GitHub Actions artifact zips without redundant internal zips.
- **Documentation Overhaul:** English documentation with direct GitHub Releases links.

---

## [v1.0.0] - 2026-09-18

### 📱 Google Play Store Metadata
- **Version Name (`versionName`):** `1.0.0`
- **Version Code (`versionCode`):** `10000`
- **Application ID:** `com.morsego.app`
- **Target SDK:** Android 34 (Android 14) / **Min SDK:** Android 24 (Android 7.0)

### 🧪 Tests and Quality
- **Test Suite:** 100% Passed (Unit Tests & Behavior Tests)
- **Hermetic Build Environment:** Docker (OpenJDK 17 + Android SDK 34)

### 📥 Release Deliverables
| Artifact | File | Description |
| :--- | :--- | :--- |
| **AAB Bundle** | [`morseGO-v1.0.0.aab`](https://github.com/dirtybug/morsego/releases/tag/v1.0.0) | Signed production Android App Bundle for Google Play Store |
| **APK Release** | [`morseGO-v1.0.0.apk`](https://github.com/dirtybug/morsego/releases/tag/v1.0.0) | Signed production Android APK for direct device installation |
| **Test Reports** | [`morseGO-v1.0.0-test-reports.zip`](https://github.com/dirtybug/morsego/releases/tag/v1.0.0) | Full HTML test execution reports and summaries |
| **Screenshots** | [`morseGO-v1.0.0-screenshots.zip`](https://github.com/dirtybug/morsego/releases/tag/v1.0.0) | Visual UI behavior test screenshots across orientations |

### 🚀 Key Features
- **Complete Morse Binary Tree:** 21 progressive training levels (letters, numbers, prosigns).
- **Default Timing Configuration:** 10 WPM, 720ms inter-letter spacing (6 dits), 1560ms word spacing (13 dits).
- **Physical USB Type-C Paddle Keyer:** Native plug-and-play support for dual-paddle keyers (*CW Keyer Automatic Trainer PCB*).
- **Multiple Keyer Modes:** Iambic Mode B (Curtis with memory), Iambic Mode A, and Straight Key / Manual.
- **Adaptive Screen Layouts:** Responsive portrait and landscape views for phones and tablets.
- **100% Free & Open Source:** No ads, no tracking, completely open on GitHub.
<!-- RELEASES_LIST_END -->
