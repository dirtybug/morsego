#!/usr/bin/env bash
# morseGO - Complete cleanup of temporary files, build outputs, .gradle and .idea
echo "===================================================="
echo "            morseGO - Workspace Clean               "
echo "===================================================="
echo ""

echo "[1/4] Stopping Gradle daemons..."
[ -f "./gradlew" ] && ./gradlew --stop 2>/dev/null || true

echo "[2/4] Removing build directories..."
rm -rf build app/build

echo "[3/4] Removing .gradle and .idea directories..."
rm -rf .gradle .idea

echo "[4/4] Removing residual cache and iml files..."
rm -f *.iml app/*.iml

echo ""
echo "===================================================="
echo "  Workspace cleaned successfully!"
echo "  Removed directories: .gradle, .idea, build, app/build"
echo "===================================================="
echo ""
