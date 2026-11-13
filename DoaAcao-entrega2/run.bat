@echo off
cd /d "%~dp0"
java -cp "bin;lib/*" br.com.doaacao.App %*
pause
