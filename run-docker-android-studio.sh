#!/bin/bash
# morseGO - Executar Android Studio com GUI Nativa (sem VNC)

echo "===================================================="
echo "     morseGO - Android Studio GUI Runner (Nativo)   "
echo "===================================================="
echo ""

ACTION="${1:-start}"

case "$ACTION" in
    stop)
        echo "[INFO] Fechando Android Studio..."
        killall -9 studio.sh java 2>/dev/null || true
        docker compose stop android-studio 2>/dev/null || true
        echo "[INFO] Android Studio fechado com sucesso!"
        exit 0
        ;;
    start|*)
        echo "[1/2] Preparando ambiente gráfico nativo (sem VNC)..."
        echo "[2/2] Abrindo Android Studio em janela nativa..."
        if [ -f "/opt/android-studio/bin/studio.sh" ]; then
            env ANDROID_HOME=/opt/android-sdk PATH="/opt/android-sdk/platform-tools:/opt/android-sdk/cmdline-tools/latest/bin:$PATH" /opt/android-studio/bin/studio.sh . &
        else
            echo "[ERRO] /opt/android-studio/bin/studio.sh não encontrado."
            exit 1
        fi

        echo ""
        echo "===================================================="
        echo "          Android Studio Aberto com Sucesso!        "
        echo "===================================================="
        echo "  - Modo:    Interface Gráfica Nativa (sem VNC)"
        echo "  - Projeto: morseGO"
        echo "  - SDK:     /opt/android-sdk (API 34)"
        echo "===================================================="
        ;;
esac
