@echo off
setlocal ENABLEEXTENSIONS ENABLEDELAYEDEXPANSION

REM === ConfPublisher one-click launcher (Windows) ===
REM Usage: double-click run.bat

TITLE ConfPublisher Launcher
color 0A

echo ==============================================
echo   ConfPublisher — запуск одной кнопкой
echo ==============================================

REM --- 0) Проверка наличия Docker ---
where docker >nul 2>nul
if errorlevel 1 (
  echo [ERROR] Docker не найден. Установи Docker Desktop и перезапусти.
  echo https://www.docker.com/products/docker-desktop/
  pause
  exit /b 1
)

REM --- 1) Поднятие базы данных ---
echo.
echo [DB] Поднимаю PostgreSQL через Docker Compose...
 docker compose up -d db
if errorlevel 1 (
  echo [ERROR] Не удалось запустить контейнер БД через docker compose.
  pause
  exit /b 1
)

REM --- 2) Определяем, чем запускать backend: Maven Wrapper или Gradle Wrapper ---
set BACKEND_DIR=backend
set USE_MAVEN=
set USE_GRADLE=

if exist "%BACKEND_DIR%\mvnw.cmd" set USE_MAVEN=1
if exist "%BACKEND_DIR%\mvnw" set USE_MAVEN=1
if exist "%BACKEND_DIR%\gradlew.bat" set USE_GRADLE=1
if exist "%BACKEND_DIR%\gradlew" set USE_GRADLE=1

if not defined USE_MAVEN if not defined USE_GRADLE (
  echo.
  echo [WARN] В проекте не найден mvnw/gradlew. Попробую системный mvn, затем gradle.
)

REM --- 3) Запуск backend в отдельном окне ---
echo.
echo [BACKEND] Запускаю Spring Boot...
if defined USE_MAVEN (
  start cmd /k "cd /d %BACKEND_DIR% && .\mvnw.cmd spring-boot:run || mvnw spring-boot:run || mvn spring-boot:run"
) else if defined USE_GRADLE (
  start cmd /k "cd /d %BACKEND_DIR% && .\gradlew.bat bootRun || gradlew bootRun || gradle bootRun"
) else (
  start cmd /k "cd /d %BACKEND_DIR% && mvn spring-boot:run || gradle bootRun"
)

REM --- 4) Запуск frontend в отдельном окне ---
echo.
echo [FRONTEND] Устанавливаю зависимости и запускаю Vite...
start cmd /k "cd /d frontend && npm install && npm run dev"

REM --- 5) Небольшая пауза и открытие браузера ---
choice /T 6 /D Y /N >nul
start http://localhost:5173
start http://localhost:9091/swagger-ui/index.html

echo.
echo [OK] Проект запускается. Окна backend и frontend открыты отдельно.
echo Для остановки — закройте соответствующие окна (или Ctrl+C в каждом).

echo.
pause
