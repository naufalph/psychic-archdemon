#!/bin/bash

# Start Rumantra development environment for remote access via Tailscale
# Usage: ./start-remote.sh
#
# IMPORTANT: Before first use, configure Google OAuth Console:
# 1. Go to https://console.cloud.google.com/
# 2. Navigate to: APIs & Services → Credentials
# 3. Find your OAuth 2.0 Client ID
# 4. Under "Authorized redirect URIs", ADD:
#    http://aexther-lens.tailfeca57.ts.net:8080/rmtr/users/oauth2/callback/google
# 5. Keep localhost URI for local dev:
#    http://localhost:8080/rmtr/users/oauth2/callback/google
# 6. Click SAVE (changes take 5-10 minutes to propagate)
#
# What this script does:
# - Starts PostgreSQL database
# - Starts backend with remote profile (uses Tailscale URLs)
# - Starts frontend
# - Enables OAuth login from Android/remote devices via Tailscale
#
# Access URLs:
# - From Android (via Tailscale): http://aexther-lens.tailfeca57.ts.net:3000
# - From local machine: http://localhost:3000

set -e

echo "🚀 Starting Rumantra for Remote Access (Tailscale)"
echo "=================================================="
echo ""
echo "Remote URL: http://aexther-lens.tailfeca57.ts.net:3000"
echo "Local URL:  http://localhost:3000"
echo ""

# Check if .env exists in frontend2
if [ ! -f frontend2/.env ]; then
    echo "⚠️  Warning: frontend2/.env not found"
    echo "   Copying from .env.example..."
    cp frontend2/.env.example frontend2/.env
    echo "   Please edit frontend2/.env with your API keys"
fi

# Start database
echo "📦 Starting PostgreSQL database..."
docker compose -f docker/dev-database.yml up -d

# Wait for database to be ready
echo "⏳ Waiting for database to be ready..."
sleep 5

# Check if database is actually ready
until docker exec rumantra-db pg_isready -U postgres > /dev/null 2>&1; do
    echo "   Database is unavailable - waiting..."
    sleep 2
done

echo "✅ Database is ready"
echo ""

# Start backend with remote profile
echo "🔧 Starting Spring Boot backend (remote profile)..."
cd backend

# Load environment variables from .env file if it exists
if [ -f ".env" ]; then
    echo "   Loading environment variables from .env file..."
    export $(grep -v '^#' .env | grep -v '^$' | xargs)
else
    echo "⚠️  Warning: backend/.env not found"
    echo "   Create backend/.env with required variables (see README.md)"
fi

mvn spring-boot:run -Dspring-boot.run.profiles=remote > ../backend.log 2>&1 &
BACKEND_PID=$!
echo "   Backend PID: $BACKEND_PID"
cd ..

# Wait for backend to start
echo "⏳ Waiting for backend to start..."
sleep 10

# Check if backend is running
if ! curl -s http://localhost:8080/rmtr/users/oauth2/google > /dev/null 2>&1; then
    echo "⚠️  Warning: Backend might not be fully ready yet"
    echo "   Check backend.log for details"
fi

# Start frontend
echo "🎨 Starting Vue frontend..."
cd frontend2
npm run dev > ../frontend.log 2>&1 &
FRONTEND_PID=$!
echo "   Frontend PID: $FRONTEND_PID"
cd ..

echo ""
echo "✅ All services started!"
echo ""
echo "📱 Access from Android (via Tailscale):"
echo "   http://aexther-lens.tailfeca57.ts.net:3000"
echo ""
echo "💻 Local access:"
echo "   Frontend: http://localhost:3000"
echo "   Backend:  http://localhost:8080"
echo "   Swagger:  http://localhost:8080/swagger-ui.html"
echo ""
echo "📊 View logs:"
echo "   Backend:  tail -f backend.log"
echo "   Frontend: tail -f frontend.log"
echo "   Database: docker compose -f docker/dev-database.yml logs -f"
echo ""
echo "🛑 Stop services:"
echo "   ./stop-dev.sh"
echo ""

# Save PIDs for stop script
echo "$BACKEND_PID" > .backend.pid
echo "$FRONTEND_PID" > .frontend.pid
