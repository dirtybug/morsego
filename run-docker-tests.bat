@echo off
REM morseGO - Executar testes no Docker Container
echo ====================================================
echo             morseGO - Docker Test Runner             
echo ====================================================

where docker >nul 2>nul
if %errorlevel% neq 0 (
    echo [ERRO] Docker nao encontrado no PATH. Por favor, instale ou inicie o Docker Desktop.
    exit /b 1
)

set TARGET=%1
if "%TARGET%"=="" set TARGET=unit

echo Executando container Docker para o alvo: %TARGET%...
if "%TARGET%"=="unit" (
    docker compose run --rm test-unit
) else if "%TARGET%"=="build" (
    docker compose run --rm build-apk
) else if "%TARGET%"=="release" (
    docker compose run --rm build-release
) else if "%TARGET%"=="instrumented" (
    docker compose run --rm test-instrumented
) else if "%TARGET%"=="all" (
    docker compose run --rm test-all
) else (
    docker compose run --rm test-unit %TARGET%
)

echo.
echo Relatorios disponiveis na pasta: .\reports\
echo Screenshots (se aplicavel): .\screenshots\
echo Concluido!
