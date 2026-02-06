#!/bin/bash

# Colors for output
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
RED='\033[0;31m'
BLUE='\033[0;34m'
NC='\033[0m'

BASE_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"

echo -e "${GREEN}Rumantra Development Environment Status${NC}\n"

# Function to check port
check_port() {
    local port=$1
    local service_name=$2
    local health_url=$3

    echo -e "${YELLOW}$service_name (Port $port):${NC}"
    PIDS=$(lsof -ti:$port 2>/dev/null)

    if [ ! -z "$PIDS" ]; then
        for PID in $PIDS; do
            PROCESS_INFO=$(ps -p $PID -o comm= 2>/dev/null)
            echo -e "  Status: ${GREEN}✓ Running${NC} (PID: $PID, Process: $PROCESS_INFO)"
        done

        # Check health if URL provided
        if [ ! -z "$health_url" ]; then
            if curl -s $health_url > /dev/null 2>&1; then
                echo -e "  Health: ${GREEN}✓ Accessible${NC}"
            else
                echo -e "  Health: ${YELLOW}⚠ Not responding${NC}"
            fi
        fi
        return 0
    else
        echo -e "  Status: ${RED}✗ Not running${NC}"
        return 1
    fi
}

# Check database
echo -e "${YELLOW}Database (PostgreSQL - Port 5432):${NC}"
DB_RUNNING=0
DB_STATUS=$(docker compose -f "$BASE_DIR/docker/dev-database.yml" ps --format json 2>/dev/null | grep -q "running" && echo "running" || echo "stopped")
if [ "$DB_STATUS" == "running" ]; then
    echo -e "  Status: ${GREEN}✓ Running${NC}"
    docker compose -f "$BASE_DIR/docker/dev-database.yml" ps --format "table {{.Name}}\t{{.Status}}\t{{.Ports}}"
    DB_RUNNING=1
else
    echo -e "  Status: ${RED}✗ Stopped${NC}"
fi

# Check backend
echo -e ""
check_port 8080 "Backend (Spring Boot)" "http://localhost:8080/actuator/health"
BACKEND_RUNNING=$?

# Check frontend
echo -e ""
check_port 3001 "Frontend (Vue 3 + Vite)" "http://localhost:3001"
FRONTEND_RUNNING=$?

# Port conflicts check
echo -e "\n${BLUE}Port Usage Summary:${NC}"
echo -e "  Port 5432 (PostgreSQL): $(lsof -ti:5432 2>/dev/null | wc -l) process(es)"
echo -e "  Port 8080 (Backend):    $(lsof -ti:8080 2>/dev/null | wc -l) process(es)"
echo -e "  Port 3001 (Frontend):   $(lsof -ti:3001 2>/dev/null | wc -l) process(es)"

# Summary
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
