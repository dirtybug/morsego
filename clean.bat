@echo off
REM morseGO - Limpeza completa de ficheiros temporarios, build, .gradle e .idea
echo ====================================================
echo             morseGO - Limpeza de Workspace          
echo ====================================================
echo.

echo [1/4] A parar processos do Gradle...
call gradlew.bat --stop 2>nul

echo [2/4] A remover pastas de build...
if exist "build" rmdir /s /q "build" 2>nul
if exist "app\build" rmdir /s /q "app\build" 2>nul

echo [3/4] A remover pastas .gradle e .idea...
if exist ".gradle" rmdir /s /q ".gradle" 2>nul
if exist ".idea" rmdir /s /q ".idea" 2>nul

echo [4/4] A remover ficheiros residuais de cache...
del /f /q *.iml 2>nul
del /f /q app\*.iml 2>nul

echo.
echo ====================================================
echo   Workspace limpo com sucesso!
echo   Pastas removidas: .gradle, .idea, build, app/build
echo ====================================================
echo.
