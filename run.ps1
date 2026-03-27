# ConfPublisher one-click launcher (PowerShell)
# Usage: right-click -> Run with PowerShell

$ErrorActionPreference = 'Stop'
Write-Host '=== ConfPublisher — запуск одной кнопкой (PowerShell) ===' -ForegroundColor Green

# 0) Docker check
if (-not (Get-Command docker -ErrorAction SilentlyContinue)) {
  Write-Host '[ERROR] Docker не найден. Установи Docker Desktop.' -ForegroundColor Red
  Write-Host 'https://www.docker.com/products/docker-desktop/'
  exit 1
}

# 1) DB
echo '[DB] docker compose up -d db'
docker compose up -d db | Out-Null

# 2) Backend
$backendDir = 'backend'
$mvnw = Join-Path $backendDir 'mvnw.cmd'
$gradlew = Join-Path $backendDir 'gradlew.bat'

if (Test-Path $mvnw) {
  Start-Process cmd "/k cd /d $backendDir && .\mvnw.cmd spring-boot:run"
}
elseif (Test-Path $gradlew) {
  Start-Process cmd "/k cd /d $backendDir && .\gradlew.bat bootRun"
}
else {
  Start-Process cmd "/k cd /d $backendDir && mvn spring-boot:run"
}

# 3) Frontend
Start-Process cmd "/k cd /d frontend && npm install && npm run dev"

Start-Sleep -Seconds 6
Start-Process 'http://localhost:5173'
Start-Process 'http://localhost:9091/swagger-ui/index.html'

Write-Host '[OK] Проект запускается в отдельных окнах терминала.' -ForegroundColor Green
