@echo off
setlocal ENABLEEXTENSIONS ENABLEDELAYEDEXPANSION
TITLE ConfPublisher Launcher (Docker-only)
color 0A

echo ==============================================
echo   ConfPublisher — запуск одной кнопкой (Docker)
echo ==============================================

REM --- 0) Проверка Docker ---
where docker >nul 2>nul
if errorlevel 1 (
  echo [ERROR] Docker не найден. Установи Docker Desktop и перезапусти.
  echo https://www.docker.com/products/docker-desktop/
  pause
  exit /b 1
)

REM --- 1) Поднимаем БД (compose) ---
echo.
echo [DB] docker compose up -d db
docker compose up -d db
if errorlevel 1 (
  echo [ERROR] Не удалось поднять db через docker compose.
  pause
  exit /b 1
)

REM --- 2) Запускаем BACKEND внутри контейнера Maven (без локального mvn/gradle) ---
echo.
echo [BACKEND] Запускаю Spring Boot из контейнера Maven (порт 9091)...
set BACKEND_DIR=%CD%\backend

start "ConfPublisher Backend" cmd /k ^
 "docker run --rm -it -p 9091:9091 ^
   -v \"%BACKEND_DIR%\":/app -w /app ^
   maven:3.9.6-eclipse-temurin-21 ^
   mvn -q -Dspring-boot.run.jvmArguments=--server.port=9091 spring-boot:run"

REM --- 3) Запускаем FRONTEND внутри контейнера Node (без локального node/npm) ---
echo.
echo [FRONTEND] Запускаю Vite из контейнера Node (порт 5173)...
set FRONTEND_DIR=%CD%\frontend

start "ConfPublisher Frontend" cmd /k ^
 "docker run --rm -it -p 5173:5173 ^
   -v \"%FRONTEND_DIR%\":/app -w /app ^
   node:20 ^
   bash -lc \"npm install && npm run dev -- --host 0.0.0.0 --port 5173\""

REM --- 4) Откроем браузер через паузу ---
choice /T 7 /D Y /N >nul
start http://localhost:5173
start http://localhost:9091/swagger-ui/index.html

echo.
echo [OK] Проект запускается в двух окнах (backend и frontend) через Docker.
echo Закрыть проект = закрыть эти два окна (или Ctrl+C в каждом).
echo.
pause