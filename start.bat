@echo off
echo ========================================
echo Historia API - Uruchamianie z JPA + H2
echo ========================================
echo.

echo [1/3] Czyszczenie i kompilacja...
call mvnw.cmd clean package
if %ERRORLEVEL% NEQ 0 (
    echo BLAD: Kompilacja nie powiodla sie!
    pause
    exit /b 1
)

echo.
echo [2/3] Uruchamianie Liberty server...
echo Aplikacja bedzie dostepna pod: http://localhost:9080/History-API/
echo Aby zatrzymac serwer, nacisnij Ctrl+C
echo.

call mvnw.cmd -P liberty liberty:dev

pause

