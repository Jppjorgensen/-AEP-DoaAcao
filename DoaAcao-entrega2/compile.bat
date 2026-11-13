@echo off
cd /d "%~dp0"
if exist bin rmdir /s /q bin
mkdir bin
javac -encoding UTF-8 --release 17 -cp "lib/*" -sourcepath src -d bin src\br\com\doaacao\App.java
if errorlevel 1 goto fim
echo Compilado com sucesso. Agora rode: run.bat
:fim
pause
