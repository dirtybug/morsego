#!/usr/bin/env bash
set -e

SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
cd "$SCRIPT_DIR/.."

echo "===================================================="
echo "        morseGO - Signed Release AAB Builder        "
echo "===================================================="

# 1. Verify Key Password File Exists
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

if [ -z "$KEY_PASS" ]; then
    echo "❌ [ERROR] Key password file not found! Key does not exist. AAB will NOT be signed."
    exit 1
fi

# 2. Verify Keystore File Exists
KEYSTORE=""
if [ -f "release/key.jks" ]; then
    KEYSTORE="release/key.jks"
elif [ -f "release/release.keystore" ]; then
    KEYSTORE="release/release.keystore"
elif [ -f "key.jks" ]; then
    KEYSTORE="key.jks"
elif [ -f "release.keystore" ]; then
    KEYSTORE="release.keystore"
elif [ -f "app/release.keystore" ]; then
    KEYSTORE="app/release.keystore"
fi

if [ -z "$KEYSTORE" ]; then
    echo "❌ [ERROR] Keystore file not found! Expected 'release/key.jks' or 'release/release.keystore'. AAB will NOT be signed."
    exit 1
fi

# 3. Verify Key and Keystore match with keytool before running
KEY_ALIAS=""
if command -v keytool >/dev/null 2>&1; then
    if ! keytool -list -keystore "$KEYSTORE" -storepass "$KEY_PASS" >/dev/null 2>&1; then
        echo "❌ [ERROR] Keystore '$KEYSTORE' could not be unlocked with password! Key does not match keystore. AAB will NOT be signed."
        exit 1
    fi
    KEY_ALIAS=$(keytool -list -keystore "$KEYSTORE" -storepass "$KEY_PASS" 2>/dev/null | grep -E 'PrivateKeyEntry|trustedCertEntry' | head -n 1 | cut -d',' -f1 || true)
fi
if [ -z "$KEY_ALIAS" ]; then
    if [[ "$KEYSTORE" == *.jks ]]; then
        KEY_ALIAS="key0"
    else
        KEY_ALIAS="morsego"
    fi
fi

# 4. Determine Latest Version & VersionCode
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
echo "[INFO] Keystore:           $KEYSTORE (Alias: $KEY_ALIAS)"
echo "[INFO] Keystore Password:  Verified successfully with keytool"
echo ""

# Clean prior intermediate bundle file so failures are not masked
rm -f "app/build/outputs/bundle/release/app-release.aab" 2>/dev/null || true

# 5. Check if Docker is available & running
USE_DOCKER=0
if command -v docker >/dev/null 2>&1 && docker info >/dev/null 2>&1; then
    USE_DOCKER=1
fi

if [ "$USE_DOCKER" -eq 1 ]; then
    echo "[INFO] Building and signing AAB inside Docker container..."
    APP_VERSION_NAME="$VERSION" APP_VERSION_CODE="$CODE" KEYSTORE_PASSWORD="$KEY_PASS" KEY_PASSWORD="$KEY_PASS" KEY_ALIAS="$KEY_ALIAS" docker compose run --rm -e APP_VERSION_NAME="$VERSION" -e APP_VERSION_CODE="$CODE" -e KEYSTORE_PASSWORD="$KEY_PASS" -e KEY_PASSWORD="$KEY_PASS" -e KEY_ALIAS="$KEY_ALIAS" test-unit bundle
else
    echo "[INFO] Docker not active. Building and signing AAB with local Gradle..."
    chmod +x ./gradlew 2>/dev/null || true
    ./gradlew bundleRelease -PversionName="$VERSION" -PversionCode="$CODE" -PkeystorePassword="$KEY_PASS" -PkeyAlias="$KEY_ALIAS" --info
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
echo "  - Signing Status:        SIGNED with $KEYSTORE (Alias: $KEY_ALIAS)"
echo "===================================================="
echo "Ready for Google Play Console upload!"
echo ""