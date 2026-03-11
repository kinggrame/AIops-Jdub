$ErrorActionPreference = 'Stop'

$root = Split-Path -Parent $MyInvocation.MyCommand.Path

Write-Host 'Starting AIOps backend...' -ForegroundColor Cyan
Start-Process powershell -ArgumentList '-NoExit', '-Command', "Set-Location '$root\aiops-backend'; mvn -pl aiops-web -am -DskipTests package; java -jar 'aiops-web\target\aiops-web-1.0.0-SNAPSHOT.jar'"

Write-Host 'Starting AIOps frontend...' -ForegroundColor Cyan
Start-Process powershell -ArgumentList '-NoExit', '-Command', "Set-Location '$root\aiops-frontend'; npm run dev"

Write-Host 'Backend and frontend start commands launched.' -ForegroundColor Green
Write-Host 'Backend:  http://localhost:8080' -ForegroundColor Yellow
Write-Host 'Frontend: http://localhost:5173' -ForegroundColor Yellow
Write-Host 'If you need remote agent access, enable tunnel before or during backend startup.' -ForegroundColor Yellow
