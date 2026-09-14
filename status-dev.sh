#!/bin/bash

BASE_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
source "$BASE_DIR/dev-lib.sh"

echo -e "${GREEN}Rumantra Development Environment Status${NC}\n"

check_port() {
    local port=$1
    local service_name=$2
    local health_url=$3
    local pids pid

    echo -e "${YELLOW}$service_name (Port $port):${NC}"

    if ! pids=$(pids_on_port "$port"); then
        echo -e "  Status: ${RED}✗ Unknown - no port inspection tool${NC}"
        return 1
    fi

    if [ -z "$pids" ]; then
        echo -e "  Status: ${RED}✗ Not running${NC}"
        return 1
    fi

    for pid in $pids; do
        echo -e "  Status: ${GREEN}✓ Running${NC} (PID: $pid, Process: $(ps -p "$pid" -o comm= 2>/dev/null))"
    done

    if [ -n "$health_url" ]; then
        if curl -sS --connect-timeout 2 --max-time 5 "$health_url" > /dev/null 2>&1; then
            echo -e "  Health: ${GREEN}✓ Accessible${NC}"
        else
            echo -e "  Health: ${YELLOW}⚠ Not responding${NC}"
        fi
    fi
    return 0
}

echo -e "${YELLOW}Database (PostgreSQL - Port 5432):${NC}"
DB_RUNNING=0
if ! require_docker 2>/dev/null; then
    echo -e "  Status: ${RED}✗ Docker unavailable${NC}"
    require_docker >/dev/null
else
    DB_STATUS=$(docker compose -f "$BASE_DIR/docker/dev-database.yml" ps --format json 2>/dev/null | grep -q "running" && echo "running" || echo "stopped")
    if [ "$DB_STATUS" == "running" ]; then
        echo -e "  Status: ${GREEN}✓ Running${NC}"
        docker compose -f "$BASE_DIR/docker/dev-database.yml" ps --format "table {{.Name}}\t{{.Status}}\t{{.Ports}}"
        DB_RUNNING=1
    else
        echo -e "  Status: ${RED}✗ Stopped${NC}"
    fi
fi

echo -e ""
check_port 8080 "Backend (Spring Boot)" "http://localhost:8080/actuator/health"
BACKEND_RUNNING=$?

echo -e ""
check_port 3001 "Frontend (Vue 3 + Vite)" "http://localhost:3001"
FRONTEND_RUNNING=$?

echo -e "\n${BLUE}Port Usage Summary:${NC}"
echo -e "  Port 5432 (PostgreSQL): $(pids_on_port 5432 2>/dev/null | wc -l) process(es)"
echo -e "  Port 8080 (Backend):    $(pids_on_port 8080 2>/dev/null | wc -l) process(es)"
echo -e "  Port 3001 (Frontend):   $(pids_on_port 3001 2>/dev/null | wc -l) process(es)"

echo -e "\n${GREEN}═══════════════════════════════════════════${NC}"
if [ $DB_RUNNING -eq 1 ] && [ $BACKEND_RUNNING -eq 0 ] && [ $FRONTEND_RUNNING -eq 0 ]; then
    echo -e "${GREEN}✓ All services are running${NC}"
    echo -e "\nAccess your application:"
    echo -e "  Frontend:  ${YELLOW}http://localhost:3001${NC}"
    echo -e "  Backend:   ${YELLOW}http://localhost:8080${NC}"
    echo -e "  Swagger:   ${YELLOW}http://localhost:8080/swagger-ui.html${NC}"
    echo -e "  Database:  ${YELLOW}localhost:5432${NC} (user: postgres, db: rumantra-db)"
else
    echo -e "${YELLOW}⚠ Some services are not running${NC}"
    echo -e "\nTo start all services, run:"
    echo -e "  ${YELLOW}./start-dev.sh${NC}"
    echo -e "\nTo stop all services, run:"
    echo -e "  ${YELLOW}./stop-dev.sh${NC}"
fi
echo -e "${GREEN}═══════════════════════════════════════════${NC}\n"
