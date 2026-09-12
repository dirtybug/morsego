# morseGO - Limpeza completa de ficheiros temporarios, build, .gradle e .idea
Write-Host "====================================================" -ForegroundColor Cyan
Write-Host "            morseGO - Limpeza de Workspace          " -ForegroundColor Cyan
Write-Host "====================================================" -ForegroundColor Cyan
Write-Host ""

Write-Host "[1/4] A parar processos do Gradle..." -ForegroundColor Yellow
if (Test-Path ".\gradlew.bat") {
    .\gradlew.bat --stop 2>$null
}

Write-Host "[2/4] A remover pastas de build..." -ForegroundColor Yellow
@("build", "app\build") | ForEach-Object {
    if (Test-Path $_) { Remove-Item -Path $_ -Recurse -Force -ErrorAction SilentlyContinue }
}

Write-Host "[3/4] A remover pastas .gradle e .idea..." -ForegroundColor Yellow
@(".gradle", ".idea") | ForEach-Object {
    if (Test-Path $_) { Remove-Item -Path $_ -Recurse -Force -ErrorAction SilentlyContinue }
}

Write-Host "[4/4] A remover ficheiros residuais..." -ForegroundColor Yellow
Get-ChildItem -Filter "*.iml" -Recurse -Depth 2 -ErrorAction SilentlyContinue | Remove-Item -Force -ErrorAction SilentlyContinue

Write-Host "`n====================================================" -ForegroundColor Cyan
Write-Host "  Workspace limpo com sucesso!" -ForegroundColor Green
Write-Host "  Pastas removidas: .gradle, .idea, build, app/build" -ForegroundColor White
Write-Host "====================================================" -ForegroundColor Cyan
