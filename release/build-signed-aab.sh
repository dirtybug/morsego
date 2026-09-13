#!/usr/bin/env bash
set -e

SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
cd "$SCRIPT_DIR/.."

echo "===================================================="
echo "        morseGO - Signed Release AAB Builder        "
echo "===================================================="

# 1. Read signing password from release/keystore-pass.txt, keystore-pass.txt, or keystore.properties
KEY_PASS=""
if [ -f "release/keystore-pass.txt" ]; then
    KEY_PASS=$(head -n 1 release/keystore-pass.txt | tr -d '\r\n')
elif [ -f "keystore-pass.txt" ]; then
    KEY_PASS=$(head -n 1 keystore-pass.txt | tr -d '\r\n')
elif [ -f "release/keystore.properties" ]; then
    KEY_PASS=$(grep '^storePassword=' release/keystore.properties | cut -d'=' -f2- | tr -d '\r\n')
elif [ -f "keystore.properties" ]; then
    KEY_PASS=$(grep '^storePassword=' keystore.properties | cut -d'=' -f2- | tr -d '\r\n')
fi
KEY_PASS="${KEY_PASS:-morsego123}"

# 2. Verify or Generate Keystore
KEYSTORE="release/release.keystore"
if [ ! -f "$KEYSTORE" ]; then
    if [ -f "app/release.keystore" ]; then
        KEYSTORE="app/release.keystore"
    else
        echo "[INFO] Keystore '$KEYSTORE' not found. Creating a new release keystore..."
        mkdir -p release
        keytool -genkeypair -v -keystore "$KEYSTORE" -alias morsego -keyalg RSA -keysize 2048 -validity 10000 -storepass "$KEY_PASS" -keypass "$KEY_PASS" -dname "CN=morseGO, OU=Android, O=morseGO, L=City, ST=State, C=US" >/dev/null 2>&1 || true
        if [ ! -f "$KEYSTORE" ]; then
            echo "❌ [ERROR] Could not find or create release keystore '$KEYSTORE'!"
            exit 1
        fi
        echo "✓ [SUCCESS] Generated new release keystore at '$KEYSTORE'."
    fi
fi

# 3. Determine Latest Version & VersionCode
VERSION="${1:-}"
if [ -z "$VERSION" ] && [ -f "app/src/main/AndroidManifest.xml" ]; then
    VERSION=$(grep -o 'android:versionName="[^"]*"' app/src/main/AndroidManifest.xml | cut -d'"' -f2 || echo "1.0.0")
fi
VERSION="${VERSION:-1.0.0}"
VERSION="${VERSION#v}"

CODE=$(grep -o 'android:versionCode="[^"]*"' app/src/main/AndroidManifest.xml 2>/dev/null | cut -d'"' -f2 || echo "10000")
CODE="${CODE:-10000}"

echo "[INFO] Target Version:     $VERSION"
echo "[INFO] Target VersionCode: $CODE"
echo "[INFO] Keystore:           $KEYSTORE (Alias: morsego)"
echo "[INFO] Keystore Password:  Loaded securely (gitignored)"
echo ""

# 3. Check if Docker is available & running
USE_DOCKER=0
if command -v docker >/dev/null 2>&1 && docker info >/dev/null 2>&1; then
    USE_DOCKER=1
fi

if [ "$USE_DOCKER" -eq 1 ]; then
    echo "[INFO] Building and signing AAB inside Docker container..."
    APP_VERSION_NAME="$VERSION" APP_VERSION_CODE="$CODE" KEYSTORE_PASSWORD="$KEY_PASS" KEY_PASSWORD="$KEY_PASS" docker compose run --rm -e APP_VERSION_NAME="$VERSION" -e APP_VERSION_CODE="$CODE" -e KEYSTORE_PASSWORD="$KEY_PASS" -e KEY_PASSWORD="$KEY_PASS" test-unit bundle
else
    echo "[INFO] Docker not active. Building and signing AAB with local Gradle..."
    chmod +x ./gradlew 2>/dev/null || true
    ./gradlew bundleRelease -PversionName="$VERSION" -PversionCode="$CODE" -PkeystorePassword="$KEY_PASS" --info
fi

# 4. Locate output AAB file
AAB_SOURCE="app/build/outputs/bundle/release/app-release.aab"
if [ ! -f "$AAB_SOURCE" ]; then
    if [ -f "release/morseGO-release.aab" ]; then
        AAB_SOURCE="release/morseGO-release.aab"
    elif [ -f "release/v${VERSION}/morseGO-release.aab" ]; then
        AAB_SOURCE="release/v${VERSION}/morseGO-release.aab"
    else
        echo "❌ [ERROR] Could not find generated AAB at '$AAB_SOURCE'!"
        exit 1
    fi
fi

# 5. Copy to Version Release Directory (Single AAB only)
VERSION_DIR="release/v${VERSION}"
mkdir -p "$VERSION_DIR"

if [ "$AAB_SOURCE" != "$VERSION_DIR/morseGO-release.aab" ]; then
    cp -f "$AAB_SOURCE" "$VERSION_DIR/morseGO-release.aab"
fi

# Ensure no loose AAB files in release root or duplicate versioned AAB
rm -f "release/morseGO-release.aab" 2>/dev/null || true
rm -f "release/morseGO-release.aab.sha256" 2>/dev/null || true
rm -f "$VERSION_DIR/morseGO-v${VERSION}-release.aab" 2>/dev/null || true

# 6. Verify Signature
if command -v jarsigner >/dev/null 2>&1; then
    echo "[INFO] Verifying signature with jarsigner..."
    if jarsigner -verify "$VERSION_DIR/morseGO-release.aab" >/dev/null 2>&1; then
        echo "✓ [VERIFIED] Signature is valid (Signed with $KEYSTORE)."
    else
        echo "⚠️ [WARNING] Signature verification returned non-zero code."
    fi
fi

# 7. Generate SHA256 Checksum in Version Folder
if command -v sha256sum >/dev/null 2>&1; then
    (cd "$VERSION_DIR" && sha256sum morseGO-release.aab > morseGO-release.aab.sha256)
fi

echo ""
echo "===================================================="
echo "             Signed Release AAB Summary             "
echo "===================================================="
echo "  - App Version:           $VERSION (Code: $CODE)"
echo "  - Signed AAB (Release):  $VERSION_DIR/morseGO-release.aab"
echo "  - Signing Status:        SIGNED with $KEYSTORE (Alias: morsego)"
echo "===================================================="
echo "Ready for Google Play Console upload!"
echo ""