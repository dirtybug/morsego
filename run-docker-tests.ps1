# morseGO - Executar testes no Docker Container (PowerShell)
param(
    [string]$Target = "unit"
)

Write-Host "====================================================" -ForegroundColor Cyan
Write-Host "            morseGO - Docker Test Runner            " -ForegroundColor Cyan
Write-Host "====================================================" -ForegroundColor Cyan

if (-not (Get-Command docker -ErrorAction SilentlyContinue)) {
    Write-Error "Docker não foi encontrado no PATH. Por favor, instale ou inicie o Docker Desktop."
    exit 1
}

Write-Host "Alvo selecionado: $Target" -ForegroundColor Yellow

switch ($Target.ToLower()) {
    "unit" {
        docker compose run --rm test-unit
    }
    "build" {
        docker compose run --rm build-apk
    }
    "release" {
        docker compose run --rm build-release
    }
    "instrumented" {
        docker compose run --rm test-instrumented
    }
    "all" {
        docker compose run --rm test-all
    }
    default {
        docker compose run --rm test-unit $Target
    }
}

Write-Host "`nTestes finalizados!" -ForegroundColor Green
Write-Host "Relatórios e APKs salvos em: .\release\development\" -ForegroundColor White

