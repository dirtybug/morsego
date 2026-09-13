#!/usr/bin/env bash
set -e

echo "===================================================="
echo "        morseGO - Docker Dev Environment             "
echo "===================================================="

if ! command -v docker &> /dev/null; then
    echo "[ERROR] Docker not found in PATH."
    exit 1
fi

if ! docker info &> /dev/null; then
    echo "[ERROR] Docker daemon is not running."
    exit 1
fi

echo "Starting interactive development container..."
echo "Type 'exit' to leave container."
echo ""
docker compose run --rm dev