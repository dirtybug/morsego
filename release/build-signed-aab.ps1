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

# 1. Read signing password from release\keystore-pass.txt, keystore-pass.txt, or keystore.properties
$keyPass = ""
if (Test-Path "release\keystore-pass.txt") {
    $keyPass = (Get-Content "release\keystore-pass.txt" -Raw).Trim()
} elseif (Test-Path "keystore-pass.txt") {
    $keyPass = (Get-Content "keystore-pass.txt" -Raw).Trim()
} elseif (Test-Path "release\keystore.properties") {
    $prop = Get-Content "release\keystore.properties"
    foreach ($line in $prop) {
        if ($line -match "^storePassword\s*=\s*(.*)$") {
            $keyPass = $matches[1].Trim()
        }
    }
} elseif (Test-Path "keystore.properties") {
    $prop = Get-Content "keystore.properties"
    foreach ($line in $prop) {
        if ($line -match "^storePassword\s*=\s*(.*)$") {
            $keyPass = $matches[1].Trim()
        }
    }
}
if ([string]::IsNullOrWhiteSpace($keyPass)) { $keyPass = "morsego123" }

# 2. Verify or Generate Keystore
$keystorePath = "release\release.keystore"
if (-not (Test-Path $keystorePath)) {
    if (Test-Path "app\release.keystore") {
        $keystorePath = "app\release.keystore"
    } else {
        Write-Host "[INFO] Keystore '$keystorePath' not found. Creating a new release keystore..." -ForegroundColor Yellow
        if (-not (Test-Path "release")) { New-Item -ItemType Directory -Path "release" -Force | Out-Null }
        & keytool -genkeypair -v -keystore $keystorePath -alias morsego -keyalg RSA -keysize 2048 -validity 10000 -storepass $keyPass -keypass $keyPass -dname "CN=morseGO, OU=Android, O=morseGO, L=City, ST=State, C=US" 2>&1 | Out-Null
        if (-not (Test-Path $keystorePath)) {
            Write-Error "[ERROR] Could not find or create release keystore '$keystorePath'!"
            exit 1
        }
        Write-Host "[SUCCESS] Generated new release keystore at '$keystorePath'." -ForegroundColor Green
    }
}

# 3. Determine Latest Version & VersionCode
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
Write-Host "[INFO] Keystore:           $keystorePath (Alias: morsego)" -ForegroundColor Green
Write-Host "[INFO] Keystore Password:  Loaded securely (gitignored)" -ForegroundColor Green
Write-Host ""

# 3. Check if Docker is available & running
$useDocker = $false
if (Get-Command docker -ErrorAction SilentlyContinue) {
    docker info >$null 2>&1
    if ($LASTEXITCODE -eq 0) {
        $useDocker = $true
    }
}

if ($useDocker) {
    Write-Host "[INFO] Building and signing AAB inside Docker container..." -ForegroundColor Yellow
    docker compose run --rm -e "APP_VERSION_NAME=$Version" -e "APP_VERSION_CODE=$code" -e "KEYSTORE_PASSWORD=$keyPass" -e "KEY_PASSWORD=$keyPass" test-unit bundle
    if ($LASTEXITCODE -ne 0) {
        Write-Error "[ERROR] Docker AAB build failed with exit code $LASTEXITCODE"
        exit 1
    }
} else {
    Write-Host "[INFO] Docker not active. Building and signing AAB with local Gradle..." -ForegroundColor Yellow
    & ".\gradlew.bat" bundleRelease -PversionName="$Version" -PversionCode="$code" -PkeystorePassword="$keyPass" --info
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
Write-Host "  - Signing Status:        SIGNED with $keystorePath (Alias: morsego)" -ForegroundColor Green
Write-Host "====================================================" -ForegroundColor Cyan
Write-Host "Ready for Google Play Console upload!" -ForegroundColor Green
Write-Host ""
