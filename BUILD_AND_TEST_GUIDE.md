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
| **Interactive Dev Shell** | `run-docker-dev.bat` | `.\run-docker-dev.ps1` | `bash` / terminal | Interactive shell with Gradle daemon |
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

#### 6. Interactive Development Container (Dev Shell)
Opens an interactive bash shell inside the Docker container with JDK 17, Android SDK 34, fast Gradle daemon, and your workspace mounted:
```cmd
# Windows CMD
run-docker-dev.bat

# Windows PowerShell
.\run-docker-dev.ps1

# Linux / macOS
./run-docker-dev.sh
```

Inside the interactive container shell, you have access to convenient shortcuts:
- `test` or `./gradlew test` — Run all unit tests.
- `./gradlew test --continuous` — Continuous testing mode: automatically reruns tests every time you save a file on your host machine!
- `build` or `./gradlew assembleDebug` — Build the debug APK.
- `release` or `./gradlew assembleRelease` — Build the release APK.
- `lint` or `./gradlew lint` — Run the Android Lint analyzer.
- `exit` — Exit the container shell.

#### 7. Run Android Studio IDE with Full GUI in Docker (Web noVNC & VNC)
Launches the complete official **Android Studio IDE with graphical user interface** directly inside Docker, pre-loading the `morseGo` project and serving it via your web browser (no local X server required!):
```cmd
# Windows CMD (launches container and opens browser automatically)
run-docker-android-studio.bat

# Windows PowerShell
.\run-docker-android-studio.ps1

# Linux / macOS
./run-docker-android-studio.sh
```

**Access Points:**
* **Web Browser GUI (noVNC):** `http://localhost:6080/vnc.html?autoconnect=true&resize=remote`
* **Native VNC Client:** `localhost:5900` (no password)
* **Automatic Project Load:** `/workspace` (`morseGo`) is pre-loaded on startup with Android SDK 34 pre-configured.
* **Persistent Settings:** Caches Android Studio configurations and Gradle dependencies in Docker volumes (`studio-config`, `studio-share`, `gradle-cache`).

**Helper Commands:**
* `run-docker-android-studio.bat stop` — Stop the Android Studio container.
* `run-docker-android-studio.bat logs` — View live container logs.
* `run-docker-android-studio.bat restart` — Restart the container and reopen the browser.


#### 8. Visual Studio Code Dev Containers & Tasks (Launch Android Studio)
You can launch Android Studio directly inside VS Code or via Dev Containers:
1. **Dev Containers (Auto-Launch Android Studio)**:
   - Ensure the **Dev Containers** extension (`ms-vscode-remote.remote-containers`) is installed in VS Code.
   - Open the `morseGo` folder in VS Code.
   - Press `Ctrl+Shift+P` / `F1` and select `Dev Containers: Reopen in Container`.
   - VS Code starts the `android-studio` Docker container, maps ports 6080 and 5900, automatically opens the browser at `http://localhost:6080` displaying Android Studio, and hooks up the container workspace.
2. **VS Code Tasks**:
   - Press `Ctrl+Shift+B` (Default Build Task) or `Ctrl+Shift+P` -> `Tasks: Run Task`:
     - **`Launch Android Studio in Docker`**: Starts the container and opens the browser.
     - **`Open Android Studio Web GUI (Browser)`**: Directly opens `http://localhost:6080/vnc.html?autoconnect=true&resize=remote`.
     - **`Run Unit Tests (Docker)`**: Runs the unit test suite inside Docker.
     - **`Build Release APK (Docker)`**: Assembles and signs the release APK.

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
└── release/                         # Single consolidated release directory
    └── v1.0.0/                      # Version folder
        ├── morseGO-v1.0.0-release.apk   # Production Release APK (for Google Play Store)
        ├── morseGO-v1.0.0-debug.apk     # Debug testing APK
        ├── reports/                     # Test execution reports
        │   ├── index.html               # Main HTML test dashboard
        │   ├── behavior-tests/          # 49 Behavior tests & screenshots gallery
        │   ├── unit-tests/              # 86 Unit tests report (100% pass)
        │   └── instrumented/            # 49 Instrumented tests report
        ├── screenshots/                 # Visual proof screenshots (89 files: 23 landscape + 66 portrait)
        │   ├── phone_rotation_01_portrait_0_deg.jpg       # 0° Portrait Baseline
        │   ├── phone_rotation_02_rotated_90_deg_tree.jpg   # 90° Rotated Landscape Tree (860×412)
        │   ├── phone_rotation_03_rotated_90_deg_keyer.jpg  # 90° Rotated Landscape Keyer (860×412)
        │   ├── phone_rotation_04_rotated_90_deg_exam.jpg   # 90° Rotated Landscape Exam (860×412)
        │   ├── phone_rotation_05_rotated_90_deg_hardware.jpg # 90° Rotated Landscape Hardware (860×412)
        │   ├── phone_rotation_06_restored_portrait_0_deg.jpg # 0° Restored Portrait
        │   ├── behavior_test_01.jpg     # Tree traversal screenshots (Landscape 860×412)
        │   ├── behavior_test_48.jpg     # Word transmission success (CQ, 73)
        │   └── behavior_test_49.jpg     # Timing failure test case (Landscape 860×412)
        ├── SHA256SUMS.txt               # Cryptographic hashes
        ├── release-manifest.json        # Machine-readable release & Store metadata
        └── test-summary.json            # Machine-readable test metrics
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
   1. **Unit Tests**: Runs all 86 unit tests on OpenJDK 17 (including 90° phone rotation invariance tests).

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

### 5. `App not installed as package appears to be invalid` (RESOLVED)
- **Root Causes**:
  1. **Unsigned Release APK** (`INSTALL_PARSE_FAILED_NO_CERTIFICATES`): Prior release builds had no `signingConfig` defined in `app/build.gradle`, producing an unsigned APK which Android Package Installer rejects automatically.
  2. **Malformed Intent Filter** (`INSTALL_PARSE_FAILED_MANIFEST_MALFORMED`): `AndroidManifest.xml` had `<action android:name="android.hardware.usb.action.USB_DEVICE_ATTACHED" />` under `<activity>` without the mandatory `<meta-data>` resource.
- **Fix Applied**:
  - Generated dedicated release keystore (`release/release.keystore`, gitignored).
  - Added `signingConfigs.release` in `app/build.gradle` with both v1 (JAR signing) and v2 (APK Signature Scheme) enabled.
  - Removed the unnecessary USB intent-filter from `AndroidManifest.xml` (physical CW keyers connect as standard HID OTG keyboard devices).
  - Both `morseGO-v1.0.0-release.apk` and `morseGO-v1.0.0-debug.apk` are fully signed and validated with `apksigner`.
  - Added automated signed AAB build scripts (`release/build-signed-aab.bat`, `.ps1`, `.sh`) storing release bundles into `release/v<VERSION>/`.
  - Keys and passwords (`release/release.keystore`, `keystore-pass.txt`) are excluded via `.gitignore`.

---

*MorseGO — Built with pure Java for high-performance, low-latency Morse code training.*
