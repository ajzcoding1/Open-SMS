@echo off
title OpenSMS Runner
cd /d "%~dp0app"
echo Starting OpenSMS...
"..\.venv\Scripts\python.exe" -u main.py
pause
