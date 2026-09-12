#!/bin/bash
# morseGO - Executar Android Studio com GUI no Docker (Linux / macOS / WSL)

echo "===================================================="
echo "     morseGO - Android Studio GUI Docker Runner     "
echo "===================================================="
echo ""

if ! command -v docker &>/dev/null; then
    echo "[ERRO] Docker não foi encontrado no PATH."
    exit 1
fi

if ! docker info &>/dev/null; then
    echo "[ERRO] O daemon do Docker não está em execução."
    exit 1
fi

ACTION="${1:-start}"

case "$ACTION" in
    stop)
        echo "[INFO] Parando o container do Android Studio..."
        docker compose stop android-studio
        echo "[INFO] Container parado com sucesso!"
        exit 0
        ;;
    logs)
        echo "[INFO] Exibindo logs do Android Studio..."
        docker compose logs -f android-studio
        exit 0
        ;;
    build)
        echo "[INFO] Reconstruindo imagem Docker do Android Studio..."
        docker compose build android-studio
        exit 0
        ;;
    restart)
        echo "[INFO] Reiniciando Android Studio..."
        docker compose restart android-studio
        sleep 3
        if command -v xdg-open &>/dev/null; then
            xdg-open "http://localhost:6080/vnc.html?autoconnect=true&resize=remote" 2>/dev/null || true
        elif command -v open &>/dev/null; then
            open "http://localhost:6080/vnc.html?autoconnect=true&resize=remote" 2>/dev/null || true
        fi
        exit 0
        ;;
    start|*)
        echo "[1/3] Iniciando o container Docker do Android Studio GUI..."
        docker compose up -d android-studio

        if [ $? -ne 0 ]; then
            echo "[AVISO] Construindo imagem morsego-android-studio..."
            docker compose build android-studio
            docker compose up -d android-studio
        fi

        echo "[2/3] Aguardando inicialização do servidor noVNC..."
        sleep 4

        echo "[3/3] Abrindo o Android Studio no navegador web..."
        if command -v xdg-open &>/dev/null; then
            xdg-open "http://localhost:6080/vnc.html?autoconnect=true&resize=remote" 2>/dev/null || true
        elif command -v open &>/dev/null; then
            open "http://localhost:6080/vnc.html?autoconnect=true&resize=remote" 2>/dev/null || true
        fi

        echo ""
        echo "===================================================="
        echo "          Android Studio Pronto e Carregado!        "
        echo "===================================================="
        echo "  [Interface Web noVNC]"
        echo "  - URL no Navegador:     http://localhost:6080/"
        echo "  - Auto-Conexão Direta:  http://localhost:6080/vnc.html?autoconnect=true&resize=remote"
        echo ""
        echo "  [Cliente VNC Nativo]"
        echo "  - Host / Porta:         localhost:5900 (sem senha)"
        echo ""
        echo "  [Projeto morseGO]"
        echo "  - Localização montada:  /workspace (morseGO)"
        echo "  - Status:               Carregado automaticamente pelo studio.sh"
        echo "  - SDK Android:          /opt/android-sdk (API 34, build-tools 34.0.0)"
        echo "===================================================="
        ;;
esac
