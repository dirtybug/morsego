#!/usr/bin/env bash
set -e

echo "====================================================="
echo "               MorseGO Test Runner                   "
echo "====================================================="

VERSION_TAG="${APP_VERSION_NAME:-1.0.0}"
if [[ "$VERSION_TAG" != v* ]]; then
    VERSION_TAG="v${VERSION_TAG}"
fi
RELEASE_DIR="${RELEASE_DIR:-/workspace/release/development}"
mkdir -p "$RELEASE_DIR/reports/unit-tests"
mkdir -p "$RELEASE_DIR/reports/instrumented"
[ -f /workspace/gradlew ] && chmod +x /workspace/gradlew 2>/dev/null || true
rm -f /root/.gradle/caches/journal-1/*.lock 2>/dev/null || true
rm -f /root/.gradle/caches/*.lock 2>/dev/null || true

ACTION="${1:-unit}"

export GRADLE_OPTS="-Dorg.gradle.daemon=false -Dorg.gradle.jvmargs=\"-Xmx2048m -XX:MaxMetaspaceSize=512m\""

case "$ACTION" in
    dev|shell|bash|sh)
        export GRADLE_OPTS="-Dorg.gradle.daemon=true -Dorg.gradle.jvmargs=\"-Xmx2048m -XX:MaxMetaspaceSize=512m\""
        echo "====================================================="
        echo "        morseGO - Development Environment            "
        echo "====================================================="
        echo "Java:        $(java -version 2>&1 | head -n 1)"
        echo "Android SDK: $ANDROID_HOME (API 34, Build-Tools 34.0.0)"
        echo "Workspace:   $(pwd)"
        echo "Available quick commands:"
        echo "  test                        -> Run unit tests"
        echo "  build                       -> Compile Debug APK"
        echo "  release                     -> Compile Release APK"
        echo "  lint                        -> Run Android Lint analysis"
        echo "  ./gradlew test --continuous -> Automatically re-test on edit"
        echo "  exit                        -> Exit container"
        echo "====================================================="
        exec /bin/bash
        ;;

    unit|test)
        echo ">>> Running Unit Tests (com.morsego.app: MorseBinaryTree, MorseTiming, MorseWordGenerator)..."
        ./gradlew test -PversionName="${APP_VERSION_NAME:-1.0.0}" -PversionCode="${APP_VERSION_CODE:-1}" --info --stacktrace
        echo ""
        echo ">>> Copying unit test reports to $RELEASE_DIR/reports/unit-tests..."
        if [ -d "app/build/reports/tests/testDebugUnitTest" ]; then
            cp -r app/build/reports/tests/testDebugUnitTest/* "$RELEASE_DIR/reports/unit-tests/"
            [ ! -f "$RELEASE_DIR/reports/index.html" ] && cp "$RELEASE_DIR/reports/unit-tests/index.html" "$RELEASE_DIR/reports/index.html" 2>/dev/null || true
            echo "✓ Unit test report saved to $RELEASE_DIR/reports/unit-tests/index.html"
        fi
        echo ""
        echo ">>> Generating visual behavior and mode screenshots into $RELEASE_DIR/screenshots..."
        mkdir -p "$RELEASE_DIR/screenshots"
        java -Djava.awt.headless=true tools/ScreenshotGenerator.java "$RELEASE_DIR/screenshots" || true
        echo "✓ All unit tests and screenshots generated successfully!"
        ;;

    screenshots)
        echo ">>> Generating visual behavior and mode screenshots into $RELEASE_DIR/screenshots..."
        mkdir -p "$RELEASE_DIR/screenshots"
        java -Djava.awt.headless=true tools/ScreenshotGenerator.java "$RELEASE_DIR/screenshots"
        echo "✓ Screenshots saved to $RELEASE_DIR/screenshots/"
        ;;

    build|assemble)
        echo ">>> Building Debug and AndroidTest APKs (Version: ${APP_VERSION_NAME:-1.0.0}, Code: ${APP_VERSION_CODE:-1})..."
        ./gradlew assembleDebug assembleDebugAndroidTest -PversionName="${APP_VERSION_NAME:-1.0.0}" -PversionCode="${APP_VERSION_CODE:-1}" --info
        find app/build/outputs/apk/debug -name "*.apk" -exec cp {} "$RELEASE_DIR/morseGO-debug.apk" \; 2>/dev/null || true
        cp -f "$RELEASE_DIR/morseGO-debug.apk" "$RELEASE_DIR/morseGO-development-debug.apk" 2>/dev/null || true
        echo "✓ Debug APK saved to $RELEASE_DIR/morseGO-debug.apk"
        ;;

    release)
        echo ">>> Building Release APK for Google Play Store (Version: ${APP_VERSION_NAME:-1.0.0}, Code: ${APP_VERSION_CODE:-1})..."
        ./gradlew assembleRelease -PversionName="${APP_VERSION_NAME:-1.0.0}" -PversionCode="${APP_VERSION_CODE:-1}" --info
        find app/build/outputs/apk/release -name "*.apk" -exec cp {} "$RELEASE_DIR/morseGO-release.apk" \; 2>/dev/null || true
        cp -f "$RELEASE_DIR/morseGO-release.apk" "$RELEASE_DIR/morseGO-development-release.apk" 2>/dev/null || true
        (cd "$RELEASE_DIR" && sha256sum *.apk > SHA256SUMS.txt 2>/dev/null || true)
        echo "✓ Release APK saved to $RELEASE_DIR/morseGO-release.apk"
        ;;

    lint)
        echo ">>> Running Android Lint..."
        ./gradlew lintDebug || true
        mkdir -p "$RELEASE_DIR/reports/lint"
        if [ -d "app/build/reports" ]; then
            find app/build/reports -name "lint-results*" -exec cp {} "$RELEASE_DIR/reports/lint/" \; 2>/dev/null || true
            echo "✓ Lint reports saved to $RELEASE_DIR/reports/lint/"
        fi
        ;;

    connected|instrumented)
        echo ">>> Checking for Android device or emulator connection..."
        adb devices
        DEVICE_COUNT=$(adb devices | grep -v "List" | grep "device$" | wc -l)
        if [ "$DEVICE_COUNT" -eq 0 ]; then
            echo "Trying to connect to host ADB daemon (host.docker.internal:5555)..."
            adb connect host.docker.internal:5555 || true
            sleep 2
        fi

        DEVICE_COUNT=$(adb devices | grep -v "List" | grep "device$" | wc -l)
        if [ "$DEVICE_COUNT" -eq 0 ]; then
            echo "⚠️ No connected Android device or emulator detected via ADB."
            echo "To run instrumented tests from Docker, either:"
            echo "  1. Start an emulator or connect a device on host, run 'adb tcpip 5555', and the container will connect to host.docker.internal:5555"
            echo "  2. Run './gradlew connectedAndroidTest' directly on the host machine."
            exit 1
        fi

        echo ">>> Running Connected Android Instrumented Tests (Behavior, Radio Words, Screenshots)..."
        ./gradlew connectedDebugAndroidTest -PversionName="${APP_VERSION_NAME:-1.0.0}" -PversionCode="${APP_VERSION_CODE:-1}" --info

        if [ "${GENERATE_SCREENSHOTS:-false}" = "true" ] || [ "${GENERATE_SCREENSHOTS:-0}" = "1" ]; then
            echo ">>> Collecting behavior test screenshots from device..."
            mkdir -p "$RELEASE_DIR/screenshots"
            adb pull /sdcard/Android/data/com.morsego.app/files/Pictures/behavior_screenshots/. "$RELEASE_DIR/screenshots/" 2>/dev/null || true
        else
            echo "ℹ️ Skipping screenshot generation."
        fi

        if [ -d "app/build/reports/androidTests/connected" ]; then
            cp -r app/build/reports/androidTests/connected/* "$RELEASE_DIR/reports/instrumented/"
            echo "✓ Instrumented test report saved to $RELEASE_DIR/reports/instrumented/index.html"
        fi
        ;;

    all)
        echo ">>> Running Full Test Suite (Unit Tests + Build + Release)..."
        $0 unit
        $0 build
        $0 release
        DEVICE_COUNT=$(adb devices | grep -v "List" | grep "device$" | wc -l)
        if [ "$DEVICE_COUNT" -gt 0 ]; then
            $0 connected
        else
            echo "ℹ️ Note: Skipping connected tests because no ADB device is connected."
        fi
        echo "✓ Full suite finished! All artifacts saved to $RELEASE_DIR/"
        ;;

    *)
        echo ">>> Executing custom command: $@"
        exec "$@"
        ;;
esac
