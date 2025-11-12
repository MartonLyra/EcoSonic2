@echo off
title Git Rollback - Remote FORCE (Varios Commits)
color 0C

echo =====================================================
echo          GIT ROLLBACK REMOTO - MODO FORCADO
echo =====================================================
echo.
echo ATENCAO: Este modo APAGA commits do historico remoto!
echo.
echo Use APENAS em casos criticos (ex: commit de senha, chave,
echo dado privado ou erro grave) e SOMENTE se voce entende
echo as consequencias.
echo.
echo O comando usara:
echo     git reset --hard HEAD~N
echo     git push --force
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

:: Mostra commits recentes
git --no-pager log -10 --pretty=format:"%%h - %%an - %%s"

echo.
echo Informe QUANTOS commits deseja APAGAR (ex: 3)
set /p qtd="> "

:: Verifica se é um número válido
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
echo Estes serao os commits REMOVIDOS do historico remoto:
git log -%qtd% --oneline
echo.
echo *** ATENCAO ***
echo - O historico sera reescrito.
echo - Outros colaboradores terao que executar "git pull --rebase" manualmente.
echo - Os commits apagados NAO poderao ser recuperados apos o push forçado.
echo.

set /p backupConfirm="Deseja criar um branch de backup antes de continuar (S/N)? "
if /I "%backupConfirm%"=="S" (
    set /p branchName="Digite um nome para o branch de backup (ex: backup-pre-reset): "
    if "%branchName%"=="" set branchName=backup-pre-reset
    echo Criando branch de backup "%branchName%"...
    git branch %branchName%
    echo Branch de backup criado com sucesso.
    echo.
)

set /p confirm1="Tem CERTEZA que deseja apagar os ultimos %qtd% commits (S/N)? "
if /I "%confirm1%" NEQ "S" (
    echo Operacao cancelada.
    pause
    exit /b
)

set /p confirm2="Esta acao e IRREVERSIVEL apos o push. Deseja continuar (S/N)? "
if /I "%confirm2%" NEQ "S" (
    echo Operacao cancelada.
    pause
    exit /b
)

echo.
echo Executando reset forçado...
git reset --hard HEAD~%qtd%
if %ERRORLEVEL% NEQ 0 (
    echo ERRO: Falha ao executar git reset.
    pause
    exit /b
)

echo.
set /p confirmPush="Deseja aplicar (push) ao GitHub agora (S/N)? "
if /I "%confirmPush%"=="S" (
    echo.
    echo Enviando alteracoes forçadas para o GitHub...
    git push --force
    echo.
    echo *** Push forçado concluido com sucesso! ***
) else (
    echo.
    echo Reset local concluido, mas NAO foi enviado ao GitHub.
    echo Execute "git push --force" manualmente se desejar publicar.
)

echo.
echo ======= Historico atual =======
git --no-pager log -5 --pretty=format:"%%h - %%an - %%s"

echo.
echo Operacao concluida.
pause
