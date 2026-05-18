# RUMANTRA

An architecture marketplace platform connecting architects and clients. Built with Vue 3 frontend and Spring Boot backend.

## Project Structure

- `frontend2/` — **Active frontend** (Vue 3 + Vite, port 3001)
- `backend/` — Spring Boot API (port 8080)
- `docker/` — Docker Compose configs for local development

> `frontend/` is legacy and unused — always work in `frontend2/`.

## Prerequisites

- Docker & Docker Compose
- Java 17+
- Maven
- Node 18+

### Installation

#### Mac
```bash
brew install openjdk@17 maven
brew install --cask docker
```

#### Windows
- [Java 17 JDK](https://adoptium.net/)
- [Maven](https://maven.apache.org/download.cgi)
- [Docker Desktop](https://www.docker.com/products/docker-desktop/)

#### Linux (Ubuntu/Debian)
```bash
sudo apt-get update
sudo apt-get install openjdk-17-jdk maven docker.io docker-compose
sudo usermod -aG docker $USER  # avoid sudo for docker — log out/in after
```

### Verify Installations
```bash
java --version
mvn --version
docker --version
node --version
```

## Local Development

### Quick Start

```bash
./start-dev.sh   # starts database, backend (8080), and frontend (3001)
./status-dev.sh  # check running services
./stop-dev.sh    # stop all services
```

### Manual Setup

#### 1. Database (PostgreSQL via Docker)
```bash
docker compose -f docker/dev-database.yml up -d
```
Starts PostgreSQL on port 5432. To stop: `docker compose -f docker/dev-database.yml down`.
To reset all data: `docker compose -f docker/dev-database.yml down -v`.

#### 2. Backend (Spring Boot)
```bash
cd backend
mvn clean package
mvn spring-boot:run
# API available at http://localhost:8080
```

#### 3. Frontend (Vue 3)
```bash
cd frontend2
npm install       # first time only
npm run dev
# App available at http://localhost:3001
```

### Full Stack with Docker Compose
```bash
docker compose up --build
# or faster: DOCKER_BUILDKIT=1 docker compose build
```

## Development Commands

### Frontend
```bash
cd frontend2
npm run dev       # dev server
npm run build     # production build
npm run lint      # ESLint
npm run format    # Prettier
```

### Backend
```bash
cd backend
export $(cat .env | grep -v '^#' | xargs) && mvn spring-boot:run       # run app
mvn test                   # run tests
mvn spotless:apply         # format code (required before commits)
```

## Architecture

- **Frontend**: Vue 3 + Composition API, Tailwind CSS, Pinia, Vue Router, Axios
- **Backend**: Spring Boot 3.1.5, PostgreSQL, JPA/Hibernate, Spring Security + JWT, Flyway
- **Auth**: Custom JWT + Google OAuth2, role-based access (ARCHITECT, CLIENT, SUPERUSER)
- **Payments**: Xendit (subscriptions + one-time token purchases + phase invoicing)
- **Storage**: Local (dev), Railway S3-compatible (prod), Cloudinary (fallback)

### Core Platform Flow

1. Client creates a project → Superuser validates → project opens for bidding
2. Architects bid with a payment phase schedule
3. Client accepts a bid → negotiation → both confirm → project goes IN_PROGRESS
4. Client pays phase invoices via Xendit → architect works and uploads deliverables
5. Client approves work → architect requests payout → repeat per phase
6. All phases complete → project auto-closes as COMPLETED

## Debugging

```bash
# View container logs
docker compose logs

# View database logs
docker compose -f docker/dev-database.yml logs postgres

# Backend with remote debug (port 5005)
cd backend && mvn spring-boot:run -Dspring-boot.run.jvmArguments="-Xdebug -Xrunjdwp:transport=dt_socket,server=y,suspend=n,address=5005"
```

## Security Notes

- Replace default passwords in `application.properties` before deploying
- Use environment-specific configurations for production secrets
- JWT secret keys should be randomly generated per environment
