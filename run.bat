@echo off
cd /d "%~dp0"
java -cp "bin;lib/*" App %*
pause
