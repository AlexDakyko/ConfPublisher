@echo off
setlocal ENABLEEXTENSIONS ENABLEDELAYEDEXPANSION
TITLE ConfPublisher Launcher (Docker-only)
color 0A

echo ==============================================
echo   ConfPublisher — запуск одной кнопкой (Docker)
echo ==============================================

REM --- 0) Docker check ---
where docker >nul 2>nul
if errorlevel 1 (
  echo [ERROR] Docker не найден. Установи Docker Desktop и перезапусти.
  echo https://www.docker.com/products/docker-desktop/
  pause
  exit /b 1
)

REM --- 1) DB up ---
echo.
echo [DB] docker compose up -d db
docker compose up -d db
if errorlevel 1 (
  echo [ERROR] Не удалось поднять db через docker compose.
  pause
  exit /b 1
)

REM ================================
REM === BACKEND (Spring Boot)  ====
REM ================================
echo.
echo [BACKEND] Запускаю Spring Boot через Maven-контейнер...

set "BACKEND_DIR=%CD%\backend"

start "ConfPublisher Backend" cmd /k ^
docker run --rm -it -p 9091:9091 -v "%BACKEND_DIR%":/app -w /app maven:3.9.6-eclipse-temurin-21 ^
mvn -q spring-boot:run -Dspring-boot.run.jvmArguments=-Dserver.port=9091


REM ================================
REM === FRONTEND (Vite + React) ===
REM ================================
echo.
echo [FRONTEND] Запускаю Vite через Node-контейнер...

set "FRONTEND_DIR=%CD%\frontend"

start "ConfPublisher Frontend" cmd /k ^
docker run --rm -it -p 5173:5173 -v "%FRONTEND_DIR%":/app -w /app node:20 bash -lc "npm install --legacy-peer-deps && npm i -D @vitejs/plugin-react && npm run dev -- --host 0.0.0.0 --port 5173"


REM --- 4) Авто-открытие браузера ---
echo.
echo [INFO] Открываю браузер через 10 секунд...
timeout /t 10 >nul
start http://localhost:5173
start http://localhost:9091/swagger-ui/index.html

echo.
echo [OK] Проект запущен в двух Docker-окнах (backend и frontend).
echo Закрыть проект = закрыть эти окна (или Ctrl+C).
echo.
pause