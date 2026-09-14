#!/bin/bash
# Shared helpers for start-dev.sh / stop-dev.sh / status-dev.sh.
# Sourced, not executed.

GREEN='\033[0;32m'
YELLOW='\033[1;33m'
RED='\033[0;31m'
BLUE='\033[0;34m'
NC='\033[0m'

# Install hint for the host's package manager, so a non-Arch machine is not told to run pacman.
pkg_hint() {
    local arch_pkg=$1
    local debian_pkg=${2:-$1}
    local brew_pkg=${3:-$1}

    if command -v pacman >/dev/null 2>&1; then
        echo "sudo pacman -S $arch_pkg"
    elif command -v apt-get >/dev/null 2>&1; then
        echo "sudo apt-get install $debian_pkg"
    elif command -v brew >/dev/null 2>&1; then
        echo "brew install $brew_pkg"
    else
        echo "install $debian_pkg with your package manager"
    fi
}

# Starts a command in its own session when setsid exists (util-linux), so the whole process
# group can be signalled later. macOS has no setsid; there the PID is signalled alone and
# kill_port sweeps up any forked child still holding the port.
run_detached() {
    if command -v setsid >/dev/null 2>&1; then
        setsid "$@"
    else
        "$@"
    fi
}

# A recorded PID can outlive its process and be reused by something unrelated, so only
# treat it as ours if it is still one of the dev toolchain processes.
is_dev_process() {
    local pid=$1
    ps -p "$pid" -o command= 2>/dev/null | grep -qE 'mvn|maven|java|npm|node|vite'
}

# Prints PIDs listening on a TCP port, one per line.
# lsof is absent on a stock Arch install, so fall back to ss (iproute2), which
# reports PIDs for the caller's own processes without root. Never return empty
# on tooling failure - that silently reported every service as stopped.
pids_on_port() {
    local port=$1

    if command -v lsof >/dev/null 2>&1; then
        lsof -ti:"$port" 2>/dev/null
        return 0
    fi

    if command -v ss >/dev/null 2>&1; then
        ss -tlnpH "sport = :$port" 2>/dev/null \
            | grep -o 'pid=[0-9]*' \
            | cut -d= -f2 \
            | sort -u
        return 0
    fi

    echo -e "${RED}Cannot determine port usage: neither 'lsof' nor 'ss' found.${NC}" >&2
    echo -e "  Install one:  ${YELLOW}$(pkg_hint lsof)${NC}  (or iproute2 for ss)" >&2
    return 1
}

require_docker() {
    if ! command -v docker >/dev/null 2>&1; then
        echo -e "${RED}✗ 'docker' is not installed.${NC}" >&2
        echo -e "  Run:  ${YELLOW}$(pkg_hint 'docker docker-compose' 'docker.io docker-compose-v2' 'docker')${NC}" >&2
        return 1
    fi

    if docker info >/dev/null 2>&1; then
        return 0
    fi

    echo -e "${RED}✗ Cannot talk to the Docker daemon.${NC}" >&2

    # Arch does not enable docker.service on install, unlike Debian's docker.io; Docker
    # Desktop on macOS has no systemctl and is caught by the command check.
    if command -v systemctl >/dev/null 2>&1 \
        && [ "$(systemctl is-active docker 2>/dev/null)" != "active" ]; then
        echo -e "\n  Docker daemon is not running." >&2
        echo -e "  Run:  ${YELLOW}sudo systemctl enable --now docker.service${NC}" >&2
    fi

    if getent group docker >/dev/null 2>&1 && ! id -nG | grep -qw docker; then
        echo -e "\n  User '$(id -un)' is not in the 'docker' group." >&2
        echo -e "  Run:  ${YELLOW}sudo usermod -aG docker \$USER${NC}" >&2
        echo -e "  Then log out and back in (or: ${YELLOW}newgrp docker${NC})" >&2
    fi

    return 1
}

require_backend_env() {
    local env_file="$BASE_DIR/backend/.env"

    if [ -f "$env_file" ]; then
        return 0
    fi

    echo -e "${RED}✗ backend/.env is missing - the backend will not boot without it.${NC}" >&2
    echo -e "  JWT_SECRET, GOOGLE_CLIENT_ID/SECRET, LINKEDIN_CLIENT_ID/SECRET and" >&2
    echo -e "  EMAIL_* have no defaults in application.properties, so Spring fails" >&2
    echo -e "  on placeholder resolution." >&2
    echo -e "\n  Run:  ${YELLOW}cp backend/.env.example backend/.env${NC}" >&2
    echo -e "  Then fill in at least ${YELLOW}JWT_SECRET${NC}." >&2
    return 1
}

# Quotes, spaces and '#' inside values survive this; `export $(... | xargs)` does not.
load_env_file() {
    local env_file=$1
    [ -f "$env_file" ] || return 0
    set -a
    # shellcheck disable=SC1090
    . "$env_file"
    set +a
}

wait_for_db() {
    local retries=${1:-60}
    local i

    for ((i = 1; i <= retries; i++)); do
        if docker exec rumantra-db pg_isready -U postgres >/dev/null 2>&1; then
            echo -e "${GREEN}✓ Database is ready${NC}"
            return 0
        fi
        [ $((i % 5)) -eq 0 ] && echo "   Database is unavailable - waiting... (${i}s)"
        sleep 1
    done

    echo -e "${RED}✗ Database did not become ready after ${retries}s${NC}" >&2
    echo -e "  Check:  ${YELLOW}docker compose -f docker/dev-database.yml logs postgres${NC}" >&2
    return 1
}

kill_port() {
    local port=$1
    local service_name=$2
    local pids pid remaining

    pids=$(pids_on_port "$port") || return 1

    if [ -z "$pids" ]; then
        echo -e "${YELLOW}No $service_name process found on port $port${NC}"
        return 0
    fi

    echo -e "${YELLOW}Found $service_name on port $port (PIDs: $(echo $pids))${NC}"
    for pid in $pids; do
        echo "  Killing PID $pid ($(ps -p "$pid" -o comm= 2>/dev/null))"
        kill "$pid" 2>/dev/null || true
    done

    sleep 2
    remaining=$(pids_on_port "$port")
    if [ -n "$remaining" ]; then
        echo -e "${RED}  Force killing remaining processes${NC}"
        kill -9 $remaining 2>/dev/null || true
    fi

    echo -e "${GREEN}✓ $service_name stopped${NC}"
}

# Waits for a service to bind its port; tails its log and fails if it never does.
wait_for_port() {
    local port=$1
    local service_name=$2
    local log_file=$3
    local retries=${4:-90}
    local i

    for ((i = 1; i <= retries; i++)); do
        if [ -n "$(pids_on_port "$port")" ]; then
            echo -e "${GREEN}✓ $service_name is listening on port $port${NC}"
            return 0
        fi
        [ $((i % 10)) -eq 0 ] && echo "   Waiting for $service_name... (${i}s)"
        sleep 1
    done

    echo -e "${RED}✗ $service_name never bound port $port after ${retries}s${NC}" >&2
    if [ -f "$log_file" ]; then
        echo -e "${YELLOW}--- last 30 lines of $(basename "$log_file") ---${NC}" >&2
        tail -n 30 "$log_file" >&2
    fi
    return 1
}
