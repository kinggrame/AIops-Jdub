$ErrorActionPreference = 'Stop'

$root = Split-Path -Parent $MyInvocation.MyCommand.Path

if (-not $env:AIOPS_BOOTSTRAP_TOKEN) {
  $env:AIOPS_BOOTSTRAP_TOKEN = 'aiops-mvp-seed-demo-token'
}

if (-not $env:AIOPS_SERVER_URL) {
  $env:AIOPS_SERVER_URL = 'http://localhost:8080'
}

Set-Location "$root\aiops-agent"
Write-Host "Starting agent..." -ForegroundColor Cyan
Write-Host "AIOPS_SERVER_URL=$env:AIOPS_SERVER_URL" -ForegroundColor Yellow
go run ./cmd -c config.yaml
