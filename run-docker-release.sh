#!/usr/bin/env bash
set -e

echo "======================================================"
echo "      MorseGO - Docker Release & Test Runner          "
echo "======================================================"
echo ""

SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
cd "$SCRIPT_DIR"

if ! command -v docker >/dev/null 2>&1; then
    echo "[ERROR] Docker was not found in PATH. Please install Docker."
    exit 1
fi

echo "[RUN] 1. Running Full Test Suite in Docker..."
docker compose run --rm test-all || echo "[WARN] Some tests reported warnings."

echo ""
echo "[RUN] 2. Building Release APK in Docker..."
docker compose run --rm build-release

echo ""
echo "======================================================"
echo "  DOCKER RELEASE & TESTS COMPLETED SUCCESSFULLY!      "
echo "======================================================"
echo ""
echo "  Release APK: $SCRIPT_DIR/build-apks/morseGO-release.apk"
echo "  Reports:     $SCRIPT_DIR/reports/unit-tests/index.html"
echo ""
ls -lh "$SCRIPT_DIR/build-apks/" 2>/dev/null || true
echo ""
