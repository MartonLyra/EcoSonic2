@echo off
REM =======================================================
REM  Upload seguro para o GitHub (Pull + Commit + Push)
REM =======================================================
title Git Push Seguro - EcoSonic
color 0b

echo --------------------------------------------
echo   Upload seguro para o GitHub (EcoSonic)
echo --------------------------------------------
echo.

cd /d "%~dp0"

if not exist ".git" (
    echo ERRO: Este diretório não é um repositório Git.
    pause
    exit /b
)

REM Exibe o branch atual
for /f "delims=" %%b in ('git rev-parse --abbrev-ref HEAD') do set BRANCH=%%b
echo Branch atual: %BRANCH%
echo.

REM Passo 1 - Atualiza repositório remoto antes de enviar
echo Fazendo git pull para sincronizar...
git pull origin %BRANCH%

if %errorlevel% neq 0 (
    echo.
    echo ❌ Erro ao sincronizar com o GitHub. Corrija antes de enviar.
    pause
    exit /b
)

REM Passo 2 - Mostra status atual
echo.
git status
echo.

REM Passo 3 - Pede comentário do commit
set /p COMMIT_MSG=Digite a mensagem de commit: 
if "%COMMIT_MSG%"=="" set COMMIT_MSG=Atualização automática

REM Passo 4 - Adiciona e envia
echo.
echo Adicionando arquivos modificados...
git add .

echo Criando commit...
git commit -m "%COMMIT_MSG%"

echo.
echo Enviando alterações para o GitHub...
git push origin %BRANCH%

if %errorlevel%==0 (
    echo.
    echo ✅ Upload concluído com sucesso!
) else (
    echo.
    echo ❌ O envio falhou. Verifique a conexão ou conflitos.
)

echo.
pause
