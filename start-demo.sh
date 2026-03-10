#!/usr/bin/env bash
set -euo pipefail

ROOT_DIR="$(cd "$(dirname "$0")" && pwd)"

echo "Starting AIOps backend..."
(cd "$ROOT_DIR/aiops-backend" && mvn -pl aiops-web -am -DskipTests package && java -jar aiops-web/target/aiops-web-1.0.0-SNAPSHOT.jar) &
BACKEND_PID=$!

echo "Starting AIOps frontend..."
(cd "$ROOT_DIR/aiops-frontend" && npm run dev) &
FRONTEND_PID=$!

cleanup() {
  echo "Stopping demo services..."
  kill "$BACKEND_PID" 2>/dev/null || true
  kill "$FRONTEND_PID" 2>/dev/null || true
}

trap cleanup EXIT INT TERM

echo "Backend:  http://localhost:8080"
echo "Frontend: http://localhost:5173"
echo "Press Ctrl+C to stop both services."

wait
