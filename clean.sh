#!/usr/bin/env bash
# morseGO - Limpeza completa de ficheiros temporarios, build, .gradle e .idea
echo "===================================================="
echo "            morseGO - Limpeza de Workspace          "
echo "===================================================="
echo ""

echo "[1/4] A parar processos do Gradle..."
[ -f "./gradlew" ] && ./gradlew --stop 2>/dev/null || true

echo "[2/4] A remover pastas de build..."
rm -rf build app/build

echo "[3/4] A remover pastas .gradle e .idea..."
rm -rf .gradle .idea

echo "[4/4] A remover ficheiros residuais de cache..."
rm -f *.iml app/*.iml

echo ""
echo "===================================================="
echo "  Workspace limpo com sucesso!"
echo "  Pastas removidas: .gradle, .idea, build, app/build"
echo "===================================================="
echo ""
