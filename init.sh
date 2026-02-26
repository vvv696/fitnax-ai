#!/bin/bash
set -e

PROJECT_ROOT="$(cd "$(dirname "$0")" && pwd)"

echo "========================================="
echo "  Fintax AI - Development Server Startup"
echo "========================================="

# Colors
GREEN='\033[0;32m'
RED='\033[0;31m'
YELLOW='\033[1;33m'
NC='\033[0m'

# Kill existing processes on ports 8080 and 5173
echo ""
echo "Cleaning up existing processes..."
lsof -ti:8080 | xargs kill -9 2>/dev/null || true
lsof -ti:5173 | xargs kill -9 2>/dev/null || true
sleep 1

# ===== Backend =====
echo ""
echo "----- Starting Backend (Spring Boot) -----"
cd "$PROJECT_ROOT/backend"

# Build (skip tests for fast startup)
echo "Building backend..."
./mvnw clean compile -q 2>/dev/null || mvn clean compile -q

# Start Spring Boot in background
echo "Starting Spring Boot on port 8080..."
nohup ./mvnw spring-boot:run -q 2>/dev/null || nohup mvn spring-boot:run -q &
BACKEND_PID=$!
echo "Backend PID: $BACKEND_PID"

# Wait for backend to be ready
echo "Waiting for backend to start..."
for i in $(seq 1 60); do
    if curl -s http://localhost:8080/api/health > /dev/null 2>&1; then
        echo -e "${GREEN}Backend is ready!${NC}"
        break
    fi
    if [ $i -eq 60 ]; then
        echo -e "${RED}Backend failed to start within 60 seconds${NC}"
        exit 1
    fi
    sleep 2
done

# ===== Frontend =====
echo ""
echo "----- Starting Frontend (Vite) -----"
cd "$PROJECT_ROOT/frontend"

# Install dependencies if needed
if [ ! -d "node_modules" ]; then
    echo "Installing frontend dependencies..."
    npm install
fi

# Start Vite dev server in background
echo "Starting Vite dev server on port 5173..."
nohup npm run dev > /dev/null 2>&1 &
FRONTEND_PID=$!
echo "Frontend PID: $FRONTEND_PID"

# Wait for frontend to be ready
echo "Waiting for frontend to start..."
for i in $(seq 1 30); do
    if curl -s http://localhost:5173 > /dev/null 2>&1; then
        echo -e "${GREEN}Frontend is ready!${NC}"
        break
    fi
    if [ $i -eq 30 ]; then
        echo -e "${RED}Frontend failed to start within 30 seconds${NC}"
        exit 1
    fi
    sleep 2
done

# ===== Basic E2E Test =====
echo ""
echo "----- Running Basic E2E Tests -----"

# Test 1: Backend health check
echo -n "Test 1 - Backend health check: "
HEALTH=$(curl -s http://localhost:8080/api/health)
if echo "$HEALTH" | grep -q '"code":200'; then
    echo -e "${GREEN}PASSED${NC}"
else
    echo -e "${RED}FAILED${NC} - Response: $HEALTH"
fi

# Test 2: Frontend accessible
echo -n "Test 2 - Frontend accessible: "
FRONTEND_RESPONSE=$(curl -s -o /dev/null -w "%{http_code}" http://localhost:5173)
if [ "$FRONTEND_RESPONSE" = "200" ]; then
    echo -e "${GREEN}PASSED${NC}"
else
    echo -e "${RED}FAILED${NC} - HTTP status: $FRONTEND_RESPONSE"
fi

echo ""
echo "========================================="
echo -e "${GREEN}  Fintax AI is running!${NC}"
echo "  Backend:  http://localhost:8080"
echo "  Frontend: http://localhost:5173"
echo "========================================="
