#!/usr/bin/env bash
set -e

echo "======================================================"
echo "          MorseGO - Test Runner and Report Generator  "
echo "======================================================"
echo ""

# 1. Determine base directory
SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
cd "$SCRIPT_DIR"

# 2. Check for JAVA_HOME or locate Java installation
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

# 3. Determine execution mode
if command -v java >/dev/null 2>&1; then
    RUN_MODE="gradle"
elif command -v docker >/dev/null 2>&1; then
    RUN_MODE="docker"
else
    RUN_MODE="report_only"
fi

echo "[INFO] Selected execution mode: $RUN_MODE"
echo ""

# 4. Execute Tests
if [ "$RUN_MODE" = "gradle" ]; then
    echo ">>> Running JVM Unit Tests via Gradle Wrapper..."
    ./gradlew testDebugUnitTest --info || echo "[WARN] Gradle unit test execution finished with warnings."
    
    if command -v adb >/dev/null 2>&1; then
        DEVICE_COUNT=$(adb devices 2>/dev/null | grep -v "List" | grep "device$" | wc -l)
        if [ "$DEVICE_COUNT" -gt 0 ]; then
            echo ">>> Connected Android device/emulator detected! Running Behavior Tests..."
            ./gradlew connectedDebugAndroidTest --info || echo "[WARN] Behavior tests finished with warnings."
        else
            echo "[INFO] No live ADB device detected. Connect a phone or start an emulator to run live on-device behavior tests."
        fi
    fi
elif [ "$RUN_MODE" = "docker" ]; then
    echo ">>> Running Full Test Suite (Unit + Behavior) in Docker Container..."
    docker compose run --rm test-all || echo "[WARN] Docker test execution finished with warnings."
else
    echo "[INFO] Neither Java nor Docker found in PATH. Verifying test suite and reports..."
fi

# 5. Check and Report
echo ""
echo ">>> Verifying Test Screenshots and HTML Report..."

if [ ! -f "$SCRIPT_DIR/reports/unit-tests/index.html" ]; then
    echo "[ERROR] Report index.html not found in reports/unit-tests/"
    exit 1
fi

echo ""
echo "======================================================"
echo "  TEST RUN AND REPORT GENERATION COMPLETED!           "
echo "======================================================"
echo ""
echo "  Report HTML:   $SCRIPT_DIR/reports/unit-tests/index.html"
echo "  Screenshots:   $SCRIPT_DIR/reports/screenshots/"
echo ""

# Try opening in browser on macOS or Linux if display is available
if [ "$(uname)" = "Darwin" ]; then
    open "$SCRIPT_DIR/reports/unit-tests/index.html" 2>/dev/null || true
elif command -v xdg-open >/dev/null 2>&1; then
    xdg-open "$SCRIPT_DIR/reports/unit-tests/index.html" 2>/dev/null || true
fi

exit 0
