# morseGO - Iniciar Container de Desenvolvimento Interativo
Write-Host "====================================================" -ForegroundColor Cyan
Write-Host "        morseGO - Docker Dev Environment             " -ForegroundColor Cyan
Write-Host "====================================================" -ForegroundColor Cyan

if (-not (Get-Command docker -ErrorAction SilentlyContinue)) {
    Write-Host "[ERRO] Docker nao encontrado no PATH. Por favor, instale o Docker Desktop." -ForegroundColor Red
    exit 1
}

$dockerInfo = docker info 2>&1
if ($LASTEXITCODE -ne 0) {
    Write-Host "[ERRO] O Docker Desktop nao esta em execucao ou a engine ainda esta iniciando." -ForegroundColor Red
    Write-Host "Por favor, abra o Docker Desktop e aguarde o status 'Engine running'." -ForegroundColor Yellow
    exit 1
}

Write-Host "Iniciando container de desenvolvimento interativo..." -ForegroundColor Green
Write-Host "Digite 'exit' para sair do container." -ForegroundColor Yellow
Write-Host ""
docker compose run --rm dev
