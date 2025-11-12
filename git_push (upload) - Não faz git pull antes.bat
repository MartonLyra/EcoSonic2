@echo off
REM ============================================
REM  Envia as alterações locais para o GitHub
REM  Faz o upload das alterações para o GitHub.
REM  - Adiciona todos os arquivos alterados;
REM  - Pede um comentário antes de enviar;
REM  - Mostra progresso e confirma o envio.
REM ============================================
title Git Push - Upload
color 0b

echo ---------------------------------------------
echo   Subindo alterações para o GitHub... upload!
echo ATENÇÃO: ESSA VERSÃO NÃO BAIXA ANTES, PODENDO CAUSAR CONFLITOS.
echo ---------------------------------------------
echo.

cd /d "%~dp0"

if not exist ".git" (
    echo ERRO: Este diretório não é um repositório Git.
    pause
    exit /b
)

set /p COMMIT_MSG=Digite a mensagem de commit: 
if "%COMMIT_MSG%"=="" set COMMIT_MSG=Atualização automática

echo.
echo Adicionando arquivos modificados...
git add .

echo.
echo Gravando commit: "%COMMIT_MSG%"
git commit -m "%COMMIT_MSG%"

echo.
echo Enviando alterações para o GitHub...

rem Para hipótese do repositório se chamar 'main':
git push origin main

rem Para hipótese do repositório se chamar 'master' ou outro nome do branch:
rem git push origin master


if %errorlevel%==0 (
    echo.
    echo ✅ Upload concluído com sucesso!
) else (
    echo.
    echo ❌ Erro ao enviar alterações.
)

echo.
pause
