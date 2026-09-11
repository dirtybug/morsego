@echo off
setlocal enabledelayedexpansion

echo ======================================================
echo           MorseGO - Android APK Builder               
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

REM 2. Determine execution mode: Gradle or Docker
set BUILD_MODE=none
where java >nul 2>nul
if %errorlevel% equ 0 (
    set BUILD_MODE=gradle
) else (
    where docker >nul 2>nul
    if %errorlevel% equ 0 (
        set BUILD_MODE=docker
    ) else (
        set BUILD_MODE=error
    )
)

echo.
echo [INFO] Selected build engine: %BUILD_MODE%
echo.

if not exist "%~dp0build-apks" mkdir "%~dp0build-apks"

REM 3. Execute Build
if "%BUILD_MODE%"=="gradle" (
    echo [RUN] Building Debug APK with Gradle Wrapper...
    call gradlew.bat assembleDebug --info
    if !errorlevel! neq 0 (
        echo [ERROR] Gradle APK build failed!
        exit /b 1
    )
    
    echo [RUN] Copying APK to build-apks directory...
    if exist "%~dp0app\build\outputs\apk\debug\app-debug.apk" (
        copy /y "%~dp0app\build\outputs\apk\debug\app-debug.apk" "%~dp0build-apks\morseGO-debug.apk" >nul
        echo [OK] morseGO-debug.apk saved to build-apks\
    )
) else if "%BUILD_MODE%"=="docker" (
    echo [RUN] Building APK inside Docker Container...
    call docker compose run --rm build-apk
    if !errorlevel! neq 0 (
        echo [ERROR] Docker APK build failed!
        exit /b 1
    )
) else (
    echo [ERROR] Neither Java/JDK nor Docker was found in PATH.
    echo [INFO] Please install Android Studio, JDK 17, or Docker Desktop to build the APK.
    exit /b 1
)

echo.
echo ======================================================
echo   BUILD COMPLETED SUCCESSFULLY!
echo ======================================================
echo.
if exist "%~dp0build-apks\morseGO-debug.apk" echo   APK File:   %~dp0build-apks\morseGO-debug.apk
if exist "%~dp0build-apks\app-debug.apk" echo   APK File:   %~dp0build-apks\app-debug.apk
echo   APKs folder: %~dp0build-apks\
echo.

set /p OPEN_EXPLORER="Do you want to open the build-apks folder in Explorer? (Y/N, default Y): "
if /i "%OPEN_EXPLORER%"=="" set OPEN_EXPLORER=Y
if /i "%OPEN_EXPLORER%"=="Y" (
    explorer.exe "%~dp0build-apks"
)

exit /b 0
