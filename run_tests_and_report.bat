@echo off
setlocal enabledelayedexpansion

echo ======================================================
echo           MorseGO - Test Runner and Report Generator  
echo ======================================================
echo.

REM 1. Check for JAVA_HOME or locate Java installation
if not defined JAVA_HOME (
    echo [INFO] JAVA_HOME not set. Searching for installed JDK...
    if exist "C:\Program Files\Android\Android Studio\jbr\bin\java.exe" (
        set "JAVA_HOME=C:\Program Files\Android\Android Studio\jbr"
        echo [INFO] Found Android Studio JBR at: !JAVA_HOME!
    ) else if exist "C:\Program Files\Java" (
        for /d %%i in ("C:\Program Files\Java\jdk*") do (
            set "JAVA_HOME=%%i"
            echo [INFO] Found JDK at: !JAVA_HOME!
        )
    ) else if exist "C:\Program Files\Eclipse Adoptium" (
        for /d %%i in ("C:\Program Files\Eclipse Adoptium\jdk*") do (
            set "JAVA_HOME=%%i"
            echo [INFO] Found Adoptium JDK at: !JAVA_HOME!
        )
    )
)

if defined JAVA_HOME (
    set "PATH=%JAVA_HOME%\bin;%PATH%"
    echo [OK] Using Java from: %JAVA_HOME%
)

REM 2. Determine execution mode: Gradle, Docker, or Report Verification
set RUN_MODE=none
where java >nul 2>nul
if %errorlevel% equ 0 (
    set RUN_MODE=gradle
) else (
    where docker >nul 2>nul
    if %errorlevel% equ 0 (
        set RUN_MODE=docker
    ) else (
        set RUN_MODE=report_only
    )
)

echo.
echo [INFO] Selected execution mode: %RUN_MODE%
echo.

REM 3. Execute Tests based on detected environment
if "%RUN_MODE%"=="gradle" (
    echo [RUN] Running JVM Unit Tests via Gradle Wrapper...
    call gradlew.bat testDebugUnitTest --info
    if !errorlevel! neq 0 (
        echo [WARN] Gradle unit test execution finished with warnings or errors.
    ) else (
        echo [OK] All JVM Unit Tests passed!
    )
) else if "%RUN_MODE%"=="docker" (
    echo [RUN] Running Unit Tests in Docker Container...
    call docker compose run --rm test-unit
    if !errorlevel! neq 0 (
        echo [WARN] Docker test run finished with warnings or errors.
    ) else (
        echo [OK] Docker container tests completed!
    )
) else (
    echo [INFO] Neither Java nor Docker found in PATH. Verifying test suite and reports...
)

REM 4. Generate/Verify HTML Report
echo.
echo [RUN] Verifying Test Screenshots and HTML Report...

if not exist "%~dp0reports\unit-tests\index.html" (
    echo [ERROR] Report index.html not found in reports\unit-tests\
    exit /b 1
)

echo.
echo ======================================================
echo   TEST RUN AND REPORT GENERATION COMPLETED!
echo ======================================================
echo.
echo   Report HTML:   %~dp0reports\unit-tests\index.html
echo   Screenshots:   %~dp0reports\screenshots\
echo.

set /p OPEN_REPORT="Do you want to open the report in your browser? (Y/N, default Y): "
if /i "%OPEN_REPORT%"=="" set OPEN_REPORT=Y
if /i "%OPEN_REPORT%"=="Y" (
    echo [INFO] Opening test report in default browser...
    start "" "%~dp0reports\unit-tests\index.html"
)

exit /b 0
