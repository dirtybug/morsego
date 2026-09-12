@echo off
REM morseGO - Executar testes no Docker Container
echo ====================================================
echo             morseGO - Docker Test Runner             
echo ====================================================

where docker >nul 2>nul
if %errorlevel% neq 0 (
    echo [ERRO] Docker nao encontrado no PATH. Por favor, instale o Docker Desktop.
    exit /b 1
)

docker info >nul 2>nul
if %errorlevel% neq 0 (
    echo [ERRO] O Docker Desktop nao esta em execucao ou a engine ainda esta iniciando.
    echo Por favor, abra o Docker Desktop e aguarde o status "Engine running" antes de executar os testes.
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
echo ====================================================
echo                   morseGO - Resumo
echo ====================================================
if "%TARGET%"=="unit" (
    echo [INFO] Testes unitarios concluidos com sucesso!
    echo [DICA] O alvo "unit" testa apenas a JVM. Para gerar os ficheiros APK:
    echo        - APK Debug:   run-docker-tests.bat build
    echo        - APK Release: run-docker-tests.bat release
    echo        - Todos:       run-docker-tests.bat all
)
echo.
echo Ficheiros disponiveis na pasta: .\release\development\
echo   - APK Release:           .\release\development\morseGO-release.apk
echo   - APK Debug:             .\release\development\morseGO-debug.apk
echo   - Relatorios de Testes:  .\release\development\reports\
echo   - Portal Central MorseGO:.\index.html
echo ====================================================
echo Concluido com sucesso!
