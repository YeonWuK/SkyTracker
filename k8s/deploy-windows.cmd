@echo off
setlocal

powershell.exe -NoProfile -ExecutionPolicy Bypass -File "%~dp0deploy-windows.ps1" %*

if errorlevel 1 (
  echo.
  echo Deploy failed.
  pause
  exit /b %errorlevel%
)

echo.
echo Deploy finished.
pause
