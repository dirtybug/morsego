@echo off
REM morseGO - Executar Android Studio com GUI Nativa (sem VNC)
echo ====================================================
echo      morseGO - Android Studio GUI Runner (Nativo)    
echo ====================================================
echo.

set ACTION=%1
if "%ACTION%"=="" set ACTION=start

if "%ACTION%"=="stop" (
    echo [INFO] Fechando Android Studio...
    wsl.exe -d Ubuntu killall -9 studio.sh java 2>nul
    docker compose stop android-studio 2>nul
    echo [INFO] Android Studio fechado com sucesso!
    exit /b 0
)

where wsl >nul 2>nul
if %errorlevel% neq 0 (
    echo [ERRO] WSL2 / WSLg nao foi encontrado no PATH.
    exit /b 1
)

echo [1/2] A preparar ambiente grafico nativo WSLg (sem VNC)...
echo [2/2] A abrir Android Studio em janela nativa do Windows...
start "" wsl.exe -d Ubuntu env ANDROID_HOME=/opt/android-sdk PATH="/opt/android-sdk/platform-tools:/opt/android-sdk/cmdline-tools/latest/bin:$PATH" /opt/android-studio/bin/studio.sh /mnt/c/Users/JúlioAndrade/morseGo

echo.
echo ====================================================
echo     Android Studio Aberto em Janela Nativa!        
echo ====================================================
echo  - Modo:       Interface Grafica Nativa Windows (WSLg)
echo  - Sem VNC:    Janela direta do Windows (sem browser)
echo  - Projeto:    morseGO (/workspace)
echo  - SDK:        /opt/android-sdk (API 34)
echo ====================================================
echo.
