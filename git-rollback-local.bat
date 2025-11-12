@echo off
title Git Rollback - Local
color 0A
echo =====================================================
echo     GIT ROLLBACK LOCAL (somente commit local)
echo =====================================================
echo.
echo Este modo remove o ultimo commit, mas mantem seus arquivos.
echo O historico remoto (GitHub) NAO sera afetado.
echo.
set /p confirm="Tem certeza que deseja desfazer o ultimo commit local (S/N)? "
if /I "%confirm%" NEQ "S" (
    echo Operacao cancelada.
    pause
    exit /b
)
echo.
git status
echo.
echo Revertendo ultimo commit local...
git reset --soft HEAD~1
echo.
echo Operacao concluida.
git status
pause
