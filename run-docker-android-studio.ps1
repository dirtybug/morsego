# morseGO - Executar Android Studio com GUI Nativa (sem VNC) (PowerShell)
param(
    [string]$Action = "start"
)

Write-Host "====================================================" -ForegroundColor Cyan
Write-Host "     morseGO - Android Studio GUI Runner (Nativo)   " -ForegroundColor Cyan
Write-Host "====================================================" -ForegroundColor Cyan
Write-Host ""

if ($Action.ToLower() -eq "stop") {
    Write-Host "[INFO] Fechando Android Studio..." -ForegroundColor Yellow
    wsl.exe -d Ubuntu killall -9 studio.sh java 2>$null
    docker compose stop android-studio 2>$null
    Write-Host "[INFO] Android Studio fechado com sucesso!" -ForegroundColor Green
    exit 0
}

if (-not (Get-Command wsl -ErrorAction SilentlyContinue)) {
    Write-Error "WSL2 / WSLg não foi encontrado no PATH."
    exit 1
}

Write-Host "[1/2] Preparando ambiente gráfico nativo WSLg (sem VNC)..." -ForegroundColor Yellow
Write-Host "[2/2] Abrindo Android Studio em janela nativa do Windows..." -ForegroundColor Green

Start-Process -FilePath "wsl.exe" -ArgumentList "-d", "Ubuntu", "env", "ANDROID_HOME=/opt/android-sdk", "PATH=/opt/android-sdk/platform-tools:/opt/android-sdk/cmdline-tools/latest/bin:$env:PATH", "/opt/android-studio/bin/studio.sh", "/mnt/c/Users/JúlioAndrade/morseGo"

Write-Host "`n====================================================" -ForegroundColor Cyan
Write-Host "     Android Studio Aberto em Janela Nativa!        " -ForegroundColor Green
Write-Host "====================================================" -ForegroundColor Cyan
Write-Host "  - Modo:       Interface Gráfica Nativa Windows (WSLg)" -ForegroundColor White
Write-Host "  - Sem VNC:    Janela direta do Windows (sem browser)" -ForegroundColor White
Write-Host "  - Projeto:    morseGO (/workspace)" -ForegroundColor Yellow
Write-Host "  - SDK:        /opt/android-sdk (API 34)" -ForegroundColor Yellow
Write-Host "====================================================" -ForegroundColor Cyan

