@echo off
REM morseGO - Executar Android Studio com GUI no Docker
echo ====================================================
echo      morseGO - Android Studio GUI Docker Runner     
echo ====================================================
echo.

where docker >nul 2>nul
if %errorlevel% neq 0 (
    echo [ERRO] O Docker nao foi encontrado no PATH.
    echo Por favor, instale e inicie o Docker Desktop.
    exit /b 1
)

docker info >nul 2>nul
if %errorlevel% neq 0 (
    echo [ERRO] O Docker Desktop nao esta em execucao ou a engine ainda esta iniciando.
    echo Por favor, abra o Docker Desktop e aguarde o status "Engine running".
    exit /b 1
)

set ACTION=%1
if "%ACTION%"=="" set ACTION=start

if "%ACTION%"=="stop" (
    echo [INFO] Parando o container do Android Studio...
    docker compose stop android-studio
    echo [INFO] Container parado com sucesso!
    exit /b 0
)

if "%ACTION%"=="logs" (
    echo [INFO] Exibindo logs do Android Studio...
    docker compose logs -f android-studio
    exit /b 0
)

if "%ACTION%"=="build" (
    echo [INFO] Reconstruindo imagem Docker do Android Studio...
    docker compose build android-studio
    exit /b 0
)

if "%ACTION%"=="restart" (
    echo [INFO] Reiniciando Android Studio...
    docker compose restart android-studio
    ping 127.0.0.1 -n 4 >nul
    goto :open_ui
)

echo [1/3] Iniciando o container Docker do Android Studio GUI...
docker compose up -d android-studio

if %errorlevel% neq 0 (
    echo.
    echo [AVISO] Falha ao iniciar com imagem pre-construida.
    echo [INFO] A construir imagem morsego-android-studio... Isto pode demorar alguns minutos na primeira vez.
    docker compose build android-studio
    docker compose up -d android-studio
)

echo.
echo [2/3] Aguardando inicializacao do servidor grafico noVNC...
ping 127.0.0.1 -n 5 >nul

:open_ui
echo.
echo [3/3] Abrindo a Janela da Interface Grafica (Modo UI)...

set "APP_URL=http://localhost:6080/vnc.html?autoconnect=true&resize=remote"

REM 1. Verificar se existe cliente VNC nativo instalado
where vncviewer >nul 2>nul
if %errorlevel% equ 0 goto :launch_vnc

REM 2. Verificar executaveis de navegadores para modo janela de app dedicada
set "EDGE_EXE=C:\Program Files (x86)\Microsoft\Edge\Application\msedge.exe"
if exist "%EDGE_EXE%" goto :launch_edge

set "EDGE_EXE_64=C:\Program Files\Microsoft\Edge\Application\msedge.exe"
if exist "%EDGE_EXE_64%" goto :launch_edge_64

set "CHROME_EXE=C:\Program Files\Google\Chrome\Application\chrome.exe"
if exist "%CHROME_EXE%" goto :launch_chrome

set "CHROME_EXE_X86=C:\Program Files (x86)\Google\Chrome\Application\chrome.exe"
if exist "%CHROME_EXE_X86%" goto :launch_chrome_x86

REM 3. Fallback padrao
echo [UI] Abrindo janela de interface grafica...
start "" "%APP_URL%"
goto :ui_launched

:launch_vnc
echo [UI] Iniciando cliente VNC nativo na porta 5900...
start "" vncviewer localhost:5900
goto :ui_launched

:launch_edge
echo [UI] Abrindo janela dedicada do Android Studio no Edge App Mode...
start "" "%EDGE_EXE%" --app="%APP_URL%" --window-name="Android Studio - morseGO" --window-size=1600,950
goto :ui_launched

:launch_edge_64
echo [UI] Abrindo janela dedicada do Android Studio no Edge App Mode...
start "" "%EDGE_EXE_64%" --app="%APP_URL%" --window-name="Android Studio - morseGO" --window-size=1600,950
goto :ui_launched

:launch_chrome
echo [UI] Abrindo janela dedicada do Android Studio no Chrome App Mode...
start "" "%CHROME_EXE%" --app="%APP_URL%" --window-name="Android Studio - morseGO" --window-size=1600,950
goto :ui_launched

:launch_chrome_x86
echo [UI] Abrindo janela dedicada do Android Studio no Chrome App Mode...
start "" "%CHROME_EXE_X86%" --app="%APP_URL%" --window-name="Android Studio - morseGO" --window-size=1600,950
goto :ui_launched

:ui_launched

echo.
echo ====================================================
echo           Android Studio Pronto e Carregado!        
echo ====================================================
echo.
echo   [Interface Web noVNC]
echo   - URL no Navegador:     http://localhost:6080/
echo   - Auto-Conexao Direta:  http://localhost:6080/vnc.html?autoconnect=true^&resize=remote
echo.
echo   [Cliente VNC Nativo]
echo   - Host / Porta:         localhost:5900 (sem senha)
echo.
echo   [Projeto morseGO]
echo   - Localizacao montada:  /workspace (morseGO)
echo   - Status:               Carregado automaticamente pelo studio.sh
echo   - SDK Android:          /opt/android-sdk (API 34, build-tools 34.0.0)
echo.
echo   [Comandos Uteis]
echo   - Parar container:      run-docker-android-studio.bat stop
echo   - Ver logs:             run-docker-android-studio.bat logs
echo   - Reiniciar:            run-docker-android-studio.bat restart
echo ====================================================
echo.
