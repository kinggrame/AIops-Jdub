#!/usr/bin/env bash
set -euo pipefail

ROOT_DIR="$(cd "$(dirname "$0")" && pwd)"

export AIOPS_BOOTSTRAP_TOKEN="${AIOPS_BOOTSTRAP_TOKEN:-aiops-mvp-seed-demo-token}"
export AIOPS_SERVER_URL="${AIOPS_SERVER_URL:-http://localhost:8080}"

cd "$ROOT_DIR/aiops-agent"
echo "Starting agent..."
echo "AIOPS_SERVER_URL=$AIOPS_SERVER_URL"
go run ./cmd -c config.yaml
