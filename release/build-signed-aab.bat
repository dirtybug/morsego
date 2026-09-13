@echo off
REM ==============================================================================
REM  morseGO - Automated Release AAB Builder & Signer for Google Play Store
REM  Location: .\release\build-signed-aab.bat
REM  Builds and signs .aab and saves it into the version release folder.
REM ==============================================================================
setlocal enabledelayedexpansion

echo ====================================================
echo        morseGO - Signed Release AAB Builder
echo ====================================================

set SCRIPT_DIR=%~dp0
cd /d "%SCRIPT_DIR%.."

REM 1. Read signing password from release\keystore-pass.txt, keystore-pass.txt, or keystore.properties
set KEY_PASS=
if exist "release\keystore-pass.txt" (
    set /p KEY_PASS=<release\keystore-pass.txt
) else if exist "keystore-pass.txt" (
    set /p KEY_PASS=<keystore-pass.txt
) else if exist "release\keystore.properties" (
    for /f "tokens=2 delims==" %%A in ('findstr "storePassword" release\keystore.properties') do set KEY_PASS=%%A
) else if exist "keystore.properties" (
    for /f "tokens=2 delims==" %%A in ('findstr "storePassword" keystore.properties') do set KEY_PASS=%%A
)
if "%KEY_PASS%"=="" set KEY_PASS=morsego123

REM 2. Verify or Generate Keystore
set KEYSTORE_PATH=release\release.keystore
if not exist "%KEYSTORE_PATH%" (
    if exist "app\release.keystore" (
        set KEYSTORE_PATH=app\release.keystore
    ) else (
        echo [INFO] Keystore '%KEYSTORE_PATH%' not found. Creating a new release keystore...
        if not exist "release" mkdir "release"
        keytool -genkeypair -v -keystore "%KEYSTORE_PATH%" -alias morsego -keyalg RSA -keysize 2048 -validity 10000 -storepass "%KEY_PASS%" -keypass "%KEY_PASS%" -dname "CN=morseGO, OU=Android, O=morseGO, L=City, ST=State, C=US" >nul 2>nul
        if not exist "%KEYSTORE_PATH%" (
            echo [ERROR] Could not find or create release keystore '%KEYSTORE_PATH%'!
            exit /b 1
        )
        echo [SUCCESS] Generated new release keystore at '%KEYSTORE_PATH%'.
    )
)

REM 3. Determine Latest Version & VersionCode
set VERSION=%~1
if "%VERSION%"=="" (
    for /f tokens^=2^ delims^=^"^" %%A in ('findstr "android:versionName" app\src\main\AndroidManifest.xml') do (
        set VERSION=%%A
    )
)

if "%VERSION%"=="" set VERSION=1.0.0
if "%VERSION:~0,1%"=="v" set VERSION=%VERSION:~1%

set CODE=
for /f tokens^=2^ delims^=^"^" %%A in ('findstr "android:versionCode" app\src\main\AndroidManifest.xml') do (
    set CODE=%%A
)
if "%CODE%"=="" set CODE=10000

echo [INFO] Target Version:     %VERSION%
echo [INFO] Target VersionCode: %CODE%
echo [INFO] Keystore:           %KEYSTORE_PATH% [Alias: morsego]
echo [INFO] Keystore Password:  Loaded securely (gitignored)
echo.

REM 3. Check if Docker is available & running
set USE_DOCKER=0
where docker >nul 2>nul
if %errorlevel% equ 0 (
    docker info >nul 2>nul
    if %errorlevel% equ 0 (
        set USE_DOCKER=1
    )
)

if %USE_DOCKER% equ 1 (
    echo [INFO] Building and signing AAB inside Docker container...
    docker compose run --rm -e APP_VERSION_NAME=%VERSION% -e APP_VERSION_CODE=%CODE% -e KEYSTORE_PASSWORD=%KEY_PASS% -e KEY_PASSWORD=%KEY_PASS% test-unit bundle
    if %errorlevel% neq 0 (
        echo [ERROR] Docker AAB build failed.
        exit /b 1
    )
) else (
    echo [INFO] Docker not active. Building and signing AAB with local Gradle...
    call gradlew.bat bundleRelease -PversionName=%VERSION% -PversionCode=%CODE% -PkeystorePassword=%KEY_PASS% --info
    if %errorlevel% neq 0 (
        echo [ERROR] Gradle bundleRelease failed.
        exit /b 1
    )
)

REM 4. Verify output AAB file
set AAB_SOURCE=app\build\outputs\bundle\release\app-release.aab
if not exist "%AAB_SOURCE%" (
    if exist "release\morseGO-release.aab" (
        set AAB_SOURCE=release\morseGO-release.aab
    ) else if exist "release\v%VERSION%\morseGO-release.aab" (
        set AAB_SOURCE=release\v%VERSION%\morseGO-release.aab
    ) else (
        echo [ERROR] Could not find generated AAB at '%AAB_SOURCE%'!
        exit /b 1
    )
)

REM 5. Copy to Version Release Directory (Single AAB only)
set VERSION_DIR=release\v%VERSION%
if not exist "%VERSION_DIR%" mkdir "%VERSION_DIR%"

if not "%AAB_SOURCE%"=="%VERSION_DIR%\morseGO-release.aab" (
    copy /y "%AAB_SOURCE%" "%VERSION_DIR%\morseGO-release.aab" >nul
)

REM Ensure no loose AAB files in release root or duplicate versioned AAB
if exist "release\morseGO-release.aab" del /f /q "release\morseGO-release.aab" >nul 2>nul
if exist "release\morseGO-release.aab.sha256" del /f /q "release\morseGO-release.aab.sha256" >nul 2>nul
if exist "%VERSION_DIR%\morseGO-v%VERSION%-release.aab" del /f /q "%VERSION_DIR%\morseGO-v%VERSION%-release.aab" >nul 2>nul

REM 6. Verify Signature
where jarsigner >nul 2>nul
if %errorlevel% equ 0 (
    echo [INFO] Verifying signature with jarsigner...
    jarsigner -verify "%VERSION_DIR%\morseGO-release.aab" >nul 2>nul
    if %errorlevel% equ 0 (
        echo [VERIFIED] Signature is valid [Signed with %KEYSTORE_PATH%].
    ) else (
        echo [WARNING] Signature verification returned non-zero code.
    )
)

REM 7. Generate SHA256 Checksum in Version Folder
certutil -hashfile "%VERSION_DIR%\morseGO-release.aab" SHA256 > "%VERSION_DIR%\morseGO-release.aab.sha256"

echo.
echo ====================================================
echo             Signed Release AAB Summary
echo ====================================================
echo   - App Version:           %VERSION% [Code: %CODE%]
echo   - Signed AAB (Release):  %VERSION_DIR%\morseGO-release.aab
echo   - SHA256 Checksum:       %VERSION_DIR%\morseGO-release.aab.sha256
echo   - Signing Status:        SIGNED with %KEYSTORE_PATH% [Alias: morsego]
echo ====================================================
echo Ready for Google Play Console upload!
echo.
exit /b 0
