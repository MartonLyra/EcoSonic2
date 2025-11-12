@echo off
REM =======================================================
REM  Inicializa repositório Git e conecta ao GitHub remoto
REM =======================================================
title Git Init Remote
color 0a

echo --------------------------------------------
echo   Inicializando novo repositório Git
echo.
echo Esse quinto script é muito útil quando você inicia um projeto do zero (no seu PC local) e ainda não vinculou o repositório remoto no GitHub.
echo.
echo Esse script vai configurar tudo automaticamente - criar o repositório Git local, conectar ao remoto e fazer o primeiro push inicial.
echo --------------------------------------------
echo.

cd /d "%~dp0"

REM Verifica se já é um repositório Git
if exist ".git" (
    echo Este diretório já é um repositório Git.
    echo Nada a fazer.
    pause
    exit /b
)

REM Passo 1 - Inicializar repositório local
echo Criando repositório local...
git init
if %errorlevel% neq 0 (
    echo ERRO: Falha ao inicializar repositório local.
    pause
    exit /b
)

REM Passo 2 - Solicitar URL do repositório remoto
set /p REMOTE_URL=Digite a URL do repositório remoto (ex: https://github.com/usuario/EcoSonic.git): 
if "%REMOTE_URL%"=="" (
    echo ERRO: Nenhuma URL fornecida.
    pause
    exit /b
)

REM Passo 3 - Adicionar remoto e branch principal
git remote add origin "%REMOTE_URL%"
git branch -M main

REM Passo 4 - Adicionar arquivos e commit inicial
git add .
git commit -m "Commit inicial - configuração do projeto"

REM Passo 5 - Enviar para o GitHub
echo.
echo Enviando commit inicial para o GitHub...
git push -u origin main

if %errorlevel%==0 (
    echo.
    echo ✅ Repositório criado e conectado com sucesso!
    echo 🌍 URL: %REMOTE_URL%
) else (
    echo.
    echo ❌ Falha ao enviar para o repositório remoto.
    echo Verifique se o token de acesso ou permissões estão corretos.
)

echo.
pause
