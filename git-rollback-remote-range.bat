@echo off
title Git Rollback - Remote SAFE (Varios Commits)
color 0E

echo =====================================================
echo           GIT ROLLBACK REMOTO - VARIOS COMMITS
echo =====================================================
echo.
echo Este modo cria UM NOVO COMMIT que desfaz VARIOS commits anteriores,
echo de forma SEGURA, SEM apagar historico e SEM usar push forçado.
echo.
echo Use este metodo para desfazer multiplas mudancas que ja foram
echo enviadas ao GitHub, mantendo um historico limpo.
echo.
pause

:: Verifica se estamos dentro de um repositório Git
if not exist ".git" (
    echo ERRO: Nenhum repositorio Git encontrado nesta pasta.
    echo Execute este script dentro da pasta do seu projeto.
    pause
    exit /b
)

echo.
echo ======= Ultimos 10 commits do repositório =======
echo.

:: Mostra commits recentes com hash curto, autor e mensagem
git --no-pager log -10 --pretty=format:"%%h - %%an - %%s"

echo.
echo Informe QUANTOS commits deseja desfazer (ex: 3)
set /p qtd="> "

:: Verifica se o valor é numérico
for /f "delims=0123456789" %%a in ("%qtd%") do (
    echo Valor invalido. Digite apenas numeros.
    pause
    exit /b
)

if "%qtd%"=="" (
    echo Nenhum valor informado. Operacao cancelada.
    pause
    exit /b
)

echo.
echo Sera criado um novo commit que desfaz os ultimos %qtd% commits.
echo Nenhum commit sera apagado, apenas revertido de forma segura.
echo.
git log -%qtd% --oneline
echo.
set /p confirm="Deseja realmente reverter estes %qtd% commits (S/N)? "
if /I "%confirm%" NEQ "S" (
    echo Operacao cancelada.
    pause
    exit /b
)

echo.
echo Revertendo ultimos %qtd% commits...
git revert --no-edit HEAD~%qtd%..HEAD
if %ERRORLEVEL% NEQ 0 (
    echo.
    echo ERRO: Houve conflito ou falha na reversao.
    echo Verifique manualmente os conflitos listados acima.
    pause
    exit /b
)

echo.
set /p confirmPush="Deseja enviar (push) a reversao para o GitHub agora (S/N)? "
if /I "%confirmPush%"=="S" (
    echo.
    echo Enviando reversao ao GitHub...
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
git --no-pager log -5 --pretty=format:"%%h - %%an - %%s"

echo.
echo Operacao concluida com sucesso.
pause
