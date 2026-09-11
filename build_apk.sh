#!/usr/bin/env bash
set -e

echo "======================================================"
echo "          MorseGO - Android APK Builder               "
echo "======================================================"
echo ""

SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
cd "$SCRIPT_DIR"

# 1. Check for Java / JAVA_HOME
if [ -z "$JAVA_HOME" ]; then
    if [ -x "/Applications/Android Studio.app/Contents/jbr/Contents/Home/bin/java" ]; then
        export JAVA_HOME="/Applications/Android Studio.app/Contents/jbr/Contents/Home"
    elif [ -d "/usr/lib/jvm/default-java" ]; then
        export JAVA_HOME="/usr/lib/jvm/default-java"
    fi
fi

if [ -n "$JAVA_HOME" ]; then
    export PATH="$JAVA_HOME/bin:$PATH"
    echo "[OK] Using Java from: $JAVA_HOME"
fi

mkdir -p "$SCRIPT_DIR/build-apks"

# 2. Determine execution engine
if command -v java >/dev/null 2>&1; then
    echo "[RUN] Building APK via Gradle Wrapper..."
    ./gradlew assembleDebug --info
    if [ -f "$SCRIPT_DIR/app/build/outputs/apk/debug/app-debug.apk" ]; then
        cp "$SCRIPT_DIR/app/build/outputs/apk/debug/app-debug.apk" "$SCRIPT_DIR/build-apks/morseGO-debug.apk"
    fi
elif command -v docker >/dev/null 2>&1; then
    echo "[RUN] Building APK inside Docker Container..."
    docker compose run --rm build-apk
else
    echo "[ERROR] Neither Java nor Docker found in PATH."
    exit 1
fi

echo ""
echo "======================================================"
echo "  BUILD COMPLETED SUCCESSFULLY!                       "
echo "======================================================"
echo ""
echo "  Output folder: $SCRIPT_DIR/build-apks/"
ls -lh "$SCRIPT_DIR/build-apks/" 2>/dev/null || true
echo ""
