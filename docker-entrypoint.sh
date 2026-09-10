#!/usr/bin/env bash
set -e

echo "====================================================="
echo "               MorseGO Test Runner                   "
echo "====================================================="

mkdir -p /workspace/reports
mkdir -p /workspace/screenshots

ACTION="${1:-unit}"

export GRADLE_OPTS="-Dorg.gradle.daemon=false -Dorg.gradle.jvmargs=\"-Xmx2048m -XX:MaxMetaspaceSize=512m\""

case "$ACTION" in
    unit|test)
        echo ">>> Running Unit Tests (com.morsego.app: MorseBinaryTree, MorseTiming, MorseWordGenerator)..."
        ./gradlew test --info --stacktrace
        echo ""
        echo ">>> Copying unit test reports to /workspace/reports/unit-tests..."
        mkdir -p /workspace/reports/unit-tests
        if [ -d "app/build/reports/tests/testDebugUnitTest" ]; then
            cp -r app/build/reports/tests/testDebugUnitTest/* /workspace/reports/unit-tests/
            echo "✓ Unit test report saved to reports/unit-tests/index.html"
        fi
        echo "✓ All unit tests passed successfully!"
        ;;

    build|assemble)
        echo ">>> Building Debug and AndroidTest APKs..."
        ./gradlew assembleDebug assembleDebugAndroidTest --info
        mkdir -p /workspace/build-apks
        find app/build/outputs/apk -name "*.apk" -exec cp {} /workspace/build-apks/ \; 2>/dev/null || true
        echo "✓ APKs built and saved to build-apks/"
        ;;

    lint)
        echo ">>> Running Android Lint..."
        ./gradlew lintDebug || true
        mkdir -p /workspace/reports/lint
        if [ -d "app/build/reports" ]; then
            find app/build/reports -name "lint-results*" -exec cp {} /workspace/reports/lint/ \; 2>/dev/null || true
            echo "✓ Lint reports saved to reports/lint/"
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
        ./gradlew connectedDebugAndroidTest --info

        echo ">>> Collecting behavior test screenshots from device..."
        # Pull screenshots saved by ScreenshotHelper in /sdcard/Android/data/com.morsego.app/files/Pictures/behavior_screenshots/
        adb pull /sdcard/Android/data/com.morsego.app/files/Pictures/behavior_screenshots /workspace/screenshots/ 2>/dev/null || true

        mkdir -p /workspace/reports/instrumented
        if [ -d "app/build/reports/androidTests/connected" ]; then
            cp -r app/build/reports/androidTests/connected/* /workspace/reports/instrumented/
            echo "✓ Instrumented test report saved to reports/instrumented/index.html"
        fi
        ;;

    all)
        echo ">>> Running Full Test Suite (Unit Tests + Build + Lint)..."
        $0 unit
        $0 build
        $0 lint
        DEVICE_COUNT=$(adb devices | grep -v "List" | grep "device$" | wc -l)
        if [ "$DEVICE_COUNT" -gt 0 ]; then
            $0 connected
        else
            echo "ℹ️ Note: Skipping connected tests because no ADB device is connected."
        fi
        echo "✓ Full suite finished!"
        ;;

    *)
        echo ">>> Executing custom command: $@"
        exec "$@"
        ;;
esac
