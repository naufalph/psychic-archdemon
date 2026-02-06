# RUMANTRA

## Prerequisites

### Software Requirements
- Docker
- Docker Compose
- Java 17+
- Maven

### Installation Guide

#### Mac (Intel/Apple Silicon)
1. **Homebrew Installation (Recommended)**
   ```bash
   # Install Homebrew (if not already installed)
   /bin/bash -c "$(curl -fsSL https://raw.githubusercontent.com/Homebrew/install/HEAD/install.sh)"

   # Install Java 17
   brew install openjdk@17

   # Install Maven
   brew install maven

   # Install Docker Desktop
   brew install --cask docker
   ```

#### Windows
1. **Recommended Method: Windows Installer**
   - Download and Install [Java 17 JDK](https://adoptium.net/)
   - Download and Install [Maven](https://maven.apache.org/download.cgi)
   - Download and Install [Docker Desktop](https://www.docker.com/products/docker-desktop/)


#### Linux (Ubuntu/Debian)
1. **System Package Manager**
   ```bash
   # Update package list
   sudo apt-get update

   # Install Java 17
   sudo apt-get install openjdk-17-jdk

   # Install Maven
   sudo apt-get install maven

   # Install Docker
   sudo apt-get install docker.io docker-compose

   # Add current user to docker group (avoid sudo for docker)
   sudo usermod -aG docker $USER
   ```

### Post-Installation Checklist

1. **Verify Installations**
   ```bash
   # Open Terminal/Command Prompt and run:
   java --version
   mvn --version
   docker --version
   ```

2. **First-Time Setup Tips**
   - Restart your computer after installations
   - Log out and log back in after adding user to docker group (Linux)
   - For Windows, restart Docker Desktop if prompted

## Local Development Setup

### Quick Start (Recommended)

Start all services (database, backend, frontend) with a single command:
```bash
# Start all services
./start-dev.sh

# Check status
./status-dev.sh

# Stop all services
./stop-dev.sh
```

The `start-dev.sh` script will:
- Start PostgreSQL database via Docker Compose
- Start Spring Boot backend on port 8080
- Start Vue 3 frontend on port 3000
- Display logs from all services
- Press `Ctrl+C` to stop all services

### Manual Setup

#### Database Configuration

##### Option 1: Docker Compose (Recommended)
```bash
# Start PostgreSQL database
docker compose -f docker/dev-database.yml up -d
```

##### Option 2: Manual PostgreSQL Setup
1. Install PostgreSQL locally
2. Create database: `architecture_marketplace`
3. Use credentials from `backend/src/main/resources/application.properties`

#### Running the Application

##### Backend (Spring Boot)
```bash
# Navigate to backend directory
cd backend

# Build the project
mvn clean package

# Run Spring Boot application
mvn spring-boot:run

mvn spring-boot:run -Dspring-boot.run.jvmArguments="-Xdebug -Xrunjdwp:transport=dt_socket,server=y,suspend=n,address=5005"
```

##### Frontend (Vue 3)
```bash
# Navigate to frontend directory
cd frontend2

# Install dependencies (first time only)
npm install

# Start development server
npm run dev
```

##### Full Stack with Docker Compose
```bash
# Start entire application stack
docker compose up --build
```

## Development Workflow

### Database Management
- Persistent volume ensures data preservation
- Reset database: `docker compose -f docker/dev-database.yml down -v`

### Debugging
```bash
# View container logs
docker compose logs

# Inspect specific service
docker compose logs postgres
```

## Performance & Security

### Docker Optimization
- Use BuildKit for faster builds:
  ```bash
  DOCKER_BUILDKIT=1 docker compose build
  ```

### Security Recommendations
- Replace default passwords in `application.properties`
- Use environment-specific configurations
- Generate secure JWT secret keys

## Troubleshooting

- Ensure Docker daemon is running
- Check port conflicts (default: 5432)
- Verify network connectivity

## Alternative Container Runtimes

### Podman (Daemonless Alternative)
```bash
# Install Podman
sudo apt-get install podman

# Run database
podman-compose -f docker/dev-database.yml up -d
```

## License

[Specify Project License]
