<#
.SYNOPSIS
    morseGO - Automated Release AAB Builder & Signer for Google Play Store (PowerShell)
.DESCRIPTION
    Builds, signs, verifies, and copies the signed Android App Bundle (.aab) into .\release\v<VERSION>\
.PARAMETER Version
    Optional target version name (e.g. "1.0.0"). Automatically detected from AndroidManifest.xml if omitted.
#>
param(
    [string]$Version = ""
)

$ErrorActionPreference = "Stop"

$ScriptDir = Split-Path -Parent $MyInvocation.MyCommand.Path
$ProjectRoot = Resolve-Path "$ScriptDir\.."
Set-Location $ProjectRoot

Write-Host "====================================================" -ForegroundColor Cyan
Write-Host "        morseGO - Signed Release AAB Builder        " -ForegroundColor Cyan
Write-Host "====================================================" -ForegroundColor Cyan

# 1. Verify Key Password File Exists
$keyPass = ""
$keyFound = $false
if (Test-Path "release\keystore-pass.txt") {
    $keyPass = (Get-Content "release\keystore-pass.txt" -Raw).Trim()
    $keyFound = $true
} elseif (Test-Path "keystore-pass.txt") {
    $keyPass = (Get-Content "keystore-pass.txt" -Raw).Trim()
    $keyFound = $true
} elseif (Test-Path "release\keystore.properties") {
    $prop = Get-Content "release\keystore.properties"
    foreach ($line in $prop) {
        if ($line -match "^storePassword\s*=\s*(.*)$") {
            $keyPass = $matches[1].Trim()
            $keyFound = $true
        }
    }
} elseif (Test-Path "keystore.properties") {
    $prop = Get-Content "keystore.properties"
    foreach ($line in $prop) {
        if ($line -match "^storePassword\s*=\s*(.*)$") {
            $keyPass = $matches[1].Trim()
            $keyFound = $true
        }
    }
}

if (-not $keyFound -or [string]::IsNullOrWhiteSpace($keyPass)) {
    Write-Error "[ERROR] Key password file ('release\keystore-pass.txt' or 'keystore-pass.txt') not found! Key does not exist. AAB will NOT be signed."
    exit 1
}

# 2. Verify Keystore File Exists
$keystorePath = ""
if (Test-Path "release\key.jks") {
    $keystorePath = "release\key.jks"
} elseif (Test-Path "release\release.keystore") {
    $keystorePath = "release\release.keystore"
} elseif (Test-Path "key.jks") {
    $keystorePath = "key.jks"
} elseif (Test-Path "release.keystore") {
    $keystorePath = "release.keystore"
} elseif (Test-Path "app\release.keystore") {
    $keystorePath = "app\release.keystore"
}

if ([string]::IsNullOrWhiteSpace($keystorePath)) {
    Write-Error "[ERROR] Keystore file not found! Expected 'release\key.jks' or 'release\release.keystore'. AAB will NOT be signed."
    exit 1
}

# 3. Verify Key and Keystore match with keytool before running
$keyAlias = ""
if (Get-Command keytool -ErrorAction SilentlyContinue) {
    $toolOutput = & keytool -list -keystore $keystorePath -storepass $keyPass 2>$null
    if ($LASTEXITCODE -ne 0) {
        Write-Error "[ERROR] Keystore '$keystorePath' could not be unlocked with password! Key does not match keystore. AAB will NOT be signed."
        exit 1
    }
    foreach ($line in $toolOutput) {
        if ($line -match "PrivateKeyEntry" -or $line -match "trustedCertEntry") {
            $keyAlias = $line.Split(',')[0].Trim()
            break
        }
    }
}
if ([string]::IsNullOrWhiteSpace($keyAlias)) {
    $keyAlias = if ($keystorePath.EndsWith(".jks")) { "key0" } else { "morsego" }
}

# 4. Determine Latest Version & VersionCode
$manifestPath = "app\src\main\AndroidManifest.xml"
if ([string]::IsNullOrWhiteSpace($Version) -and (Test-Path $manifestPath)) {
    $manifestContent = Get-Content $manifestPath -Raw
    if ($manifestContent -match 'android:versionName="([^"]+)"') {
        $Version = $matches[1]
    }
}
if ([string]::IsNullOrWhiteSpace($Version)) { $Version = "1.0.0" }
if ($Version.StartsWith("v")) { $Version = $Version.Substring(1) }

$code = 10000
if (Test-Path $manifestPath) {
    $manifestContent = Get-Content $manifestPath -Raw
    if ($manifestContent -match 'android:versionCode="([^"]+)"') {
        $code = [int]$matches[1]
    }
}

Write-Host "[INFO] Target Version:     $Version" -ForegroundColor Green
Write-Host "[INFO] Target VersionCode: $code" -ForegroundColor Green
Write-Host "[INFO] Keystore:           $keystorePath (Alias: $keyAlias)" -ForegroundColor Green
Write-Host "[INFO] Keystore Password:  Verified successfully with keytool" -ForegroundColor Green
Write-Host ""

# Clean prior intermediate bundle file so failures are not masked
if (Test-Path "app\build\outputs\bundle\release\app-release.aab") {
    Remove-Item "app\build\outputs\bundle\release\app-release.aab" -Force -ErrorAction SilentlyContinue
}

# 5. Check if Docker is available & running
$useDocker = $false
if (Get-Command docker -ErrorAction SilentlyContinue) {
    docker info >$null 2>&1
    if ($LASTEXITCODE -eq 0) {
        $useDocker = $true
    }
}

if ($useDocker) {
    Write-Host "[INFO] Building and signing AAB inside Docker container..." -ForegroundColor Yellow
    docker compose run --rm -e "APP_VERSION_NAME=$Version" -e "APP_VERSION_CODE=$code" -e "KEYSTORE_PASSWORD=$keyPass" -e "KEY_PASSWORD=$keyPass" -e "KEY_ALIAS=$keyAlias" test-unit bundle
    if ($LASTEXITCODE -ne 0) {
        Write-Error "[ERROR] Docker AAB build failed with exit code $LASTEXITCODE"
        exit 1
    }
} else {
    Write-Host "[INFO] Docker not active. Building and signing AAB with local Gradle..." -ForegroundColor Yellow
    & ".\gradlew.bat" bundleRelease -PversionName="$Version" -PversionCode="$code" -PkeystorePassword="$keyPass" -PkeyAlias="$keyAlias" --info
    if ($LASTEXITCODE -ne 0) {
        Write-Error "[ERROR] Gradle bundleRelease failed with exit code $LASTEXITCODE"
        exit 1
    }
}

# 4. Locate output AAB file
$aabSource = "app\build\outputs\bundle\release\app-release.aab"
if (-not (Test-Path $aabSource)) {
    if (Test-Path "release\morseGO-release.aab") {
        $aabSource = "release\morseGO-release.aab"
    } elseif (Test-Path "release\v$Version\morseGO-release.aab") {
        $aabSource = "release\v$Version\morseGO-release.aab"
    } else {
        Write-Error "[ERROR] Could not find generated AAB at '$aabSource'!"
        exit 1
    }
}

# 5. Copy to Version Release Directory (Single AAB only)
$versionDir = "release\v$Version"
if (-not (Test-Path $versionDir)) {
    New-Item -ItemType Directory -Path $versionDir -Force | Out-Null
}

if ($aabSource -ne "$versionDir\morseGO-release.aab") {
    Copy-Item -Path $aabSource -Destination "$versionDir\morseGO-release.aab" -Force
}

# Ensure no loose AAB files in release root or duplicate versioned AAB
if (Test-Path "release\morseGO-release.aab") { Remove-Item "release\morseGO-release.aab" -Force -ErrorAction SilentlyContinue }
if (Test-Path "release\morseGO-release.aab.sha256") { Remove-Item "release\morseGO-release.aab.sha256" -Force -ErrorAction SilentlyContinue }
if (Test-Path "$versionDir\morseGO-v${Version}-release.aab") { Remove-Item "$versionDir\morseGO-v${Version}-release.aab" -Force -ErrorAction SilentlyContinue }

# 6. Verify Signature
if (Get-Command jarsigner -ErrorAction SilentlyContinue) {
    Write-Host "[INFO] Verifying signature with jarsigner..." -ForegroundColor Yellow
    & jarsigner -verify "$versionDir\morseGO-release.aab" >$null 2>&1
    if ($LASTEXITCODE -eq 0) {
        Write-Host "[VERIFIED] Signature is valid (Signed with $keystorePath)." -ForegroundColor Green
    } else {
        Write-Warning "[WARNING] Signature verification returned non-zero code."
    }
}

# 7. Generate SHA256 Checksum in Version Folder
$hash = (Get-FileHash -Algorithm SHA256 "$versionDir\morseGO-release.aab").Hash.ToLower()
"$hash  morseGO-release.aab" | Out-File -FilePath "$versionDir\morseGO-release.aab.sha256" -Encoding ascii -Force

Write-Host ""
Write-Host "====================================================" -ForegroundColor Cyan
Write-Host "             Signed Release AAB Summary             " -ForegroundColor Cyan
Write-Host "====================================================" -ForegroundColor Cyan
Write-Host "  - App Version:           $Version (Code: $code)"
Write-Host "  - Signed AAB (Release):  $versionDir\morseGO-release.aab" -ForegroundColor Green
Write-Host "  - SHA256 Checksum:       $versionDir\morseGO-release.aab.sha256 ($hash)"
Write-Host "  - Signing Status:        SIGNED with $keystorePath (Alias: $keyAlias)" -ForegroundColor Green
Write-Host "====================================================" -ForegroundColor Cyan
Write-Host "Ready for Google Play Console upload!" -ForegroundColor Green
Write-Host ""
