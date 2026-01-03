# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

## Project Overview

Rumantra is a full-stack architecture marketplace platform connecting architects and clients. Built with Vue 3 frontend and Spring Boot backend.

## Development Commands

### Frontend (Vue 3 + Vite)
```bash
cd frontend
npm install                 # Install dependencies
npm run dev                 # Start dev server (http://localhost:3000)
npm run build              # Build for production
npm run preview            # Preview production build
npm run lint               # Run ESLint
npm run format             # Format code with Prettier
```

### Backend (Spring Boot + Maven)
```bash
cd backend
mvn clean package          # Build the project
mvn spring-boot:run        # Run application (http://localhost:8080)
mvn test                   # Run tests
mvn spotless:check         # Check code formatting
mvn spotless:apply         # Apply code formatting (required before commits)
```

### Database (PostgreSQL)
```bash
# Start PostgreSQL database
docker compose -f docker/dev-database.yml up -d

# Stop and remove database (resets data)
docker compose -f docker/dev-database.yml down -v

# View database logs
docker compose -f docker/dev-database.yml logs postgres
```

### Full Stack
```bash
# Start entire application stack (if main docker-compose.yml exists)
docker compose up --build

# Build with Docker BuildKit for faster builds
DOCKER_BUILDKIT=1 docker compose build
```

## Architecture

### Frontend Stack
- **Framework**: Vue 3 with Composition API
- **Build Tool**: Vite with hot reload
- **Styling**: Tailwind CSS with custom design system
- **State Management**: Pinia stores
- **Routing**: Vue Router with navigation guards
- **HTTP Client**: Axios with API proxy to backend
- **Validation**: VeeValidate with custom rules
- **UI Components**: Headless UI + custom components

### Backend Stack
- **Framework**: Spring Boot 3.1.5 with Java 17
- **Database**: PostgreSQL with JPA/Hibernate
- **Security**: Spring Security + JWT tokens
- **Authentication**: Custom JWT + Google OAuth2
- **Database Migration**: Flyway
- **API Documentation**: SpringDoc OpenAPI (Swagger UI)
- **Code Formatting**: Spotless with Google Java Format
- **Object Mapping**: MapStruct for DTOs

### Domain Architecture
The backend follows domain-driven design with these modules:
- **User**: Base user authentication and profile management
- **Architect**: Architect profiles, portfolios, and bidding capabilities
- **Client**: Client profiles and project management
- **Bidding**: Project bidding system between architects and clients
- **Payment**: Payment processing integration
- **Security**: JWT authentication, OAuth2, and user authorization

### Key Configuration
- **Frontend Port**: 3000 (proxies `/api` requests to backend)
- **Backend Port**: 8080 (API endpoints under `/api`)
- **Database**: PostgreSQL on port 5432 (container: `rumantra-database`)
- **Database Name**: `rumantra-db`

## File Structure Patterns

### Frontend Structure
```
frontend/src/
├── components/           # Reusable Vue components
│   ├── ui/              # Base UI components (Button, Input, etc.)
│   └── auth/            # Authentication components
├── views/               # Page components
│   ├── architects/      # Architect-related pages
│   ├── clients/         # Client dashboard and pages
│   ├── projects/        # Project listing and management
│   └── auth/            # Login/register pages
├── stores/              # Pinia state management
├── services/            # API services and HTTP configuration
├── router/              # Vue Router configuration
└── locales/             # i18n translation files
```

### Backend Structure
```
backend/src/main/java/com/rumantra/
├── [module]/
│   ├── controller/      # REST controllers
│   ├── service/         # Business logic
│   ├── repository/      # JPA repositories
│   ├── domain/          # JPA entities
│   └── dto/             # Data transfer objects
├── config/              # Spring configuration
├── security/            # Authentication and authorization
└── shared/              # Common utilities and exceptions
```

## Development Guidelines

### Code Formatting
- **Frontend**: ESLint + Prettier (run `npm run format`)
- **Backend**: Spotless + Google Java Format (run `mvn spotless:apply`)
- **Required**: Format code before committing

### Code Style & Comments
- **No Unnecessary Comments**: Do not add comments unless they explain critical business logic or complex algorithms
- **Self-Documenting Code**: Write clear, readable code that explains itself through proper naming
- **Business Logic Comments Only**: Comments should explain WHY something is done, not WHAT is being done
- **Example of Good Comment**: `// Deduct quota only after bid validation succeeds to prevent race conditions`
- **Example of Bad Comment**: `// Create bid object` or `// Save to database`

### Authentication & Authorization Flow

#### User Registration & Login
1. **Registration**: User registers with email → Creates base `User` entity (no roles yet)
2. **Email Verification**: User verifies email before login is allowed
3. **Login**: Returns JWT token + list of active roles (`registeredRoles` array)
4. **OAuth2**: Google and LinkedIn OAuth2 available (auto-creates Client role)

#### Role System
- **Roles**: `ARCHITECT`, `CLIENT`, `ADMIN`, `SUPERUSER` (defined in `RumantraConstants`)
- **Role Storage**:
  - ARCHITECT/CLIENT roles are determined by existence of `rmtr_architect` and `rmtr_client` records
  - SUPERUSER role is determined by `is_superuser` boolean flag in `rmtr_user` table
- **Multi-Role Support**: Users can have both ARCHITECT and CLIENT roles simultaneously
- **Role Activation**: Users activate roles via `POST /rmtr/users/me/activate-role?role={ARCHITECT|CLIENT}`
- **Superuser Assignment**: Superuser role must be assigned manually via database update (privileged role)

#### Role Activation Flow
1. User logs in with no roles → Receives empty `registeredRoles` array
2. User tries to access protected endpoint (e.g., `POST /api/portos`)
3. Frontend detects missing role → Shows "Activate Architect Role" modal
4. User clicks activate → Frontend calls `POST /rmtr/users/me/activate-role?role=ARCHITECT`
5. Backend creates `Architect` record linked to user
6. Next request: JWT filter loads user with EAGER-fetched roles → User now has `ROLE_ARCHITECT`
7. Spring Security allows access to architect endpoints

#### Security Architecture
1. **JWT Validation**: `JwtAuthenticationFilter` validates token and extracts email
2. **User Loading**: `UserDetailsService` loads `User` entity from database by email
3. **Role Loading**: `User` entity has EAGER `@OneToOne` relationships to `Architect` and `Client`
4. **UserPrincipal Creation**: `UserPrincipal.create()` dynamically assigns roles based on relationships:
   - If `user.getArchitect() != null` → Add `ROLE_ARCHITECT`
   - If `user.getClient() != null` → Add `ROLE_CLIENT`
5. **SecurityContext Storage**: `UserPrincipal` stored in ThreadLocal (per-request, server-side)
6. **Spring Security Check**: Enforces role requirements defined in `SecurityConfig`
7. **Controller Access**: Controllers use `SecurityUtils.getCurrentUserId()` for ownership verification

#### Ownership Verification Pattern
All protected resources verify ownership using the authenticated user's ID:

```java
// Example from PortoService
Long userId = SecurityUtils.getCurrentUserId();
Architect architect = architectRepository.findByUserId(userId)
    .orElseThrow(() -> new RuntimeException("Please activate architect role first"));

// Verify ownership
if (!resource.getOwner().getUser().getId().equals(userId)) {
    throw new AccessDeniedException("Not your resource");
}
```

#### Key Security Points
- **Stateless**: No session storage, JWT sent with every request
- **Role-based Access**: Spring Security's `hasRole()` enforces endpoint access
- **Ownership Verification**: Double-check using `SecurityUtils.getCurrentUserId()`
- **No Path Parameter Exploitation**: User IDs come from JWT, not request paths
- **ThreadLocal Isolation**: Each request has isolated security context

### API Integration
- Frontend API service in `src/services/api.js`
- Backend controllers return standardized `ApiResponse<T>` DTOs
- Error handling via global exception handler

### Database Changes
- Create Flyway migration files in `backend/src/main/resources/db/migration/`
- Follow naming pattern: `V{version}__{description}.sql`
- Update JPA entities and DTOs accordingly

### Testing
- **Backend**: JUnit tests for controllers, services, and repositories
- **Frontend**: Component testing setup available
- **Database**: H2 in-memory database for tests

### Component Development
- Use existing UI components in `frontend/src/components/ui/`
- Follow Tailwind CSS classes and design system
- Implement proper form validation with VeeValidate

## Common Development Tasks

### Adding New API Endpoint
1. Create/update DTO in appropriate module
2. Add method to controller class
3. Implement business logic in service
4. Add repository method if needed
5. Use `SecurityUtils.getCurrentUserId()` for ownership verification
6. Update frontend API service
7. Format code: `mvn spotless:apply`

### Subscription & Payment Flow

#### Subscription Tiers
- **FREE**: IDR 0/year - 1 bid token per year
- **BASIC**: IDR 1,500,000/year - 10 bid tokens per year
- Tokens accumulate (unused tokens carry over until subscription ends)
- Tokens are allocated once per subscription year, not monthly

#### Yearly Subscription Cycle
1. User initiates upgrade to BASIC tier
2. Backend creates Xendit recurring payment plan (yearly interval)
3. User redirects to Xendit checkout, completes payment
4. Xendit sends webhook: `recurring.payment.succeeded`
5. Backend allocates 10 bid tokens, activates subscription
6. After 365 days: Xendit auto-charges payment method
7. Webhook triggers another 10 tokens allocation
8. Cycle repeats annually

#### Bid Token System
- Tokens are consumed when placing bids (1 token = 1 bid)
- Tokens are refunded if project is cancelled before bid acceptance
- Token balance tracked in `rmtr_bid_quota` table
- All token changes logged in `rmtr_bid_usage_log` for audit trail

#### Individual Token Purchase
1. Architect requests pricing (tier-based: FREE=IDR 400k, BASIC=IDR 250k per token)
2. Architect initiates purchase (1-50 tokens)
3. Backend creates Xendit one-time payment request
4. User redirects to Xendit checkout, completes payment
5. Xendit sends webhook: `payment.succeeded`
6. Backend allocates purchased tokens immediately
7. Tokens added to quota (no expiration)

### Key API Endpoints

#### Subscription Management (Architect Role Required)
- `POST /api/subscriptions/upgrade` - Initiate upgrade to BASIC tier (returns Xendit payment link)
- `GET /api/subscriptions/status` - Get current subscription status
- `POST /api/subscriptions/cancel` - Cancel subscription (benefits continue until endDate)
- `POST /api/subscriptions/webhook` - Xendit webhook handler (public, signature-verified)

#### Token Purchase (Architect Role Required)
- `GET /tokens/purchases/pricing` - Get tier-based pricing info
- `POST /tokens/purchases` - Initiate token purchase (returns Xendit payment link)
- `GET /tokens/purchases/{id}` - Get purchase details (ownership verified)
- `GET /tokens/purchases/history` - Get paginated purchase history
- `POST /tokens/purchases/webhook` - Xendit payment webhook handler (public, signature-verified)

#### Authentication & User Management
- `POST /rmtr/users/register` - Register new user (public)
- `POST /rmtr/users/login` - Login (public)
- `GET /rmtr/users/verify-email?token={token}` - Verify email (public)
- `POST /rmtr/users/me/activate-role?role={ARCHITECT|CLIENT}` - Activate role (authenticated)
- `GET /rmtr/users/oauth2/google` - Google OAuth2 login (public)
- `GET /rmtr/users/oauth2/linkedin` - LinkedIn OAuth2 login (public)

#### Portfolio Management (Architect Role Required)
- `POST /api/portos` - Create portfolio (uses authenticated user's architect ID)
- `GET /api/portos` - Get all portfolios for authenticated architect
- `GET /api/portos/{portoId}` - Get portfolio by ID (ownership verified)
- `PUT /api/portos/{portoId}` - Update portfolio (ownership verified)
- `DELETE /api/portos/{portoId}` - Delete portfolio (ownership verified)
- `POST /api/portos/{portoId}/images` - Add images to portfolio (ownership verified)
- `DELETE /api/portos/images/{imageId}` - Delete image (ownership verified)

#### Project Management (Client Role Required)
- `POST /api/v1/projects` - Create project (uses authenticated user's client ID)
- `GET /api/v1/projects` - Get all projects for authenticated client
- `GET /api/v1/projects/{projectId}` - Get project by ID (ownership verified)
- `DELETE /api/v1/projects/{projectId}` - Delete project (ownership verified)

#### Project Validation (Superuser Role Required)
- `PUT /api/v1/projects/{projectId}/validate` - Update project validation status (superuser only, no ownership check)
  - Request body: `{ "isValid": true/false }`
- `GET /api/v1/projects/all` - Get all projects regardless of validation status (superuser only)

### Adding New Page
1. Create Vue component in `frontend/src/views/`
2. Add route in `frontend/src/router/index.js`
3. Update navigation if needed
4. Format code: `npm run format`

### Database Schema Changes
1. Create new Flyway migration file
2. Update JPA entities
3. Update DTOs and mappers
4. Test migration locally

### Assigning Superuser Role
Superuser is a privileged role that must be assigned manually via database:

```sql
-- Assign superuser role to a user
UPDATE rmtr_user SET is_superuser = true WHERE email = 'admin@rumantra.com';

-- Remove superuser role from a user
UPDATE rmtr_user SET is_superuser = false WHERE email = 'admin@rumantra.com';

-- Check who has superuser role
SELECT id, email, is_superuser FROM rmtr_user WHERE is_superuser = true;
```

After updating the database, the user needs to login again (or obtain a new JWT token) for the role to take effect.

### Environment Setup
- **Java**: Version 17+ required
- **Node**: Version 18+ required
- **Database**: PostgreSQL via Docker recommended
- **Maven**: For backend dependency management
- **Docker**: For database and full-stack deployment