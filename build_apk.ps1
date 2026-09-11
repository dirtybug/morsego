# MorseGO - Android APK Builder (PowerShell)
Write-Host "======================================================" -ForegroundColor Cyan
Write-Host "          MorseGO - Android APK Builder               " -ForegroundColor Cyan
Write-Host "======================================================" -ForegroundColor Cyan
Write-Host ""

$ScriptDir = Split-Path -Parent $MyInvocation.MyCommand.Path
$BuildApksDir = Join-Path $ScriptDir "build-apks"
if (-not (Test-Path $BuildApksDir)) {
    New-Item -ItemType Directory -Path $BuildApksDir | Out-Null
}

# 1. Locate Java if not set
if (-not $env:JAVA_HOME) {
    if (Test-Path "C:\Program Files\Android\Android Studio\jbr\bin\java.exe") {
        $env:JAVA_HOME = "C:\Program Files\Android\Android Studio\jbr"
        $env:PATH = "$env:JAVA_HOME\bin;$env:PATH"
        Write-Host "[INFO] Located Android Studio JBR at: $env:JAVA_HOME" -ForegroundColor Green
    }
}

$JavaCmd = Get-Command java -ErrorAction SilentlyContinue
$DockerCmd = Get-Command docker -ErrorAction SilentlyContinue

if ($JavaCmd) {
    Write-Host "[INFO] Building APK via Gradle Wrapper..." -ForegroundColor Yellow
    Push-Location $ScriptDir
    try {
        .\gradlew.bat assembleDebug --info
        $sourceApk = Join-Path $ScriptDir "app\build\outputs\apk\debug\app-debug.apk"
        if (Test-Path $sourceApk) {
            $destApk = Join-Path $BuildApksDir "morseGO-debug.apk"
            Copy-Item -Path $sourceApk -Destination $destApk -Force
            Write-Host "[OK] morseGO-debug.apk saved to build-apks\" -ForegroundColor Green
        }
    } finally {
        Pop-Location
    }
} elseif ($DockerCmd) {
    Write-Host "[INFO] Building APK inside Docker Container..." -ForegroundColor Yellow
    Push-Location $ScriptDir
    try {
        docker compose run --rm build-apk
    } finally {
        Pop-Location
    }
} else {
    Write-Host "[ERROR] Neither Java nor Docker found in PATH." -ForegroundColor Red
    exit 1
}

Write-Host ""
Write-Host "======================================================" -ForegroundColor Green
Write-Host "  BUILD COMPLETED SUCCESSFULLY!                       " -ForegroundColor Green
Write-Host "======================================================" -ForegroundColor Green
Write-Host ""
Write-Host "  Output Directory: $BuildApksDir" -ForegroundColor Cyan
Get-ChildItem $BuildApksDir | Format-Table Name, Length, LastWriteTime

$open = Read-Host "Do you want to open the build-apks folder in Explorer? (Y/N, default Y)"
if ([string]::IsNullOrWhiteSpace($open) -or $open.Trim().ToUpper() -eq "Y") {
    explorer.exe $BuildApksDir
}
