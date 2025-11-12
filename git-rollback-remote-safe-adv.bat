@echo off
title Git Rollback - Remote SAFE (Avancado)
color 0E

echo =======================================================
echo            GIT ROLLBACK REMOTO - MODO SEGURO - 1 Commit
echo =======================================================
echo.
echo Este script cria um novo commit que desfaz um outro commit existente,
echo SEM apagar o historico. Isso e o metodo mais SEGURO de reverter
echo alteracoes ja enviadas ao GitHub.
echo.
echo O commit original permanecera visivel, mas sera "anulado" por um novo.
echo.
pause

:: Certifica que estamos na pasta correta
if not exist ".git" (
    echo ERRO: Nenhum repositorio Git encontrado nesta pasta.
    echo Execute este script dentro da pasta do seu projeto.
    pause
    exit /b
)

echo.
echo ======= Obtendo os ultimos commits =======
echo.

:: Mostra os ultimos 5 commits (hash curto, autor e mensagem)
git --no-pager log -5 --pretty=format:"%%h - %%an - %%s"

echo.
echo Digite o HASH do commit que deseja reverter (ex: a1b2c3d4):
set /p commitHash="> "

:: Verifica se algo foi digitado
if "%commitHash%"=="" (
    echo Nenhum hash informado. Operacao cancelada.
    pause
    exit /b
)

:: Mostra detalhes do commit escolhido
echo.
echo ======= Informacoes do commit selecionado =======
git show --oneline --no-pager %commitHash%

echo.
echo Este commit sera revertido criando um novo commit "inverso".
echo O historico sera preservado e nenhum commit sera apagado.
echo.
set /p confirm="Deseja realmente reverter este commit (S/N)? "
if /I "%confirm%" NEQ "S" (
    echo Operacao cancelada.
    pause
    exit /b
)

echo.
echo Revertendo commit %commitHash%...
git revert %commitHash%
if %ERRORLEVEL% NEQ 0 (
    echo ERRO: Nao foi possivel reverter o commit. Verifique conflitos.
    pause
    exit /b
)

echo.
set /p confirmPush="Deseja enviar (push) esta reversao ao GitHub agora (S/N)? "
if /I "%confirmPush%"=="S" (
    echo Enviando commit de reversao para o GitHub...
    git push
    echo.
    echo Reversao enviada com sucesso!
) else (
    echo.
    echo Reversao concluida localmente, mas NAO foi enviada.
    echo Execute "git push" manualmente se desejar publicar.
)

echo.
echo ======= Historico apos a reversao =======
git --no-pager log -3 --pretty=format:"%%h - %%an - %%s"

echo.
echo Operacao concluida com sucesso.
pause
