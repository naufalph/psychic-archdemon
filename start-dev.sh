#!/bin/bash

set -e

# Colors for output
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
RED='\033[0;31m'
NC='\033[0m' # No Color

# Base directory
BASE_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"

echo -e "${GREEN}Starting Rumantra Development Environment${NC}\n"

# Function to cleanup on exit
cleanup() {
    echo -e "\n${YELLOW}Shutting down services...${NC}"

    # Kill background processes
    if [ ! -z "$BACKEND_PID" ]; then
        echo "Stopping backend (PID: $BACKEND_PID)"
        kill $BACKEND_PID 2>/dev/null || true
    fi

    if [ ! -z "$FRONTEND_PID" ]; then
        echo "Stopping frontend (PID: $FRONTEND_PID)"
        kill $FRONTEND_PID 2>/dev/null || true
    fi

    echo -e "${GREEN}Services stopped${NC}"
    exit 0
}

# Trap SIGINT and SIGTERM
trap cleanup SIGINT SIGTERM

# Step 1: Start PostgreSQL Database
echo -e "${GREEN}[1/3] Starting PostgreSQL Database...${NC}"
docker compose -f "$BASE_DIR/docker/dev-database.yml" up -d

# Wait for database to be ready
echo "Waiting for database to be ready..."
sleep 5

# Step 2: Start Backend (Spring Boot)
echo -e "\n${GREEN}[2/3] Starting Backend (Spring Boot)...${NC}"
cd "$BASE_DIR/backend"

# Load environment variables from .env file if it exists
if [ -f ".env" ]; then
    echo "Loading environment variables from .env file..."
    export $(grep -v '^#' .env | grep -v '^$' | xargs)
fi

mvn spring-boot:run > "$BASE_DIR/backend.log" 2>&1 &
BACKEND_PID=$!
echo "Backend started with PID: $BACKEND_PID"
echo "Backend logs: $BASE_DIR/backend.log"

# Wait for backend to start
echo "Waiting for backend to initialize..."
sleep 10

# Step 3: Start Frontend (Vue 3)
echo -e "\n${GREEN}[3/3] Starting Frontend (Vue 3)...${NC}"
cd "$BASE_DIR/frontend2"

# Check if node_modules exists
if [ ! -d "node_modules" ]; then
    echo -e "${YELLOW}Installing frontend dependencies...${NC}"
    npm install
fi

npm run dev > "$BASE_DIR/frontend.log" 2>&1 &
FRONTEND_PID=$!
echo "Frontend started with PID: $FRONTEND_PID"
echo "Frontend logs: $BASE_DIR/frontend.log"

# Show status
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

# Keep script running and show combined logs
tail -f "$BASE_DIR/backend.log" "$BASE_DIR/frontend.log" 2>/dev/null &
TAIL_PID=$!

# Wait for user interrupt
wait
