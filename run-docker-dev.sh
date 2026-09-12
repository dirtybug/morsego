#!/usr/bin/env bash
set -e

echo "===================================================="
echo "        morseGO - Docker Dev Environment             "
echo "===================================================="

if ! command -v docker &> /dev/null; then
    echo "[ERRO] Docker nao encontrado no PATH."
    exit 1
fi

if ! docker info &> /dev/null; then
    echo "[ERRO] O daemon do Docker nao esta em execucao."
    exit 1
fi

echo "Iniciando container de desenvolvimento interativo..."
echo "Digite 'exit' para sair do container."
echo ""
docker compose run --rm dev