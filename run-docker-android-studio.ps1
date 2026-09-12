# morseGO - Executar Android Studio com GUI no Docker (PowerShell)
param(
    [string]$Action = "start"
)

Write-Host "====================================================" -ForegroundColor Cyan
Write-Host "     morseGO - Android Studio GUI Docker Runner     " -ForegroundColor Cyan
Write-Host "====================================================" -ForegroundColor Cyan
Write-Host ""

if (-not (Get-Command docker -ErrorAction SilentlyContinue)) {
    Write-Error "O Docker não foi encontrado no PATH. Por favor, instale e inicie o Docker Desktop."
    exit 1
}

$ErrorActionPreference = "SilentlyContinue"
$dockerInfo = docker info 2>&1
$ErrorActionPreference = "Continue"

if ($LASTEXITCODE -ne 0) {
    Write-Error "O Docker Desktop não está em execução. Abra o Docker Desktop e aguarde o status 'Engine running'."
    exit 1
}

switch ($Action.ToLower()) {
    "stop" {
        Write-Host "[INFO] Parando o container do Android Studio..." -ForegroundColor Yellow
        docker compose stop android-studio
        Write-Host "[INFO] Container parado com sucesso!" -ForegroundColor Green
        exit 0
    }
    "logs" {
        Write-Host "[INFO] Exibindo logs do Android Studio..." -ForegroundColor Yellow
        docker compose logs -f android-studio
        exit 0
    }
    "build" {
        Write-Host "[INFO] Reconstruindo imagem Docker do Android Studio..." -ForegroundColor Yellow
        docker compose build android-studio
        exit 0
    }
    "restart" {
        Write-Host "[INFO] Reiniciando container do Android Studio..." -ForegroundColor Yellow
        docker compose restart android-studio
        Start-Sleep -Seconds 3
        $appUrl = "http://localhost:6080/vnc.html?autoconnect=true&resize=remote"
        if (Get-Command vncviewer -ErrorAction SilentlyContinue) {
            Start-Process vncviewer -ArgumentList "localhost:5900"
        } elseif (Test-Path "C:\Program Files (x86)\Microsoft\Edge\Application\msedge.exe") {
            Start-Process "C:\Program Files (x86)\Microsoft\Edge\Application\msedge.exe" -ArgumentList "--app=`"$appUrl`"", "--window-name=`"Android Studio - morseGO`"", "--window-size=1600,950"
        } elseif (Test-Path "C:\Program Files\Google\Chrome\Application\chrome.exe") {
            Start-Process "C:\Program Files\Google\Chrome\Application\chrome.exe" -ArgumentList "--app=`"$appUrl`"", "--window-name=`"Android Studio - morseGO`"", "--window-size=1600,950"
        } else {
            Start-Process $appUrl
        }
        exit 0
    }
    default {
        Write-Host "[1/3] Iniciando o container Docker do Android Studio GUI..." -ForegroundColor Yellow
        docker compose up -d android-studio

        if ($LASTEXITCODE -ne 0) {
            Write-Host "`n[AVISO] Imagem não encontrada. Construindo imagem morsego-android-studio..." -ForegroundColor Yellow
            docker compose build android-studio
            docker compose up -d android-studio
        }

        Write-Host "`n[2/3] Aguardando inicialização do servidor gráfico noVNC..." -ForegroundColor Yellow
        Start-Sleep -Seconds 4

        Write-Host "`n[3/3] Abrindo a Janela da Interface Gráfica (Modo UI)..." -ForegroundColor Green
        $appUrl = "http://localhost:6080/vnc.html?autoconnect=true&resize=remote"

        if (Get-Command vncviewer -ErrorAction SilentlyContinue) {
            Start-Process vncviewer -ArgumentList "localhost:5900"
        } elseif (Test-Path "C:\Program Files (x86)\Microsoft\Edge\Application\msedge.exe") {
            Start-Process "C:\Program Files (x86)\Microsoft\Edge\Application\msedge.exe" -ArgumentList "--app=`"$appUrl`"", "--window-name=`"Android Studio - morseGO`"", "--window-size=1600,950"
        } elseif (Test-Path "C:\Program Files\Google\Chrome\Application\chrome.exe") {
            Start-Process "C:\Program Files\Google\Chrome\Application\chrome.exe" -ArgumentList "--app=`"$appUrl`"", "--window-name=`"Android Studio - morseGO`"", "--window-size=1600,950"
        } else {
            Start-Process $appUrl
        }

        Write-Host "`n====================================================" -ForegroundColor Cyan
        Write-Host "          Android Studio Pronto e Carregado!        " -ForegroundColor Green
        Write-Host "====================================================" -ForegroundColor Cyan
        Write-Host "  [Interface Web noVNC]" -ForegroundColor White
        Write-Host "  - URL no Navegador:     http://localhost:6080/" -ForegroundColor Yellow
        Write-Host "  - Auto-Conexão Direta:  http://localhost:6080/vnc.html?autoconnect=true&resize=remote" -ForegroundColor Yellow
        Write-Host ""
        Write-Host "  [Cliente VNC Nativo]" -ForegroundColor White
        Write-Host "  - Host / Porta:         localhost:5900 (sem senha)" -ForegroundColor Yellow
        Write-Host ""
        Write-Host "  [Projeto morseGO]" -ForegroundColor White
        Write-Host "  - Localização montada:  /workspace (morseGO)" -ForegroundColor Yellow
        Write-Host "  - Status:               Carregado automaticamente pelo studio.sh" -ForegroundColor Yellow
        Write-Host "  - SDK Android:          /opt/android-sdk (API 34, build-tools 34.0.0)" -ForegroundColor Yellow
        Write-Host ""
        Write-Host "  [Comandos Úteis]" -ForegroundColor White
        Write-Host "  - Parar container:      .\run-docker-android-studio.ps1 -Action stop" -ForegroundColor Gray
        Write-Host "  - Ver logs:             .\run-docker-android-studio.ps1 -Action logs" -ForegroundColor Gray
        Write-Host "  - Reiniciar:            .\run-docker-android-studio.ps1 -Action restart" -ForegroundColor Gray
        Write-Host "====================================================" -ForegroundColor Cyan
    }
}
