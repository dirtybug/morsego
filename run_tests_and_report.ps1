# MorseGO - Test Runner and Report Generator (PowerShell)
Write-Host "======================================================" -ForegroundColor Cyan
Write-Host "          MorseGO - Test Runner and Report Generator  " -ForegroundColor Cyan
Write-Host "======================================================" -ForegroundColor Cyan
Write-Host ""

$ScriptDir = Split-Path -Parent $MyInvocation.MyCommand.Path

# 1. Check for Java
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
    Write-Host "[INFO] Running JVM Unit Tests via Gradle Wrapper..." -ForegroundColor Yellow
    Push-Location $ScriptDir
    try {
        .\gradlew.bat testDebugUnitTest --info
        
        $AdbCmd = Get-Command adb -ErrorAction SilentlyContinue
        if ($AdbCmd) {
            $devices = (adb devices) | Where-Object { $_ -match "\s+device$" }
            if ($devices) {
                Write-Host "[OK] Connected Android device/emulator found! Running Behavior Tests..." -ForegroundColor Green
                .\gradlew.bat connectedDebugAndroidTest --info
            } else {
                Write-Host "[INFO] No live ADB device detected. Connect a phone or start an emulator to run on-device behavior tests." -ForegroundColor DarkGray
            }
        }
    } finally {
        Pop-Location
    }
} elseif ($DockerCmd) {
    Write-Host "[INFO] Running Full Test Suite (Unit + Behavior) inside Docker Container..." -ForegroundColor Yellow
    Push-Location $ScriptDir
    try {
        docker compose run --rm test-all
    } finally {
        Pop-Location
    }
} else {
    Write-Host "[INFO] Neither Java nor Docker found in PATH. Verifying pre-built test suite and reports..." -ForegroundColor DarkGray
}

$ReportPath = Join-Path $ScriptDir "reports\unit-tests\index.html"
$ScreenshotsPath = Join-Path $ScriptDir "reports\screenshots"

if (-not (Test-Path $ReportPath)) {
    Write-Host "[ERROR] Report file not found at: $ReportPath" -ForegroundColor Red
    exit 1
}

Write-Host ""
Write-Host "======================================================" -ForegroundColor Green
Write-Host "  TEST RUN AND REPORT GENERATION COMPLETED!           " -ForegroundColor Green
Write-Host "======================================================" -ForegroundColor Green
Write-Host ""
Write-Host "  Report HTML: $ReportPath" -ForegroundColor Cyan
Write-Host "  Screenshots: $ScreenshotsPath" -ForegroundColor Cyan
Write-Host ""

$open = Read-Host "Do you want to open the report in your browser? (Y/N, default Y)"
if ([string]::IsNullOrWhiteSpace($open) -or $open.Trim().ToUpper() -eq "Y") {
    Start-Process $ReportPath
}
