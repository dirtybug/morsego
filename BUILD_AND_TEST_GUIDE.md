# MorseGO — Complete Build & Test Guide

> **Official Developer Guide for Building, Testing, and Packaging MorseGO on Windows, Linux, and macOS.**

This document provides complete instructions for setting up the required environment, running unit and behavior tests, building Debug and Release APKs, and understanding the automated CI/CD pipeline.

---

## 📋 Table of Contents

1. [Quick Reference Cheat Sheet](#-quick-reference-cheat-sheet)
2. [Workflow Options: Docker vs. Native](#-workflow-options-docker-vs-native)
3. [Method 1: Docker Workflow (Recommended)](#-method-1-docker-workflow-recommended)
   - [Required Software](#required-software-docker)
   - [Running Commands](#running-commands-docker)
4. [Method 2: Native Host Workflow (Without Docker)](#-method-2-native-host-workflow-without-docker)
   - [Required Software](#required-software-native)
   - [Environment Variables Setup](#environment-variables-setup)
   - [Running Commands with Gradle Wrapper](#running-commands-with-gradle-wrapper)
5. [Behavior & Instrumented Tests with Screenshots](#-behavior--instrumented-tests-with-screenshots)
6. [Generated Artifacts & Output Locations](#-generated-artifacts--output-locations)
7. [Automated Cloud CI/CD Release (GitHub Actions)](#-automated-cloud-cicd-release-github-actions)
8. [Troubleshooting & FAQ](#-troubleshooting--faq)

---

## ⚡ Quick Reference Cheat Sheet

| Action | Docker (Windows CMD) | Docker (PowerShell) | Native Host (`gradlew`) | Primary Output |
| :--- | :--- | :--- | :--- | :--- |
| **Unit Tests (79 tests)** | `run-docker-tests.bat unit` | `.\run-docker-tests.ps1 unit` | `.\gradlew.bat testDebugUnitTest` | `reports/unit-tests/index.html` |
| **Build Debug APK** | `run-docker-tests.bat build` | `.\run-docker-tests.ps1 build` | `.\gradlew.bat assembleDebug` | `build-apks/` or `app/build/outputs/apk/debug/` |
| **Build Release APK** | `run-docker-tests.bat release` | `.\run-docker-tests.ps1 release` | `.\gradlew.bat assembleRelease` | `build-apks/morseGO-release.apk` |
| **Instrumented Tests (49 tests)** | `run-docker-tests.bat instrumented` | `.\run-docker-tests.ps1 instrumented` | `.\gradlew.bat connectedDebugAndroidTest` | `reports/instrumented/index.html` |
| **Full Suite (Tests + Build)** | `run-docker-tests.bat all` | `.\run-docker-tests.ps1 all` | `.\gradlew.bat check assemble` | All reports + APKs |

---

## ⚖️ Workflow Options: Docker vs. Native

| Feature | Method 1: Docker (Containerized) | Method 2: Native Host (Direct) |
| :--- | :--- | :--- |
| **Host Pollution** | **Zero** (JDK, Android SDK, and build tools stay in container) | Requires installing JDK 17, Android SDK 34, and tools on host |
| **Reproducibility** | **100% identical** across all developer machines and CI | Depends on local PATH and SDK versions installed |
| **Setup Time** | ~5 minutes (Install Docker Desktop) | ~15-20 minutes (Install JDK, SDK, configure PATH) |
| **Speed** | Fast (cached Gradle daemon inside volume) | Native CPU speed |
| **Device Testing** | Connects via `host.docker.internal:5555` | Connects directly to local ADB / emulator |

---

## 🐳 Method 1: Docker Workflow (Recommended)

The container encapsulates **OpenJDK 17**, **Android SDK Command-line Tools 34**, **Android Build-Tools 34.0.0**, and **ADB**. You do not need Java or the Android SDK installed on your PC.

### Required Software (Docker)

1. **Docker Desktop for Windows**:
   - Download: [https://www.docker.com/products/docker-desktop/](https://www.docker.com/products/docker-desktop/)
   - Or install via Windows Package Manager (**winget** in PowerShell / CMD):
     ```cmd
     winget install Docker.DockerDesktop
     ```
2. **WSL 2 (Windows Subsystem for Linux)**:
   - Docker Desktop uses WSL 2 as its backend on Windows.
   - If not already installed, run PowerShell as Administrator:
     ```powershell
     wsl --install
     ```
   - Ensure hardware virtualization (**VT-x** on Intel or **AMD-V** on AMD) is enabled in your PC BIOS.

### Running Commands (Docker)

Open a terminal (CMD, PowerShell, or Git Bash) in the project root (`C:\Users\JúlioAndrade\morseGo`):

#### 1. Run Unit Tests (Fast, ~3-5 seconds)
Executes all 79 core business logic tests: binary tree traversal, PARIS standard timing, Farnsworth timing, dynamic word generator, and hardware keyer decoder.
```cmd
# Windows CMD
run-docker-tests.bat unit

# Windows PowerShell
.\run-docker-tests.ps1 unit

# Linux / macOS
./run-docker-tests.sh unit
```

#### 2. Build Debug and Test APKs
Compiles the application APK and the instrumented test runner APK:
```cmd
run-docker-tests.bat build
```
Outputs are copied directly to `./build-apks/`.

#### 3. Build Production Release APK (for Google Play Store)
Compiles the release APK with ProGuard/R8 optimizations:
```cmd
run-docker-tests.bat release
```
Outputs: `./build-apks/morseGO-release.apk`.

#### 4. Run Instrumented Behavior Tests (with Screenshots)
Connects to an Android emulator or physical device running on the host machine:
1. Start an Android emulator or plug in your Android phone with USB Debugging enabled.
2. In host terminal, enable TCP/IP ADB port:
   ```cmd
   adb tcpip 5555
   ```
3. Run the container:
   ```cmd
   run-docker-tests.bat instrumented
   ```
The container connects to `host.docker.internal:5555`, executes the 49 behavior tests, and saves device screenshots to `./screenshots/`.

#### 5. Run Full Suite (All Tests + Build)
```cmd
run-docker-tests.bat all
```

---

## 💻 Method 2: Native Host Workflow (Without Docker)

If you prefer building directly on Windows without Docker, you must install the Java Development Kit (JDK 17) and the Android SDK on your machine.

### Required Software (Native)

#### 1. Java Development Kit (JDK 17)
Android Gradle Plugin 8.3 requires **Java 17**.
- Recommended distribution: **Eclipse Temurin JDK 17 (LTS)**.
- Install via winget:
  ```powershell
  winget install EclipseAdoptium.Temurin.17.JDK
  ```
- Or download manually: [Adoptium Eclipse Temurin 17](https://adoptium.net/temurin/releases/?version=17)

#### 2. Android SDK Command-Line Tools & Platforms
- Download Android Studio or Android Command-line Tools: [Android Developer Studio](https://developer.android.com/studio)
- Required Android SDK Packages:
  - **SDK Platforms**: `platforms;android-34` (Android 14 UpsideDownCake)
  - **Build-Tools**: `build-tools;34.0.0`
  - **Platform-Tools**: `platform-tools` (contains `adb.exe`)

Install via `sdkmanager` (in PowerShell):
```powershell
sdkmanager "platforms;android-34" "build-tools;34.0.0" "platform-tools"
```

### Environment Variables Setup

Configure the following System Environment Variables in Windows:

1. **`JAVA_HOME`**:
   - Path: `C:\Program Files\Eclipse Adoptium\jdk-17.x.x-hotspot` (or your JDK 17 path)
2. **`ANDROID_HOME`** (or `ANDROID_SDK_ROOT`):
   - Path: `C:\Users\%USERNAME%\AppData\Local\Android\Sdk`
3. **`PATH`**:
   Add the following entries to your `Path`:
   - `%JAVA_HOME%\bin`
   - `%ANDROID_HOME%\platform-tools`
   - `%ANDROID_HOME%\cmdline-tools\latest\bin`

Verify the configuration in a new terminal:
```cmd
java -version
javac -version
adb version
```

### Running Commands with Gradle Wrapper

Once Java and Android SDK are configured, use the included Gradle Wrapper (`gradlew.bat` on Windows, `./gradlew` on Linux/macOS):

#### 1. Clean Build Directory
```cmd
.\gradlew.bat clean
```

#### 2. Run All Unit Tests
```cmd
.\gradlew.bat testDebugUnitTest --info
```
* HTML Report: `app\build\reports\tests\testDebugUnitTest\index.html`

#### 3. Build Debug APK
```cmd
.\gradlew.bat assembleDebug
```
* Output APK: `app\build\outputs\apk\debug\app-debug.apk`

#### 4. Build Release APK (Google Play Store)
```cmd
.\gradlew.bat assembleRelease
```
* Output APK: `app\build\outputs\apk\release\app-release-unsigned.apk`

#### 5. Build Android Test APK
```cmd
.\gradlew.bat assembleDebugAndroidTest
```
* Output APK: `app\build\outputs\apk\androidTest\debug\app-debug-androidTest.apk`

#### 6. Run Instrumented Tests on Connected Device / Emulator
```cmd
.\gradlew.bat connectedDebugAndroidTest --info
```
* HTML Report: `app\build\reports\androidTests\connected\index.html`

---

## 📱 Behavior & Instrumented Tests with Screenshots

MorseGO includes an automated UI behavior testing suite with **49 test scenarios** covering:
- **Binary Tree Navigation**: Step-by-step visual branch traversal (`E`, `T`, `I`, `A`, `N`, `M`, etc.).
- **Koch Method Learning Flow**: Level progression, dynamic locked/unlocked characters, and scoring.
- **Transmitter Word Exam**: Multi-letter live keying with real-time feedback for amateur radio words (`CQ`, `73`, `SOS`, `QSO`).
- **Hardware Integration**: Dual-paddle calibration, 3.5mm audio jack / USB-C OTG paddle detection, Left/Right Ctrl key emulation, and Farnsworth timing.

### Extracting Screenshots
When running instrumented tests, screenshots are captured on device storage at:
`/sdcard/Android/data/com.morsego.app/files/Pictures/behavior_screenshots/`

Pull them to your PC with:
```cmd
adb pull /sdcard/Android/data/com.morsego.app/files/Pictures/behavior_screenshots ./screenshots/
```

---

## 📁 Generated Artifacts & Output Locations

After running builds and tests, the following directory layout is produced:

```
morseGo/
├── build-apks/                      # Output APK binaries (via Docker)
│   ├── morseGO-debug.apk           # Debug testing APK
│   └── morseGO-release.apk         # Production Release APK
│
├── reports/                         # Test execution reports
│   ├── unit-tests/                 # 79 Unit tests report
│   │   ├── index.html              # Main HTML test dashboard
│   │   └── com.morsego.app.*.html  # Class-by-class results
│   ├── instrumented/               # 49 Instrumented tests report
│   │   └── index.html              # Connected device test results
│   └── lint/                       # Android lint code quality reports
│
├── screenshots/                     # Visual proof screenshots
│   ├── behavior_test_01.jpg        # Tree traversal screenshots
│   ├── behavior_test_48.jpg        # Word transmission success (CQ, 73)
│   └── behavior_test_49.jpg        # Timing failure test case
│
├── releases/                        # CI/CD Versioned release catalogs
│   └── v1.0.0/                     # Version folder
│       ├── morseGO-v1.0.0-release.apk
│       ├── morseGO-v1.0.0-debug.apk
│       ├── SHA256SUMS.txt
│       └── release-manifest.json
│
└── tests/                           # CI/CD Versioned test suites
    └── v1.0.0/
        ├── index.html              # Test report archive
        ├── test-summary.json       # Machine-readable metrics
        └── morseGO-test-suite-v1.0.0.zip
```

---

## 🚀 Automated Cloud CI/CD Release (GitHub Actions)

In production, **official releases are never compiled manually on local PCs**. They are built, tested, and signed in an isolated cloud environment via [`.github/workflows/ci.yml`](file:///C:/Users/JúlioAndrade/morseGo/.github/workflows/ci.yml).

### How to Trigger an Official Release:

1. Commit your changes and tag the release:
   ```bash
   git add .
   git commit -m "feat: prepare version v1.0.0"
   git tag v1.0.0
   git push origin master --tags
   ```

2. What GitHub Actions does automatically:
   1. **Unit Tests**: Runs all 79 unit tests on OpenJDK 17.
   2. **Android Emulator Matrix**: Boots a hardware-accelerated Pixel 6 emulator (API 30) and runs all 49 behavior tests, capturing live device screenshots.
   3. **Docker Build**: Compiles Release and Debug APKs inside the official Docker container.
   4. **Integrity Verification**: Generates `SHA256SUMS.txt` cryptographic hashes.
   5. **Cataloging**: Organizes `releases/v1.0.0/` and `tests/v1.0.0/` folders with manifests.
   6. **GitHub Release Publication**: Automatically publishes the release with downloadable APKs, checksums, and zipped test reports.

> **Security Gate**: If any unit test or behavior test fails, the CI pipeline **aborts immediately**, preventing broken APKs from being released.

---

## ❓ Troubleshooting & FAQ

### 1. `[ERRO] Docker nao encontrado no PATH`
- **Cause**: Docker Desktop is either not installed, or the Docker background service is not running.
- **Solution**:
  1. Open the Start Menu and launch **Docker Desktop**.
  2. Wait until the whale icon in the Windows taskbar shows "Docker Desktop is running".
  3. Re-run `run-docker-tests.bat`.

### 2. `ERROR: JAVA_HOME is not set and no 'java' command could be found in your PATH`
- **Cause**: Running `gradlew.bat` natively on Windows without Java 17 configured.
- **Solution**:
  1. Install JDK 17: `winget install EclipseAdoptium.Temurin.17.JDK`
  2. In Windows System Properties -> Environment Variables, set `JAVA_HOME` to `C:\Program Files\Eclipse Adoptium\jdk-17.x.x-hotspot`.
  3. Add `%JAVA_HOME%\bin` to `Path`.
  4. Restart your terminal.

### 3. `Could not find an installed version of the Android SDK`
- **Cause**: Running native `gradlew.bat` without `ANDROID_HOME` configured.
- **Solution**:
  1. Set `ANDROID_HOME` to `C:\Users\%USERNAME%\AppData\Local\Android\Sdk`.
  2. Or simply use `run-docker-tests.bat`, which bundles the Android SDK inside Docker.

### 4. `adb devices` shows no device or `offline`
- **Cause**: USB debugging prompt was not accepted on the phone, or ADB daemon needs a restart.
- **Solution**:
  ```cmd
  adb kill-server
  adb start-server
  adb devices
  ```
  Ensure "Always allow from this computer" is checked on the phone screen.

---

*MorseGO — Built with pure Java for high-performance, low-latency Morse code training.*
