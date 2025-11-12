@echo off
REM ============================================
REM  Exibe status do repositório
REM ============================================
title Git Status - EcoSonic
color 0e

echo Mostra o estado atual dos arquivos — ótimo antes de dar push (upload)

cd /d "%~dp0"

if not exist ".git" (
    echo ERRO: Este diretório não é um repositório Git.
    pause
    exit /b
)

echo --------------------------------------------
echo   STATUS DO REPOSITÓRIO
echo --------------------------------------------
echo.

git status

echo.
pause
