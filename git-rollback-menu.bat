@echo off
title Git Rollback - Menu Unificado
color 0B

REM ------------------------------------------------------------
REM Git Rollback - Menu Unificado
REM - Exibe explicação de cada modo
REM - Confirmação do usuário antes de chamar os scripts reais
REM - Espera que os scripts chamados estejam no mesmo diretório
REM ------------------------------------------------------------

setlocal enabledelayedexpansion

echo ============================================================
echo                  GIT ROLLBACK - MENU UNIFICADO
echo ============================================================
echo.
echo Este menu reune os scripts de rollback que voce ja tem:
echo.
echo  1) Local (desfazer ultimo commit local) ................ (git-rollback-local.bat)
echo     - Remove o ultimo commit localmente (mantem arquivos staged).
echo     - Nao altera o remoto (GitHub).
echo.
echo  2) Remoto SEGURO (revert HEAD) ......................... (git-rollback-remote-safe.bat)
echo     - Cria um novo commit que desfaz o ultimo commit remoto.
echo     - Metodo seguro para reverter sem reescrever historico.
echo.
echo  3) Remoto SEGURO AVANCADO (escolher commit) ............ (git-rollback-remote-safe-adv.bat)
echo     - Mostra commits recentes; voce escolhe qual reverter.
echo     - Seguro: cria commit de reversao.
echo.
echo  4) Remoto - VARIOS commits (revert range) .............. (git-rollback-remote-range.bat)
echo     - Cria um unico commit que desfaz os ultimos N commits.
echo     - Seguro para desfazer blocos de alterações.
echo.
echo  5) Remoto FORCADO (reset --hard + push --force) ........ (git-rollback-range-force.bat)
echo     - REESCREVE o historico remoto. Use apenas em casos criticos.
echo     - Oferece criar branch de backup antes de apagar.
echo.
echo  0) Sair
echo.
echo Observacoes importantes:
echo  - Todos os scripts chamados pedem confirmacao adicional antes de agir.
echo  - Execute este menu dentro da pasta do seu repositorio (onde existe .git).
echo  - Recomendo criar um branch de backup antes de operacoes destrutivas.
echo.

:askChoice
set /p choice="Escolha uma opcao (0-5): "
if "%choice%"=="" goto askChoice

if "%choice%"=="0" (
    echo Saindo...
    goto end
)

REM Verifica se estamos na pasta correta
if not exist ".git" (
    echo ERRO: .git nao encontrado. Execute este script na raiz do repositorio.
    pause
    goto end
)

REM Mapeia escolha para nome do script
if "%choice%"=="1" set scriptName=git-rollback-local.bat
if "%choice%"=="2" set scriptName=git-rollback-remote-safe.bat
if "%choice%"=="3" set scriptName=git-rollback-remote-safe-adv.bat
if "%choice%"=="4" set scriptName=git-rollback-remote-range.bat
if "%choice%"=="5" set scriptName=git-rollback-range-force.bat

if not defined scriptName (
    echo Opcao invalida.
    goto askChoice
)

REM Verifica se o script alvo existe
if not exist "%scriptName%" (
    echo ERRO: O script "%scriptName%" nao foi encontrado neste diretorio.
    echo Verifique que todos os scripts estao juntos com este arquivo.
    pause
    goto end
)

echo.
echo Voce selecionou a opcao %choice%: %scriptName%
echo.
REM Explicacao resumida antes de executar (reforcar para forçado)
if "%choice%"=="1" (
    echo DESCRICAO: Desfazer o ultimo commit local (mantem arquivos). Nao afeta remoto.
)
if "%choice%"=="2" (
    echo DESCRICAO: Criar commit que desfaz o ultimo commit remoto (modo seguro).
)
if "%choice%"=="3" (
    echo DESCRICAO: Escolher um commit dos recentes para reverter (modo seguro).
)
if "%choice%"=="4" (
    echo DESCRICAO: Reverter os ultimos N commits criando um unico commit de reversao.
)
if "%choice%"=="5" (
    echo ***** ATENCAO: MODO FORCADO ***** 
    echo Irei reescrever o historico local e (se confirmar) enviar com --force.
    echo Use apenas se voce entende as consequencias e se combinou com outros colaboradores.
)

echo.
set /p confirm="Deseja prosseguir e executar '%scriptName%' (S/N)? "
if /I NOT "%confirm%"=="S" (
    echo Operacao cancelada pelo usuario.
    goto end
)

REM Chama o script escolhido (mantendo janela atual)
echo.
echo Executando: %scriptName%
echo.
call "%scriptName%"

echo.
echo Retornando ao menu.
echo.
set scriptName=
pause
goto startMenu

:startMenu
cls
echo ============================================================
echo                  GIT ROLLBACK - MENU UNIFICADO
echo ============================================================
echo.
goto askChoice

:end
endlocal
exit /b
