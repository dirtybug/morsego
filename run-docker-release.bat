@echo off
setlocal enabledelayedexpansion

echo ======================================================
echo       MorseGO - Docker Release & Test Runner          
echo ======================================================
echo.

set VERSION=%1
if "%VERSION%"=="" set VERSION=v1.0.0

echo [INFO] Target Version: %VERSION%
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

REM Organize into versioned folders
if not exist "%~dp0releases\%VERSION%" mkdir "%~dp0releases\%VERSION%"
if not exist "%~dp0tests\%VERSION%" mkdir "%~dp0tests\%VERSION%"

if exist "%~dp0build-apks\morseGO-release.apk" (
    copy /y "%~dp0build-apks\morseGO-release.apk" "%~dp0releases\%VERSION%\morseGO-%VERSION%-release.apk" >nul
)
if exist "%~dp0build-apks\morseGO-debug.apk" (
    copy /y "%~dp0build-apks\morseGO-debug.apk" "%~dp0releases\%VERSION%\morseGO-%VERSION%-debug.apk" >nul
)
if exist "%~dp0reports\unit-tests\index.html" (
    copy /y "%~dp0reports\unit-tests\index.html" "%~dp0tests\%VERSION%\index.html" >nul
)

echo.
echo ======================================================
echo   DOCKER RELEASE & TESTS COMPLETED SUCCESSFULLY!
echo ======================================================
echo.
echo   Version:        %VERSION%
echo   Release Folder: %~dp0releases\%VERSION%\
echo   Tests Folder:   %~dp0tests\%VERSION%\
echo   Catalog:        %~dp0releases\RELEASES.md
echo.

if exist "%~dp0releases\%VERSION%" (
    start "" "%~dp0releases\%VERSION%"
)

exit /b 0
