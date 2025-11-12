@echo off
rem git_pull (remote do local).bat
REM ============================================
REM  Atualiza o repositório local (Git Pull)
REM ============================================
title Git Pull - EcoSonic
color 0a

echo --------------------------------------------
echo   Atualizando repositório local... download!
echo --------------------------------------------
echo.

REM Certifica-se de estar na pasta do projeto
cd /d "%~dp0"

REM Verifica se é um repositório Git
if not exist ".git" (
    echo ERRO: Este diretório não é um repositório Git.
    pause
    exit /b
)

pause

git fetch --all
git pull

if %errorlevel%==0 (
    echo.
    echo ✅ Repositório atualizado com sucesso!
) else (
    echo.
    echo ❌ Erro ao atualizar o repositório.
)

echo.
pause
