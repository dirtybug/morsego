#!/usr/bin/env bash
set -e

if [ "$ENTRYPOINT_RELOADED" != "1" ] && [ -f /workspace/docker-entrypoint.sh ] && [ "$0" != "/workspace/docker-entrypoint.sh" ]; then
    export ENTRYPOINT_RELOADED=1
    sed -i 's/\r$//' /workspace/docker-entrypoint.sh 2>/dev/null || true
    chmod +x /workspace/docker-entrypoint.sh 2>/dev/null || true
    exec /bin/bash /workspace/docker-entrypoint.sh "$@"
fi

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

# Update AndroidManifest.xml with Play Store version if APP_VERSION_NAME is provided
if [ -n "$APP_VERSION_NAME" ] && [ -f /workspace/app/src/main/AndroidManifest.xml ]; then
    CLEAN_VER="${APP_VERSION_NAME#v}"
    sed -i -E "s/android:versionName=\"[^\"]*\"/android:versionName=\"${CLEAN_VER}\"/g" /workspace/app/src/main/AndroidManifest.xml 2>/dev/null || true
    if [ -n "$APP_VERSION_CODE" ]; then
        sed -i -E "s/android:versionCode=\"[^\"]*\"/android:versionCode=\"${APP_VERSION_CODE}\"/g" /workspace/app/src/main/AndroidManifest.xml 2>/dev/null || true
    fi
fi
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
        echo "Android SDK: $ANDROID_HOME (API 36, Build-Tools 36.0.0)"
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
        echo ">>> Setting up Behavior Tests report in $RELEASE_DIR/reports/behavior-tests..."
        mkdir -p "$RELEASE_DIR/reports/behavior-tests"
        if [ -f "/workspace/tools/reports/behavior-tests/index.html" ]; then
            cp "/workspace/tools/reports/behavior-tests/index.html" "$RELEASE_DIR/reports/behavior-tests/index.html"
            cp "/workspace/tools/reports/behavior-tests/index.html" "$RELEASE_DIR/reports/behavior-tests.html"
        elif [ -f "tools/reports/behavior-tests/index.html" ]; then
            cp "tools/reports/behavior-tests/index.html" "$RELEASE_DIR/reports/behavior-tests/index.html"
            cp "tools/reports/behavior-tests/index.html" "$RELEASE_DIR/reports/behavior-tests.html"
        fi
        echo ""
        echo ">>> Generating all visual behavior and mode screenshots into $RELEASE_DIR/screenshots..."
        mkdir -p "$RELEASE_DIR/screenshots"
        javac -encoding UTF-8 -cp ".:app/src/main/java" tools/*.java 2>/dev/null
        java -Djava.awt.headless=true -cp ".:app/src/main/java" tools.ScreenshotGenerator "$RELEASE_DIR/screenshots" || true
        java -Djava.awt.headless=true -cp ".:app/src/main/java" tools.TreeScreenshotFixer "$RELEASE_DIR/screenshots" || true
        java -Djava.awt.headless=true -cp ".:app/src/main/java" tools.ListeningExamFixer "$RELEASE_DIR/screenshots" || true
        java -Djava.awt.headless=true -cp ".:app/src/main/java" tools.StandardizeBottomNav "$RELEASE_DIR/screenshots" || true
        rm -f tools/*.class 2>/dev/null || true

        echo "✓ All unit tests, reports, and screenshots generated successfully in $RELEASE_DIR!"
        ;;

    screenshots)
        echo ">>> Generating all visual behavior and mode screenshots into $RELEASE_DIR/screenshots..."
        mkdir -p "$RELEASE_DIR/screenshots"
        mkdir -p "$RELEASE_DIR/reports/behavior-tests"
        if [ -f "/workspace/tools/reports/behavior-tests/index.html" ]; then
            cp "/workspace/tools/reports/behavior-tests/index.html" "$RELEASE_DIR/reports/behavior-tests/index.html"
            cp "/workspace/tools/reports/behavior-tests/index.html" "$RELEASE_DIR/reports/behavior-tests.html"
        elif [ -f "tools/reports/behavior-tests/index.html" ]; then
            cp "tools/reports/behavior-tests/index.html" "$RELEASE_DIR/reports/behavior-tests/index.html"
            cp "tools/reports/behavior-tests/index.html" "$RELEASE_DIR/reports/behavior-tests.html"
        fi
        javac -encoding UTF-8 -cp ".:app/src/main/java" tools/*.java 2>/dev/null
        java -Djava.awt.headless=true -cp ".:app/src/main/java" tools.ScreenshotGenerator "$RELEASE_DIR/screenshots" || true
        java -Djava.awt.headless=true -cp ".:app/src/main/java" tools.TreeScreenshotFixer "$RELEASE_DIR/screenshots" || true
        java -Djava.awt.headless=true -cp ".:app/src/main/java" tools.ListeningExamFixer "$RELEASE_DIR/screenshots" || true
        java -Djava.awt.headless=true -cp ".:app/src/main/java" tools.StandardizeBottomNav "$RELEASE_DIR/screenshots" || true
        rm -f tools/*.class 2>/dev/null || true

        echo "✓ All screenshots saved to $RELEASE_DIR/screenshots/!"
        ;;

    build|assemble)
        echo ">>> Building Debug and AndroidTest APKs (Version: ${APP_VERSION_NAME:-1.0.0}, Code: ${APP_VERSION_CODE:-1})..."
        ./gradlew assembleDebug assembleDebugAndroidTest -PversionName="${APP_VERSION_NAME:-1.0.0}" -PversionCode="${APP_VERSION_CODE:-1}" --info
        find app/build/outputs/apk/debug -name "*.apk" -exec cp {} "$RELEASE_DIR/morseGO-debug.apk" \; 2>/dev/null || true
        cp -f "$RELEASE_DIR/morseGO-debug.apk" "$RELEASE_DIR/morseGO-development-debug.apk" 2>/dev/null || true
        echo "✓ Debug APK saved to $RELEASE_DIR/morseGO-debug.apk"
        ;;

    release)
        echo ">>> Building Release Deliverables (Version: ${APP_VERSION_NAME:-1.0.0}, Code: ${APP_VERSION_CODE:-1})..."
        KEY_ALIAS_PARAM=""
        if [ -n "$KEY_ALIAS" ]; then
            KEY_ALIAS_PARAM="-PkeyAlias=$KEY_ALIAS"
        fi
        KEY_PASS_PARAM=""
        if [ -n "$KEYSTORE_PASSWORD" ]; then
            KEY_PASS_PARAM="-PkeystorePassword=$KEYSTORE_PASSWORD"
        fi
        ./gradlew assembleRelease bundleRelease -PversionName="${APP_VERSION_NAME:-1.0.0}" -PversionCode="${APP_VERSION_CODE:-1}" $KEY_PASS_PARAM $KEY_ALIAS_PARAM --info
        find app/build/outputs/apk/release -name "*.apk" -exec cp {} "$RELEASE_DIR/morseGO-release.apk" \; 2>/dev/null || true
        cp -f "$RELEASE_DIR/morseGO-release.apk" "$RELEASE_DIR/morseGO-development-release.apk" 2>/dev/null || true
        find app/build/outputs/bundle/release -name "*.aab" -exec cp {} "$RELEASE_DIR/morseGO-release.aab" \; 2>/dev/null || true
        (cd "$RELEASE_DIR" && sha256sum *.apk *.aab > SHA256SUMS.txt 2>/dev/null || true)
        echo "✓ Release APK saved to $RELEASE_DIR/morseGO-release.apk"
        [ -f "$RELEASE_DIR/morseGO-release.aab" ] && echo "✓ Release AAB saved to $RELEASE_DIR/morseGO-release.aab"
        ;;

    bundle|aab)
        echo ">>> Building and Signing Release AAB (Android App Bundle for Google Play Store)..."
        if [ ! -f "release/key.jks" ] && [ ! -f "release/release.keystore" ] && [ ! -f "key.jks" ] && [ ! -f "app/release.keystore" ]; then
            echo "❌ [ERROR] Keystore not found! Will NOT sign AAB."
            exit 1
        fi
        if [ -z "$KEYSTORE_PASSWORD" ] && [ ! -f "release/keystore-pass.txt" ] && [ ! -f "keystore-pass.txt" ]; then
            echo "❌ [ERROR] Key password file not found! Will NOT sign AAB."
            exit 1
        fi
        KEY_ALIAS_PARAM=""
        if [ -n "$KEY_ALIAS" ]; then
            KEY_ALIAS_PARAM="-PkeyAlias=$KEY_ALIAS"
        fi
        KEY_PASS_PARAM=""
        if [ -n "$KEYSTORE_PASSWORD" ]; then
            KEY_PASS_PARAM="-PkeystorePassword=$KEYSTORE_PASSWORD"
        fi
        ./gradlew bundleRelease -PversionName="${APP_VERSION_NAME:-1.0.0}" -PversionCode="${APP_VERSION_CODE:-1}" $KEY_PASS_PARAM $KEY_ALIAS_PARAM --info
        AAB_FILE=$(find app/build/outputs/bundle/release -name "*.aab" 2>/dev/null | head -n 1)
        if [ -n "$AAB_FILE" ] && [ -f "$AAB_FILE" ]; then
            TARGET_VER="${APP_VERSION_NAME:-1.0.0}"
            TARGET_VER="${TARGET_VER#v}"
            VERSION_DIR="/workspace/release/v${TARGET_VER}"
            mkdir -p "$VERSION_DIR"
            mkdir -p "/workspace/release/development"
            cp -f "$AAB_FILE" "$VERSION_DIR/morseGO-release.aab"
            cp -f "$AAB_FILE" "/workspace/release/development/morseGO-release.aab"
            (cd "$VERSION_DIR" && sha256sum morseGO-release.aab > morseGO-release.aab.sha256 2>/dev/null || true)
            echo "✓ Signed Release AAB saved to $VERSION_DIR/morseGO-release.aab"
            echo "✓ Development AAB saved to /workspace/release/development/morseGO-release.aab"
        else
            echo "❌ Error: Release AAB file was not generated!"
            exit 1
        fi
        ;;

    lint)
        echo ">>> Running Android Lint..."
        ./gradlew lintDebug || true
        mkdir -p "$RELEASE_DIR/reports/lint"
        if [ -d "app/build/reports" ]; then
            find app/build/reports -name "lint-results*" -exec cp {} "$RELEASE_DIR/reports/lint/" \; 2>/dev/null || true
        fi
        ;;

    connected|instrumented)
        echo ">>> Running Instrumented Tests on connected device..."
        chmod +x ./gradlew 2>/dev/null || true
        ./gradlew connectedDebugAndroidTest || true
        mkdir -p "$RELEASE_DIR/reports/instrumented"
        mkdir -p "$RELEASE_DIR/screenshots"

        DEVICE_ID=$(adb devices 2>/dev/null | grep -v "List" | grep "device$" | head -n 1 | awk '{print $1}')
        if [ -n "$DEVICE_ID" ]; then
            echo ">>> Pulling behavior screenshots from device $DEVICE_ID..."
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
        echo ">>> Running Full Test Suite (Unit Tests + Build + Release APK)..."
        "$0" unit
        "$0" build
        "$0" release
        DEVICE_COUNT=$(adb devices 2>/dev/null | grep -v "List" | grep "device$" | wc -l || echo 0)
        if [ "$DEVICE_COUNT" -gt 0 ]; then
            "$0" connected
        else
            echo "ℹ️ Note: Skipping connected tests because no ADB device is connected."
        fi
        echo "✓ Full suite finished! All artifacts saved to $RELEASE_DIR/!"
        ;;

    *)
        echo ">>> Executing custom command: $@"
        exec "$@"
        ;;
esac
