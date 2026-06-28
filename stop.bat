@echo off
taskkill /F /PID 37532 >nul 2>&1
timeout /t 3 /nobreak >nul
echo killed