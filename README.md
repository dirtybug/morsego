# MorseGO 📻

**MorseGO** is a pure **JAVA** Android application designed to learn, practice, and master Morse Code (CW) telegraphy through the **Morse Binary Tree**, featuring native plug-and-play support for physical USB Type-C and 3.5mm dual-paddle keyers:
> **CW Keyer Automatic Trainer PCB 4 ND Magnet Bases Double Paddle** ([View on Amazon](https://www.amazon.es/-/pt/gp/product/B0F666MVG6/ref=ox_sc_act_title_1?smid=A2N32YIZWO8AUG&psc=1))

---

## 🌳 Learning via the Morse Binary Tree

Learning strictly follows the hierarchical structure of the **Morse Binary Tree**:
* Moving **Left** in the tree corresponds to a **DIT / Dot (`.`)**.
* Moving **Right** in the tree corresponds to a **DAH / Dash (`-`)**.

### Progressive Level System:
Each level **introduces exactly 2 new characters** traversing deeper into the tree and **includes all previous characters** in the practice pool:

* **Level 1 (Tree Root):**
  * New: **E** (`.`) and **T** (`-`)
  * Pool: `[E, T]`
* **Level 2 (E Branch):**
  * New: **I** (`..`) and **A** (`.-`)
  * Pool: `[E, T, I, A]`
* **Level 3 (T Branch):**
  * New: **N** (`-.`) and **M** (`--`)
  * Pool: `[E, T, I, A, N, M]` *(All depth 1 and 2 characters complete!)*
* **Level 4 (I Sub-branch):**
  * New: **S** (`...`) and **U** (`..-`)
  * Pool: `[E, T, I, A, N, M, S, U]`
* **Level 5 (A Sub-branch):**
  * New: **R** (`.-.`) and **W** (`.--`)
  * Pool: `[E, T, I, A, N, M, S, U, R, W]`
* **Level 6 (N Sub-branch):**
  * New: **D** (`-..`) and **K** (`-.-`)
  * Pool: `[E, T, I, A, N, M, S, U, R, W, D, K]`
* **Level 7 (M Sub-branch):**
  * New: **G** (`--.`) and **O** (`---`)
  * Pool: `[E, T, I, A, N, M, S, U, R, W, D, K, G, O]` *(All depth 3 characters complete!)*
* **Levels 8 to 13:** Add depth 4 character pairs: **H** & **V**, **F** & **L**, **P** & **J**, **B** & **X**, **C** & **Y**, **Z** & **Q** (completing the entire 26-letter alphabet).
* **Levels 14 to 18:** Add 5-element numbers in pairs: **5** & **4**, **3** & **2**, **1** & **6**, **7** & **8**, **9** & **0**.
* **Levels 19 to 21:** Add punctuation and prosigns: **.** & **,**, **?** & **/**, **SOS** & **AR**.

---

## 🔌 Hardware USB Keyer Compatibility

Compatible with USB Type-C and 3.5mm dual-paddle keyers with 4 Nd-magnet bases:
1. **Mode 2 (Recommended - Blinking LED / VBand mode):**
   * Emulates a USB HID keyboard, sending `Left Ctrl` (DIT paddle) and `Right Ctrl` (DAH paddle).
   * MorseGO intercepts these events directly in `MainActivity.dispatchKeyEvent()`.
2. **Mode 1 (Solid LED / Mouse Emulation):**
   * Emulates mouse clicks (Primary & Secondary clicks), captured in `dispatchGenericMotionEvent()`.
3. **One-Touch Calibration:**
   * In the **USB Keyer** tab, tap *"Calibrate Dit"* or *"Calibrate Dah"* and press the physical paddle to map any key code.
4. **Paddle Reversal:**
   * Switch between right-handed and left-handed operation with a single toggle.

---

## 💻 100% Pure Java Architecture

The project is built entirely in **Java** using the official Android SDK and Material Components:
* **Interactive UI View:** [`MorseTreeView.java`](app/src/main/java/com/morsego/app/ui/view/MorseTreeView.java) renders the interactive 2D binary tree with smooth pan and zoom.
* **Audio Engine:** [`MorseAudioSynthesizer.java`](app/src/main/java/com/morsego/app/audio/MorseAudioSynthesizer.java) produces pure PCM sine-wave tones via `AudioTrack` without clicks or pop noise.
* **Dual-Paddle Engine:** [`IambicKeyerEngine.java`](app/src/main/java/com/morsego/app/keyer/IambicKeyerEngine.java) supports Iambic Mode B (Curtis with memory), Iambic Mode A, and Straight Key / Manual mode.
* **Real-time Decoder:** [`MorseDecoder.java`](app/src/main/java/com/morsego/app/keyer/MorseDecoder.java) translates paddle cadence into text in real time.
* **Timing & Spacing:** [`MorseTiming.java`](app/src/main/java/com/morsego/app/keyer/MorseTiming.java) calculates precise PARIS element, letter, and word durations.
  * **Default Speed:** 10 WPM (dit unit = 120 ms).
  * **Default Letter Spacing:** 6 dits = 720 ms.
  * **Default Word Spacing:** 13 dits = 1560 ms (1.560s).

---

## 📱 Running in Android Studio

1. Open the project folder in **Android Studio**.
2. Allow Gradle to sync `build.gradle` and `settings.gradle`.
3. Connect an Android device (via USB with OTG or WiFi debugging) or launch an emulator.
4. Click **Run ▶**.
5. Connect your USB Type-C paddle keyer and start exploring the Morse Binary Tree!

---

## 🐳 Running Tests in Docker Containers

> 📖 **Complete Build & Test Guide:** For full prerequisites, commands, and troubleshooting details, see [`BUILD_AND_TEST_GUIDE.md`](BUILD_AND_TEST_GUIDE.md).

The project includes containerized build and test scripts to guarantee 100% reproducible environments (OpenJDK 17 + Android SDK 34):

### 0. Interactive Development Container (Dev Shell):
```cmd
# Windows CMD
run-docker-dev.bat

# Windows PowerShell
.\run-docker-dev.ps1

# Linux / macOS
./run-docker-dev.sh
```

### 1. Unit Tests (~3 seconds):
```cmd
run-docker-tests.bat unit
```
Or with Docker Compose:
```bash
docker compose run --rm test-unit
```
Executes all core logic tests:
* [`MorseTimingTest.java`](app/src/test/java/com/morsego/app/keyer/MorseTimingTest.java): Validates PARIS calculations, 10 WPM defaults (720ms letter / 1560ms word), and cadence bounds.
* [`MorseBinaryTreeTest.java`](app/src/test/java/com/morsego/app/tree/MorseBinaryTreeTest.java): Validates all 21 levels, nodes, and tree traversal.
* [`MorseWordGeneratorTest.java`](app/src/test/java/com/morsego/app/tree/MorseWordGeneratorTest.java): Validates randomized word generation, unlocked letter pools, and penalty systems.

### 2. Building APKs & AAB Bundles:
```cmd
# Debug APK
run-docker-tests.bat build

# Production Release APK
run-docker-tests.bat release

# Full Suite (Tests + APK + AAB)
run-docker-tests.bat all
```

---

## 📦 Official Releases

All production releases are built, tested, and published automatically via GitHub Actions:

Official downloadable artifacts (signed `.aab` bundles for Google Play Store, `.apk` installers, test reports, and screenshot archives) are available directly on the [**GitHub Releases Page**](https://github.com/dirtybug/morsego/releases).

For the complete release log and version metadata, see [`RELEASES.md`](RELEASES.md).

---

## 🚀 Automated CI/CD Pipeline (GitHub Actions)

The continuous integration and delivery pipeline is defined in [`.github/workflows/ci.yml`](.github/workflows/ci.yml):

1. **Pipeline Triggers:**
   * **Push to Any Branch:** Runs all unit tests and builds inside Docker.
   * **Pull Requests:** Validates tests and builds before merging into `main`.
   * **Tag Push (`v*`):** Runs the full production release pipeline.
2. **Hermetic Docker Execution:**
   * Runs tests, builds signed APK and signed Android App Bundle (AAB), generates HTML reports, and captures UI screenshots.
3. **Clean Deliverables Organization:**
   * Generates clean, flat artifacts without redundant internal zips or nested directory trees.
4. **Automatic Release Publishing:**
   * Publishes GitHub Releases with direct download links for the `.aab` bundle and `.apk`.
   * Automatically updates `AndroidManifest.xml` and [`RELEASES.md`](RELEASES.md) on the `main` branch.
