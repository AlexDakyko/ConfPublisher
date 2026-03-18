@echo off
setlocal ENABLEEXTENSIONS ENABLEDELAYEDEXPANSION
TITLE ConfPublisher Stopper
color 0C

echo ==============================================
echo   ConfPublisher — остановка сервисов
echo ==============================================

REM 1) Подсказка закрыть окна (если ещё открыты)
echo.
echo [INFO] Закройте окна 'ConfPublisher Backend' и 'ConfPublisher Frontend' при необходимости.
echo     (или я завершу контейнеры принудительно)

REM 2) Остановить контейнеры, если они ещё живы (по портам 9091 и 5173)
echo.
echo [DOCKER] Останавливаю контейнеры backend/frontend, если они запущены...
for /f "tokens=1" %%i in ('docker ps -q --filter "publish=9091"') do docker stop %%i >nul 2>nul
for /f "tokens=1" %%i in ('docker ps -q --filter "publish=5173"') do docker stop %%i >nul 2>nul

REM 3) Остановить БД, поднятую через compose
echo.
echo [DB] docker compose down db
docker compose down db

REM 4) Подсказка по очистке (опционально)
echo.
echo [TIP] Освободить место (опционально):
echo   docker system prune -f

echo.
echo [OK] Сервисы остановлены.
echo.
pause