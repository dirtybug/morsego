#!/usr/bin/env bash
set -e

TARGET="${1:-unit}"

echo "===================================================="
echo "            morseGO - Docker Test Runner            "
echo "===================================================="

if ! command -v docker &> /dev/null; then
    echo "Error: Docker not found. Please install Docker or start Docker daemon."
    exit 1
fi

echo "Target selected: $TARGET"

case "$TARGET" in
    unit)
        docker compose run --rm test-unit
        ;;
    build)
        docker compose run --rm build-apk
        ;;
    release)
        docker compose run --rm build-release
        ;;
    instrumented)
        docker compose run --rm test-instrumented
        ;;
    all)
        docker compose run --rm test-all
        ;;
    *)
        docker compose run --rm test-unit "$@"
        ;;
esac

echo ""
echo "Done! Check ./reports/ for HTML test reports."
