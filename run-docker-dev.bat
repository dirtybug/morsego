@echo off
REM morseGO - Start Interactive Development Container
echo ====================================================
echo        morseGO - Docker Dev Environment             
echo ====================================================

where docker >nul 2>nul
if %errorlevel% neq 0 (
    echo [ERROR] Docker not found in PATH. Please install Docker Desktop.
    exit /b 1
)

docker info >nul 2>nul
if %errorlevel% neq 0 (
    echo [ERROR] Docker Desktop is not running or the engine is still initializing.
    echo Please open Docker Desktop and wait for "Engine running" status before starting.
    exit /b 1
)

echo Starting interactive development container...
echo Type 'exit' to leave container.
echo.
docker compose run --rm dev
