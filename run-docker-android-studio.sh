#!/bin/bash
# morseGO - Run Android Studio with Native GUI (without VNC)

echo "===================================================="
echo "     morseGO - Android Studio GUI Runner (Native)   "
echo "===================================================="
echo ""

ACTION="${1:-start}"

case "$ACTION" in
    stop)
        echo "[INFO] Stopping Android Studio..."
        killall -9 studio.sh java 2>/dev/null || true
        docker compose stop android-studio 2>/dev/null || true
        echo "[INFO] Android Studio stopped successfully!"
        exit 0
        ;;
    start|*)
        echo "[1/2] Preparing native graphical environment (without VNC)..."
        echo "[2/2] Opening Android Studio in native window..."
        if [ -f "/opt/android-studio/bin/studio.sh" ]; then
            env ANDROID_HOME=/opt/android-sdk PATH="/opt/android-sdk/platform-tools:/opt/android-sdk/cmdline-tools/latest/bin:$PATH" /opt/android-studio/bin/studio.sh . &
        else
            echo "[ERROR] /opt/android-studio/bin/studio.sh not found."
            exit 1
        fi

        echo ""
        echo "===================================================="
        echo "        Android Studio Opened Successfully!         "
        echo "===================================================="
        echo "  - Mode:    Native Graphical Interface (without VNC)"
        echo "  - Project: morseGO"
        echo "  - SDK:     /opt/android-sdk (API 34)"
        echo "===================================================="
        ;;
esac
