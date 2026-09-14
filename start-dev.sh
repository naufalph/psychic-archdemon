#!/bin/bash

set -e

BASE_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
source "$BASE_DIR/dev-lib.sh"

echo -e "${GREEN}Starting Rumantra Development Environment${NC}\n"

cleanup() {
    local code=${1:-0}
    echo -e "\n${YELLOW}Shutting down services...${NC}"

    if [ -n "$TAIL_PID" ]; then
        kill "$TAIL_PID" 2>/dev/null || true
    fi

    # Each service runs in its own session, so signal the whole process group:
    # `mvn spring-boot:run` forks a child JVM that outlives a kill of the mvn PID.
    if [ -n "$BACKEND_PID" ]; then
        echo "Stopping backend (PGID: $BACKEND_PID)"
        kill -TERM -- "-$BACKEND_PID" 2>/dev/null || kill "$BACKEND_PID" 2>/dev/null || true
    fi

    if [ -n "$FRONTEND_PID" ]; then
        echo "Stopping frontend (PGID: $FRONTEND_PID)"
        kill -TERM -- "-$FRONTEND_PID" 2>/dev/null || kill "$FRONTEND_PID" 2>/dev/null || true
    fi

    sleep 2
    kill_port 8080 "Backend" >/dev/null 2>&1 || true
    kill_port 3001 "Frontend" >/dev/null 2>&1 || true

    rm -f "$BASE_DIR/.backend.pid" "$BASE_DIR/.frontend.pid"

    echo -e "${GREEN}Services stopped${NC}"
    exit "$code"
}

trap cleanup SIGINT SIGTERM

# Fail before starting anything, so a misconfigured host does not half-start.
echo -e "${GREEN}[0/3] Preflight checks...${NC}"
PREFLIGHT_FAILED=0
require_docker || PREFLIGHT_FAILED=1
require_backend_env || PREFLIGHT_FAILED=1
pids_on_port 8080 >/dev/null || PREFLIGHT_FAILED=1
if [ "$PREFLIGHT_FAILED" -ne 0 ]; then
    echo -e "\n${RED}Preflight failed - nothing was started.${NC}" >&2
    echo -e "Fix the items above, then re-run ${YELLOW}./start-dev.sh${NC}\n" >&2
    exit 1
fi
echo -e "${GREEN}✓ Preflight passed${NC}"

echo -e "\n${GREEN}[1/3] Starting PostgreSQL Database...${NC}"
docker compose -f "$BASE_DIR/docker/dev-database.yml" up -d

echo "Waiting for database to be ready..."
wait_for_db || exit 1

echo -e "\n${GREEN}[2/3] Starting Backend (Spring Boot)...${NC}"
cd "$BASE_DIR/backend"

echo "Loading environment variables from .env file..."
load_env_file "$BASE_DIR/backend/.env"

run_detached mvn spring-boot:run > "$BASE_DIR/backend.log" 2>&1 &
BACKEND_PID=$!
echo "$BACKEND_PID" > "$BASE_DIR/.backend.pid"
echo "Backend started with PID: $BACKEND_PID"
echo "Backend logs: $BASE_DIR/backend.log"

echo "Waiting for backend to initialize..."
if ! wait_for_port 8080 "Backend" "$BASE_DIR/backend.log" 120; then
    cleanup 1
fi

if curl -sf --connect-timeout 2 --max-time 10 http://localhost:8080/actuator/health >/dev/null 2>&1; then
    echo -e "${GREEN}✓ Backend health check passed${NC}"
else
    echo -e "${YELLOW}⚠ Backend is listening but /actuator/health did not respond${NC}"
fi

echo -e "\n${GREEN}[3/3] Starting Frontend (Vue 3)...${NC}"
cd "$BASE_DIR/frontend2"

if [ ! -d "node_modules" ]; then
    echo -e "${YELLOW}Installing frontend dependencies...${NC}"
    npm install
fi

run_detached npm run dev > "$BASE_DIR/frontend.log" 2>&1 &
FRONTEND_PID=$!
echo "$FRONTEND_PID" > "$BASE_DIR/.frontend.pid"
echo "Frontend started with PID: $FRONTEND_PID"
echo "Frontend logs: $BASE_DIR/frontend.log"

if ! wait_for_port 3001 "Frontend" "$BASE_DIR/frontend.log" 60; then
    cleanup 1
fi

echo -e "\n${GREEN}═══════════════════════════════════════════${NC}"
echo -e "${GREEN}✓ All services started successfully!${NC}"
echo -e "${GREEN}═══════════════════════════════════════════${NC}"
echo -e "\nService URLs:"
echo -e "  Frontend:  ${YELLOW}http://localhost:3001${NC}"
echo -e "  Backend:   ${YELLOW}http://localhost:8080${NC}"
echo -e "  Database:  ${YELLOW}localhost:5432${NC}"
echo -e "\nLogs:"
echo -e "  Backend:   ${YELLOW}tail -f $BASE_DIR/backend.log${NC}"
echo -e "  Frontend:  ${YELLOW}tail -f $BASE_DIR/frontend.log${NC}"
echo -e "  Database:  ${YELLOW}docker compose -f docker/dev-database.yml logs -f${NC}"
echo -e "\nPress ${RED}Ctrl+C${NC} to stop all services\n"
echo -e "${GREEN}═══════════════════════════════════════════${NC}\n"

tail -f "$BASE_DIR/backend.log" "$BASE_DIR/frontend.log" 2>/dev/null &
TAIL_PID=$!

wait
