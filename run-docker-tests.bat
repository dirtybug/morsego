@echo off
REM morseGO - Run tests in Docker Container
echo ====================================================
echo             morseGO - Docker Test Runner             
echo ====================================================

set TARGET=%1
if "%TARGET%"=="" set TARGET=all

where docker >nul 2>nul
if %errorlevel% neq 0 (
    echo [ERROR] Docker not found in PATH. Please install Docker Desktop.
    exit /b 1
)

docker info >nul 2>nul
if %errorlevel% equ 0 goto :DOCKER_READY

where wsl >nul 2>nul
if %errorlevel% equ 0 (
    echo [INFO] Windows Docker Desktop not active. Running via WSL Docker engine...
    wsl -d Ubuntu --cd "%~dp0" -e ./run-docker-tests.sh %TARGET%
    exit /b %errorlevel%
)

echo [ERROR] Docker Desktop is not running or the engine is still initializing.
echo Please open Docker Desktop and wait for "Engine running" status before running tests.
exit /b 1

:DOCKER_READY

echo Running Docker container for target: %TARGET%...
if "%TARGET%"=="clean" (
    echo [INFO] Cleaning Gradle cache, .gradle, .idea, and build folders...
    call clean.bat
    exit /b 0
) else if "%TARGET%"=="screenshots" (
    docker compose run --rm test-unit screenshots
) else if "%TARGET%"=="unit" (
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
echo                   morseGO - Summary
echo ====================================================
if "%TARGET%"=="all" (
    echo [INFO] Full test suite, screenshots, and fresh APKs built successfully!
) else if "%TARGET%"=="unit" (
    echo [INFO] Unit tests and screenshot generation completed successfully!
    echo [TIP] Additional available commands:
    echo        - Screenshots: run-docker-tests.bat screenshots
    echo        - APK Debug:   run-docker-tests.bat build
    echo        - APK Release: run-docker-tests.bat release
    echo        - All:         run-docker-tests.bat all
) else if "%TARGET%"=="build" (
    echo [INFO] Debug APK built successfully!
) else if "%TARGET%"=="release" (
    echo [INFO] Release APK built successfully!
)
echo.
echo Available files in directory: .\release\development\
if exist ".\release\development\morseGO-release.aab" (
    echo   - AAB Release:           .\release\development\morseGO-release.aab
)
if exist ".\release\development\morseGO-release.apk" (
    echo   - APK Release:           .\release\development\morseGO-release.apk
) else (
    echo   - APK Release:           [Not built - run: run-docker-tests.bat release]
)
if exist ".\release\development\morseGO-debug.apk" (
    echo   - APK Debug:             .\release\development\morseGO-debug.apk
) else (
    echo   - APK Debug:             [Not built - run: run-docker-tests.bat build]
)
if exist ".\release\development\screenshots\" (
    echo   - Screenshots:           .\release\development\screenshots\
)
if exist ".\release\development\reports\" (
    echo   - Test Reports:          .\release\development\reports\
)
if exist ".\index.html" (
    echo.
    echo Central Portal:            .\index.html
)
echo ====================================================
echo Completed successfully!
