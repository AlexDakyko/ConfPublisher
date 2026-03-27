# ConfPublisher — остановка сервисов (PowerShell)
$ErrorActionPreference = 'SilentlyContinue'
Write-Host '=== Останавливаю сервисы ConfPublisher ===' -ForegroundColor Red

# 1) Пытаемся остановить контейнеры, проброшенные на 9091 и 5173
$web = docker ps -q --filter "publish=9091"
if ($web) { docker stop $web | Out-Null }
$ui  = docker ps -q --filter "publish=5173"
if ($ui)  { docker stop $ui  | Out-Null }

# 2) Останавливаем БД из compose
Write-Host '[DB] docker compose down db'
docker compose down db | Out-Null

Write-Host 'OK: всё остановлено.' -ForegroundColor Green
Write-Host 'Подсказка: очистить кэш Docker -> docker system prune -f'