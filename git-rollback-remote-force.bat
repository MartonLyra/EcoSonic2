@echo off
title Git Rollback - Remote FORCE
color 0C
echo =====================================================
echo     GIT ROLLBACK REMOTO (modo FORCADO)
echo =====================================================
echo.
echo ATENCAO: Este modo apaga o ultimo commit do GitHub.
echo Caso outros usuarios ja tenham feito pull, isso causara conflito.
echo.
echo Os demais usuarios precisarao executar um rebase para reconciliar as diferenças:
echo   git pull --rebase
echo.
echo.
set /p confirm="Tem certeza absoluta que deseja forcar o rollback (S/N)? "
if /I "%confirm%" NEQ "S" (
    echo Operacao cancelada.
    pause
    exit /b
)
echo.
git fetch
echo.
git log -2
echo.
echo Revertendo commit remoto com push forçado...
git reset --hard HEAD~1
git push --force
echo.
echo Rollback remoto forçado concluido!
pause
