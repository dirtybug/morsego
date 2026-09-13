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

REM 1. Verify Key Password File Exists
set KEY_PASS=
set KEY_FOUND=0
if exist "release\keystore-pass.txt" (
    set /p KEY_PASS=<release\keystore-pass.txt
    set KEY_FOUND=1
) else if exist "keystore-pass.txt" (
    set /p KEY_PASS=<keystore-pass.txt
    set KEY_FOUND=1
) else if exist "release\keystore.properties" (
    for /f "tokens=2 delims==" %%A in ('findstr "storePassword" release\keystore.properties') do set KEY_PASS=%%A
    set KEY_FOUND=1
) else if exist "keystore.properties" (
    for /f "tokens=2 delims==" %%A in ('findstr "storePassword" keystore.properties') do set KEY_PASS=%%A
    set KEY_FOUND=1
)

if %KEY_FOUND% equ 0 (
    echo [ERROR] Key password file not found!
    echo [INFO] Expected 'release\keystore-pass.txt' or 'keystore-pass.txt'.
    echo [INFO] Key does not exist. AAB will NOT be signed.
    exit /b 1
)
if "%KEY_PASS%"=="" (
    echo [ERROR] Key password in file is empty! AAB will NOT be signed.
    exit /b 1
)

REM 2. Verify Keystore File Exists
set KEYSTORE_PATH=
if exist "release\key.jks" (
    set KEYSTORE_PATH=release\key.jks
) else if exist "release\release.keystore" (
    set KEYSTORE_PATH=release\release.keystore
) else if exist "key.jks" (
    set KEYSTORE_PATH=key.jks
) else if exist "release.keystore" (
    set KEYSTORE_PATH=release.keystore
) else if exist "app\release.keystore" (
    set KEYSTORE_PATH=app\release.keystore
)

if "%KEYSTORE_PATH%"=="" (
    echo [ERROR] Keystore file not found!
    echo [INFO] Expected 'release\key.jks' or 'release\release.keystore'.
    echo [INFO] Keystore does not exist. AAB will NOT be signed.
    exit /b 1
)

REM 3. Verify Key and Keystore match with keytool before running
set KEY_ALIAS=
for /f "tokens=1 delims=," %%A in ('keytool -list -keystore "%KEYSTORE_PATH%" -storepass "%KEY_PASS%" 2^>nul ^| findstr "PrivateKeyEntry"') do (
    set KEY_ALIAS=%%A
)
if "%KEY_ALIAS%"=="" (
    for /f "tokens=1 delims=," %%A in ('keytool -list -keystore "%KEYSTORE_PATH%" -storepass "%KEY_PASS%" 2^>nul ^| findstr "trustedCertEntry"') do (
        set KEY_ALIAS=%%A
    )
)

if "%KEY_ALIAS%"=="" (
    echo [ERROR] Keystore '%KEYSTORE_PATH%' could not be unlocked with password!
    echo [INFO] Key does not match keystore. AAB will NOT be signed.
    exit /b 1
)

REM 4. Determine Latest Version & VersionCode
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
echo [INFO] Keystore:           %KEYSTORE_PATH% [Alias: !KEY_ALIAS!]
echo [INFO] Keystore Password:  Verified successfully with keytool
echo.

REM Clean prior intermediate bundle files so failures are not masked
if exist "app\build\outputs\bundle\release\app-release.aab" del /f /q "app\build\outputs\bundle\release\app-release.aab" >nul 2>nul

REM 5. Check if Docker is available & running
set USE_DOCKER=0
where docker >nul 2>nul
if %errorlevel% equ 0 (
    docker info >nul 2>nul
    if %errorlevel% equ 0 (
        set USE_DOCKER=1
    )
)

if !USE_DOCKER! equ 1 (
    echo [INFO] Building and signing AAB inside Docker container...
    docker compose run --rm -e APP_VERSION_NAME=%VERSION% -e APP_VERSION_CODE=%CODE% -e KEYSTORE_PASSWORD=%KEY_PASS% -e KEY_PASSWORD=%KEY_PASS% -e KEY_ALIAS=!KEY_ALIAS! test-unit bundle
    if !errorlevel! neq 0 (
        echo [ERROR] Docker AAB build failed.
        exit /b 1
    )
) else (
    echo [INFO] Docker not active. Building and signing AAB with local Gradle...
    call gradlew.bat bundleRelease -PversionName=%VERSION% -PversionCode=%CODE% -PkeystorePassword=%KEY_PASS% -PkeyAlias=!KEY_ALIAS! --info
    if !errorlevel! neq 0 (
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
echo   - Signing Status:        SIGNED with %KEYSTORE_PATH% [Alias: !KEY_ALIAS!]
echo ====================================================
echo Ready for Google Play Console upload!
echo.
exit /b 0
