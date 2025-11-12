@echo off
title Git Rollback - Remote SAFE
color 0E
echo =====================================================
echo     GIT ROLLBACK REMOTO (modo SEGURO)
echo =====================================================
echo.
echo Este modo cria um novo commit que desfaz (reverte) o ultimo commit,
echo preservando o historico e sem afetar colaboradores.
echo.
echo Caso esse comando seja executado duas vezes seguida, o segundo comando
echo   vai desfazer o primeiro revert, ou seja, vai retornar ao estado original.
echo Ou seja, ele nao vai revertendo sucessivamente, reverte apenas o ultimo.
echo.
echo É como se você “desfizesse o desfazer”
echo.
set /p confirm="Tem certeza que deseja reverter o ultimo commit remoto (S/N)? "
if /I "%confirm%" NEQ "S" (
    echo Operacao cancelada.
    pause
    exit /b
)
echo.
git fetch
echo.
git log -1
echo.
echo Revertendo ultimo commit remoto...
git revert HEAD
echo.
git push
echo.
echo Operacao concluida com sucesso.
pause
