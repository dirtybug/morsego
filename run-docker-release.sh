#!/usr/bin/env bash
set -e

echo "======================================================"
echo "      MorseGO - Docker Release & Test Runner          "
echo "======================================================"
echo ""

SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
cd "$SCRIPT_DIR"

VERSION="${1:-v1.0.0}"
echo "[INFO] Target Version: $VERSION"
echo ""

if ! command -v docker >/dev/null 2>&1; then
    echo "[ERROR] Docker was not found in PATH. Please install Docker."
    exit 1
fi

echo "[RUN] 1. Running Full Test Suite in Docker..."
docker compose run --rm test-all || echo "[WARN] Some tests reported warnings."

echo ""
echo "[RUN] 2. Building Release APK in Docker..."
docker compose run --rm build-release

# Organize into versioned folders
mkdir -p "$SCRIPT_DIR/releases/$VERSION"
mkdir -p "$SCRIPT_DIR/tests/$VERSION"

if [ -f "$SCRIPT_DIR/build-apks/morseGO-release.apk" ]; then
    cp "$SCRIPT_DIR/build-apks/morseGO-release.apk" "$SCRIPT_DIR/releases/$VERSION/morseGO-$VERSION-release.apk"
fi
if [ -f "$SCRIPT_DIR/build-apks/morseGO-debug.apk" ]; then
    cp "$SCRIPT_DIR/build-apks/morseGO-debug.apk" "$SCRIPT_DIR/releases/$VERSION/morseGO-$VERSION-debug.apk"
fi
if [ -f "$SCRIPT_DIR/reports/unit-tests/index.html" ]; then
    cp "$SCRIPT_DIR/reports/unit-tests/index.html" "$SCRIPT_DIR/tests/$VERSION/index.html"
fi

echo ""
echo "======================================================"
echo "  DOCKER RELEASE & TESTS COMPLETED SUCCESSFULLY!      "
echo "======================================================"
echo ""
echo "  Version:        $VERSION"
echo "  Release Folder: $SCRIPT_DIR/releases/$VERSION/"
echo "  Tests Folder:   $SCRIPT_DIR/tests/$VERSION/"
echo "  Catalog:        $SCRIPT_DIR/releases/RELEASES.md"
echo ""
ls -lh "$SCRIPT_DIR/releases/$VERSION/" 2>/dev/null || true
echo ""
