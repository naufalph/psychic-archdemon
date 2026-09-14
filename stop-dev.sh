#!/bin/bash

BASE_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
source "$BASE_DIR/dev-lib.sh"

echo -e "${YELLOW}Stopping Rumantra Development Environment${NC}\n"

# Signal the recorded session first so forked children (the spring-boot JVM) go
# down with their parent; kill_port then sweeps whatever is still bound.
stop_recorded() {
    local pid_file=$1
    local service_name=$2
    local pid

    [ -f "$pid_file" ] || return 0
    pid=$(cat "$pid_file" 2>/dev/null)
    rm -f "$pid_file"

    [[ "$pid" =~ ^[0-9]+$ ]] || return 0
    kill -0 "$pid" 2>/dev/null || return 0
    if ! is_dev_process "$pid"; then
        echo "  Ignoring stale $service_name PID $pid - it now belongs to another program"
        return 0
    fi

    echo "  Signalling recorded $service_name process group (PGID: $pid)"
    kill -TERM -- "-$pid" 2>/dev/null || kill "$pid" 2>/dev/null || true
    sleep 1
}

echo -e "${GREEN}[1/3] Stopping Backend (port 8080)...${NC}"
stop_recorded "$BASE_DIR/.backend.pid" "backend"
kill_port 8080 "Backend"

echo -e "\n${GREEN}[2/3] Stopping Frontend (port 3001)...${NC}"
stop_recorded "$BASE_DIR/.frontend.pid" "frontend"
kill_port 3001 "Frontend"

echo -e "\n${GREEN}[3/3] Stopping PostgreSQL Database...${NC}"
if require_docker; then
    docker compose -f "$BASE_DIR/docker/dev-database.yml" down
    echo -e "${GREEN}✓ Database stopped${NC}"
else
    echo -e "${YELLOW}⚠ Skipping database shutdown - Docker is unavailable${NC}"
fi

echo -e "\n${YELLOW}Cleaning up log files...${NC}"
for log in backend.log frontend.log; do
    if [ -f "$BASE_DIR/$log" ]; then
        rm "$BASE_DIR/$log"
        echo "  Removed $log"
    fi
done

echo -e "\n${GREEN}═══════════════════════════════════════════${NC}"
echo -e "${GREEN}✓ All services stopped successfully!${NC}"
echo -e "${GREEN}═══════════════════════════════════════════${NC}\n"
