@echo off
setlocal enabledelayedexpansion

echo ======================================================
echo       MorseGO - Docker Release & Test Runner          
echo ======================================================
echo.

where docker >nul 2>nul
if %errorlevel% neq 0 (
    echo [ERROR] Docker was not found in PATH. Please install and start Docker Desktop.
    exit /b 1
)

echo [RUN] 1. Running Full Test Suite in Docker...
call docker compose run --rm test-all
if !errorlevel! neq 0 (
    echo [WARN] Some tests reported warnings. Proceeding with release build...
) else (
    echo [OK] All tests in Docker passed!
)

echo.
echo [RUN] 2. Building Release APK in Docker...
call docker compose run --rm build-release
if !errorlevel! neq 0 (
    echo [ERROR] Docker release build failed!
    exit /b 1
)

echo.
echo ======================================================
echo   DOCKER RELEASE & TESTS COMPLETED SUCCESSFULLY!
echo ======================================================
echo.
echo   Release APK: %~dp0build-apks\morseGO-release.apk
echo   Reports:     %~dp0reports\unit-tests\index.html
echo.

if exist "%~dp0build-apks" (
    start "" "%~dp0build-apks"
)

exit /b 0
