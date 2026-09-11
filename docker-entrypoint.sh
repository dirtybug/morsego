#!/usr/bin/env bash
set -e

echo "====================================================="
echo "               MorseGO Test Runner                   "
echo "====================================================="

VERSION_TAG="${APP_VERSION_NAME:-1.0.0}"
if [[ "$VERSION_TAG" != v* ]]; then
    VERSION_TAG="v${VERSION_TAG}"
fi
RELEASE_DIR="/workspace/release/${VERSION_TAG}"
mkdir -p "$RELEASE_DIR/reports/unit-tests"
mkdir -p "$RELEASE_DIR/reports/instrumented"
mkdir -p "$RELEASE_DIR/screenshots"

ACTION="${1:-unit}"

export GRADLE_OPTS="-Dorg.gradle.daemon=false -Dorg.gradle.jvmargs=\"-Xmx2048m -XX:MaxMetaspaceSize=512m\""

case "$ACTION" in
    unit|test)
        echo ">>> Running Unit Tests (com.morsego.app: MorseBinaryTree, MorseTiming, MorseWordGenerator)..."
        ./gradlew test -PversionName="${APP_VERSION_NAME:-1.0.0}" -PversionCode="${APP_VERSION_CODE:-1}" --info --stacktrace
        echo ""
        echo ">>> Copying unit test reports to $RELEASE_DIR/reports/unit-tests..."
        if [ -d "app/build/reports/tests/testDebugUnitTest" ]; then
            cp -r app/build/reports/tests/testDebugUnitTest/* "$RELEASE_DIR/reports/unit-tests/"
            cp "$RELEASE_DIR/reports/unit-tests/index.html" "$RELEASE_DIR/reports/index.html" 2>/dev/null || true
            echo "✓ Unit test report saved to release/${VERSION_TAG}/reports/index.html"
        fi
        echo "✓ All unit tests passed successfully!"
        ;;

    build|assemble)
        echo ">>> Building Debug and AndroidTest APKs (Version: ${APP_VERSION_NAME:-1.0.0}, Code: ${APP_VERSION_CODE:-1})..."
        ./gradlew assembleDebug assembleDebugAndroidTest -PversionName="${APP_VERSION_NAME:-1.0.0}" -PversionCode="${APP_VERSION_CODE:-1}" --info
        find app/build/outputs/apk/debug -name "*.apk" -exec cp {} "$RELEASE_DIR/morseGO-${VERSION_TAG}-debug.apk" \; 2>/dev/null || true
        echo "✓ Debug APK saved to release/${VERSION_TAG}/morseGO-${VERSION_TAG}-debug.apk"
        ;;

    release)
        echo ">>> Building Release APK for Google Play Store (Version: ${APP_VERSION_NAME:-1.0.0}, Code: ${APP_VERSION_CODE:-1})..."
        ./gradlew assembleRelease -PversionName="${APP_VERSION_NAME:-1.0.0}" -PversionCode="${APP_VERSION_CODE:-1}" --info
        find app/build/outputs/apk/release -name "*.apk" -exec cp {} "$RELEASE_DIR/morseGO-${VERSION_TAG}-release.apk" \; 2>/dev/null || true
        (cd "$RELEASE_DIR" && sha256sum *.apk > SHA256SUMS.txt 2>/dev/null || true)
        echo "✓ Release APK saved to release/${VERSION_TAG}/morseGO-${VERSION_TAG}-release.apk"
        ;;

    lint)
        echo ">>> Running Android Lint..."
        ./gradlew lintDebug || true
        mkdir -p "$RELEASE_DIR/reports/lint"
        if [ -d "app/build/reports" ]; then
            find app/build/reports -name "lint-results*" -exec cp {} "$RELEASE_DIR/reports/lint/" \; 2>/dev/null || true
            echo "✓ Lint reports saved to release/${VERSION_TAG}/reports/lint/"
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

        echo ">>> Collecting behavior test screenshots from device..."
        # Pull screenshots saved by ScreenshotHelper in /sdcard/Android/data/com.morsego.app/files/Pictures/behavior_screenshots/
        adb pull /sdcard/Android/data/com.morsego.app/files/Pictures/behavior_screenshots/. "$RELEASE_DIR/screenshots/" 2>/dev/null || true

        if [ -d "app/build/reports/androidTests/connected" ]; then
            cp -r app/build/reports/androidTests/connected/* "$RELEASE_DIR/reports/instrumented/"
            echo "✓ Instrumented test report saved to release/${VERSION_TAG}/reports/instrumented/index.html"
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
        echo "✓ Full suite finished! All artifacts saved to release/${VERSION_TAG}/"
        ;;

    *)
        echo ">>> Executing custom command: $@"
        exec "$@"
        ;;
esac
