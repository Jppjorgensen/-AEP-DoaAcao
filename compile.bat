@echo off
cd /d "%~dp0"
if not exist bin mkdir bin
javac -encoding UTF-8 --release 17 -d bin src\App.java
pause
