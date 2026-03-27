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

REM --- 1) DB up (compose) ---
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
echo [BACKEND] Запускаю Spring Boot через Maven-контейнер (порт 9091)...

set "BACKEND_DIR=%CD%\backend"
REM ВАЖНО: подключение к БД с хоста: host.docker.internal:5433
start "ConfPublisher Backend" cmd /k docker run --rm -it ^
  -p 9091:9091 ^
  -e SERVER_PORT=9091 ^
  -e SPRING_DATASOURCE_URL=jdbc:postgresql://host.docker.internal:5433/confpublisher ^
  -e SPRING_DATASOURCE_USERNAME=postgres ^
  -e SPRING_DATASOURCE_PASSWORD=postgres ^
  -v "%BACKEND_DIR%":/app -w /app ^
  maven:3.9.6-eclipse-temurin-21 mvn -q spring-boot:run

REM ================================
REM === FRONTEND (Vite + React) ===
REM ================================
echo.
echo [FRONTEND] Запускаю Vite через Node-контейнер (порт 5173)...

set "FRONTEND_DIR=%CD%\frontend"
start "ConfPublisher Frontend" cmd /k docker run --rm -it ^
  -p 5173:5173 ^
  -v "%FRONTEND_DIR%":/app -w /app ^
  node:20 bash -lc "npm install --legacy-peer-deps && npm i -D @vitejs/plugin-react@4.2.1 --legacy-peer-deps && npm run dev -- --host 0.0.0.0 --port 5173"

REM --- 4) Авто-открытие браузера ---
echo.
echo [INFO] Открываю браузер через 12 секунд...
timeout /t 12 >nul
start http://localhost:5173
start http://localhost:9091/swagger-ui/index.html

echo.
echo [OK] Проект запущен в двух Docker-окнах (backend и frontend).
echo Закрыть проект = закрыть эти окна (или Ctrl+C).
echo.
pause