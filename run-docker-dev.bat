@echo off
REM morseGO - Iniciar Container de Desenvolvimento Interativo
echo ====================================================
echo        morseGO - Docker Dev Environment             
echo ====================================================

where docker >nul 2>nul
if %errorlevel% neq 0 (
    echo [ERRO] Docker nao encontrado no PATH. Por favor, instale o Docker Desktop.
    exit /b 1
)

docker info >nul 2>nul
if %errorlevel% neq 0 (
    echo [ERRO] O Docker Desktop nao esta em execucao ou a engine ainda esta iniciando.
    echo Por favor, abra o Docker Desktop e aguarde o status "Engine running" antes de iniciar.
    exit /b 1
)

echo Iniciando container de desenvolvimento interativo...
echo Digite 'exit' para sair do container.
echo.
docker compose run --rm dev
