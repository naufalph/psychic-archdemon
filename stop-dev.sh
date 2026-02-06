#!/bin/bash

# Colors for output
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
RED='\033[0;31m'
NC='\033[0m'

BASE_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"

echo -e "${YELLOW}Stopping Rumantra Development Environment${NC}\n"

# Function to kill process by port
kill_by_port() {
    local port=$1
    local service_name=$2

    PIDS=$(lsof -ti:$port 2>/dev/null)
    if [ ! -z "$PIDS" ]; then
        echo -e "${YELLOW}Found $service_name on port $port (PIDs: $PIDS)${NC}"
        for PID in $PIDS; do
            PROCESS_INFO=$(ps -p $PID -o comm= 2>/dev/null)
            echo "  Killing PID $PID ($PROCESS_INFO)"
            kill $PID 2>/dev/null || true
        done

        # Wait a bit and check if still running
        sleep 2
        REMAINING=$(lsof -ti:$port 2>/dev/null)
        if [ ! -z "$REMAINING" ]; then
            echo -e "${RED}  Force killing remaining processes${NC}"
            kill -9 $REMAINING 2>/dev/null || true
        fi
        echo -e "${GREEN}✓ $service_name stopped${NC}"
    else
        echo -e "${YELLOW}No $service_name process found on port $port${NC}"
    fi
}

# Stop backend (port 8080)
echo -e "${GREEN}[1/3] Stopping Backend (port 8080)...${NC}"
kill_by_port 8080 "Backend"

# Stop frontend (port 3001)
echo -e "\n${GREEN}[2/3] Stopping Frontend (port 3001)...${NC}"
kill_by_port 3001 "Frontend"

# Stop database
echo -e "\n${GREEN}[3/3] Stopping PostgreSQL Database...${NC}"
docker compose -f "$BASE_DIR/docker/dev-database.yml" down
echo -e "${GREEN}✓ Database stopped${NC}"

# Clean up log files
echo -e "\n${YELLOW}Cleaning up log files...${NC}"
if [ -f "$BASE_DIR/backend.log" ]; then
    rm "$BASE_DIR/backend.log"
    echo "  Removed backend.log"
fi

if [ -f "$BASE_DIR/frontend.log" ]; then
    rm "$BASE_DIR/frontend.log"
    echo "  Removed frontend.log"
fi

echo -e "\n${GREEN}═══════════════════════════════════════════${NC}"
echo -e "${GREEN}✓ All services stopped successfully!${NC}"
echo -e "${GREEN}═══════════════════════════════════════════${NC}\n"
