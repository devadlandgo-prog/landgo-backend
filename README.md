# LandGo Backend

A Spring Boot backend API for the LandGo land listing platform with vendor management, user subscriptions, and OAuth2 authentication.

## 🏗️ Architecture Overview

### Application Architecture

```
┌──────────────────────────────────────────────────────────────┐
│                      CLIENT (Mobile App / Web / Postman)     │
└──────────────────────────┬───────────────────────────────────┘
                           │ HTTP Request
                           ▼
┌──────────────────────────────────────────────────────────────┐
│                   Security Filter Chain                      │
│  ┌─────────────────────────────────────────────────────────┐ │
│  │  JwtAuthenticationFilter                                │ │
│  │  • Extracts JWT from Authorization header               │ │
│  │  • Validates token (signature, expiry)                  │ │
│  │  • Sets SecurityContext with UserPrincipal              │ │
│  └─────────────────────────────────────────────────────────┘ │
└──────────────────────────┬───────────────────────────────────┘
                           │
                           ▼
┌──────────────────────────────────────────────────────────────┐
│                      Controllers                             │
│  AuthController │ LandController │ VendorController │ Sub.. │
└──────────────────────────┬───────────────────────────────────┘
                           │
                           ▼
┌──────────────────────────────────────────────────────────────┐
│                       Services                               │
│  AuthService │ LandService │ VendorService │ SubscriptionSvc│
└──────────────────────────┬───────────────────────────────────┘
                           │
                           ▼
┌──────────────────────────────────────────────────────────────┐
│      Mappers (MapStruct) │ Repositories (Spring Data JPA)   │
│  UserMapper │ LandMapper │ VendorMapper │ SubscriptionMapper│
└──────────────────────────┬───────────────────────────────────┘
                           │
                           ▼
┌──────────────────────────────────────────────────────────────┐
│                    PostgreSQL Database                        │
│  users │ vendor_profiles │ lands │ subscriptions │ saved..   │
└──────────────────────────────────────────────────────────────┘
```

### AWS Deployment Architecture

```
┌─────────────────────────────────────────────────────────────────┐
│                         AWS Cloud                                │
├─────────────────────────────────────────────────────────────────┤
│  ┌─────────────┐    ┌─────────────┐    ┌─────────────┐         │
│  │    Route    │───▶│     ALB     │───▶│  ECS/EKS    │         │
│  │     53      │    │             │    │  Cluster    │         │
│  └─────────────┘    └─────────────┘    └──────┬──────┘         │
│                                                │                 │
│                     ┌──────────────────────────┼─────────┐      │
│                     │                          │         │      │
│               ┌─────▼─────┐            ┌───────▼───────┐ │      │
│               │    RDS    │            │   Secrets     │ │      │
│               │ PostgreSQL│            │   Manager     │ │      │
│               └───────────┘            └───────────────┘ │      │
│                     Private Subnets                      │      │
└─────────────────────────────────────────────────────────────────┘
```

## 🚀 Features

- **User Management**: Register/login with Email, Google, or Apple
- **Password Reset**: Forgot password flow with email-based token verification (30-min expiry, single-use tokens)
- **Vendor System**: Users can register as vendors to list lands
- **Land Listings**: Full CRUD operations for land properties with search, filter, recent & popular listings
- **Subscription Plans**: FREE, BASIC, PREMIUM, ENTERPRISE tiers with feature gating
- **JWT Authentication**: Secure stateless authentication with access (1hr) and refresh (7 days) tokens
- **OAuth2 Integration**: Google and Apple Sign-In via Strategy Pattern
- **API Documentation**: OpenAPI/Swagger UI at `/swagger-ui.html`
- **Role-Based Access**: USER, VENDOR, and ADMIN roles with endpoint-level authorization
- **Soft Deletes**: Land listings support soft deletion

## 🛠️ Tech Stack

| Component | Technology | Version |
|-----------|-----------|---------|
| Framework | Spring Boot | 3.2.2 |
| Language | Java | 21 |
| Database | PostgreSQL | 16 |
| ORM | Spring Data JPA + Hibernate | 6.4.x |
| Migration | Flyway (present, disabled by default) | 10.8.1 |
| Security | Spring Security + JWT (jjwt) | 0.12.5 |
| Documentation | SpringDoc OpenAPI | 2.3.0 |
| Object Mapping | MapStruct | 1.5.5 |
| Boilerplate | Lombok | Managed |
| Build | Maven (with Maven Wrapper) | 3.9+ |
| Containerization | Docker + Docker Compose | - |

## 📁 Project Structure

```
landgo-backend/
├── docs/                        # API documentation & Postman collection
│   ├── API_DOCUMENTATION.md     # Complete API reference
│   ├── API_FLOW_MINDMAP.md      # Visual API flow diagrams
│   └── LandGo_Postman_Collection.json  # Import into Postman
├── aws/                         # AWS deployment configs
│   ├── ecs/                     # ECS Fargate (CloudFormation + Task Definition)
│   └── eks/                     # EKS Kubernetes (Deployment + Secrets)
├── src/main/java/com/landgo/
│   ├── LandGoApplication.java   # Main application entry point
│   ├── config/                  # Configuration classes
│   │   ├── AppConfig.class      # App-wide beans (PasswordEncoder, RestTemplate)
│   │   ├── OpenApiConfig.java   # Swagger/OpenAPI configuration
│   │   └── SecurityConfig.java  # Spring Security + JWT filter chain
│   ├── controller/              # REST API controllers
│   │   ├── AuthController.java          # Authentication (register, login, OAuth2, password reset)
│   │   ├── LandController.java          # Land listings (public + vendor CRUD)
│   │   ├── SubscriptionController.java  # Subscription management
│   │   └── VendorController.java        # Vendor registration & profiles
│   ├── dto/                     # Data Transfer Objects
│   │   ├── request/             # RegisterRequest, LoginRequest, OAuth2Request, etc.
│   │   └── response/            # ApiResponse, AuthResponse, LandResponse, etc.
│   ├── entity/                  # JPA entities
│   │   ├── BaseEntity.java      # Abstract base (id, timestamps, soft-delete)
│   │   ├── User.java            # User with auth, role, relationships
│   │   ├── Land.java            # Land listing with location, specs, media
│   │   ├── Subscription.java    # User subscription with plan & feature limits
│   │   ├── VendorProfile.java   # Vendor business profile
│   │   └── PasswordResetToken.java # Password reset token (UUID, 30-min expiry)
│   ├── enums/                   # AuthProvider, Role, LandType, LandStatus, etc.
│   ├── exception/               # ApiException, GlobalExceptionHandler, etc.
│   ├── factory/                 # OAuth2StrategyFactory
│   ├── mapper/                  # MapStruct mappers (User, Land, Vendor, Subscription)
│   ├── repository/              # Spring Data JPA repositories with custom queries
│   ├── security/                # JWT provider, filter, UserPrincipal, @CurrentUser
│   ├── service/                 # Business logic (Auth, Land, Vendor, Subscription, Email)
│   └── strategy/                # OAuth2 authentication strategies (Google, Apple)
├── src/main/resources/
│   ├── application.yml          # Main configuration
│   ├── application-docker.yml   # Docker profile overrides
│   └── db/migration/            # Flyway SQL migrations
├── docker-compose.yml           # Local dev (app + postgres + pgadmin)
├── Dockerfile                   # Multi-stage build (JDK 21 Alpine)
└── pom.xml                      # Maven dependencies & plugins
```

## 🎯 Design Patterns Used

| Pattern | Usage |
|---------|-------|
| **Strategy** | OAuth2 authentication providers — `GoogleAuthenticationStrategy`, `AppleAuthenticationStrategy` implementing `OAuth2AuthenticationStrategy` interface |
| **Factory** | `OAuth2StrategyFactory` selects the correct strategy based on `AuthProvider` enum |
| **Repository** | Spring Data JPA repositories with custom JPQL queries for search/filter |
| **DTO** | Request/Response DTOs with validation annotations for data transfer between layers |
| **Builder** | Lombok `@SuperBuilder` for entity and DTO construction with `@Builder.Default` for defaults |
| **Template Method** | `BaseEntity` provides common fields (id, timestamps, soft-delete) to all entities |

## 📋 SOLID Principles

- **S**ingle Responsibility: Each service handles one domain (AuthService, LandService, VendorService, SubscriptionService)
- **O**pen/Closed: Strategy pattern for OAuth providers — add new providers without modifying existing code
- **L**iskov Substitution: All OAuth strategies implement `OAuth2AuthenticationStrategy` and are interchangeable
- **I**nterface Segregation: Focused repository interfaces with domain-specific query methods
- **D**ependency Inversion: Constructor injection via `@RequiredArgsConstructor` throughout all layers

## 🚦 API Endpoints

### Authentication (`/api/v1/auth`)
| Method | Endpoint | Auth | Description |
|--------|----------|------|-------------|
| POST | `/api/v1/auth/register` | Public | Register new user with email/password |
| POST | `/api/v1/auth/login` | Public | Login with email/password |
| POST | `/api/v1/auth/oauth2` | Public | OAuth2 login (Google/Apple) |
| POST | `/api/v1/auth/forgot-password` | Public | Request password reset email |
| GET | `/api/v1/auth/reset-password/validate?token=` | Public | Validate reset token |
| POST | `/api/v1/auth/reset-password` | Public | Reset password with token |
| GET | `/api/v1/auth/me` | Bearer | Get current authenticated user |

### Vendor Management (`/api/v1/vendor` & `/api/v1/vendors`)
| Method | Endpoint | Auth | Description |
|--------|----------|------|-------------|
| POST | `/api/v1/vendor/register` | Bearer | Register current user as vendor (changes role to VENDOR) |
| GET | `/api/v1/vendor/profile` | VENDOR | Get own vendor profile |
| PUT | `/api/v1/vendor/profile` | VENDOR | Update own vendor profile |
| GET | `/api/v1/vendors` | Public | List all verified vendors (paginated) |
| GET | `/api/v1/vendors/{id}` | Bearer + Paid Sub | Get vendor details (requires non-FREE subscription) |
| GET | `/api/v1/vendors/search?query=` | Public | Search vendors by name/city |

### Land Listings — Public (`/api/v1/lands`)
| Method | Endpoint | Auth | Description |
|--------|----------|------|-------------|
| GET | `/api/v1/lands` | Public | Browse all active land listings (paginated) |
| GET | `/api/v1/lands/{id}` | Public | Get land details (increments view count) |
| GET | `/api/v1/lands/search?query=` | Public | Search lands by title/city/state |
| GET | `/api/v1/lands/filter?city=&type=&minPrice=&maxPrice=` | Public | Filter lands by criteria |
| GET | `/api/v1/lands/recent?limit=10` | Public | Get most recent active listings |
| GET | `/api/v1/lands/popular?limit=10` | Public | Get most viewed active listings |

### Land Listings — Vendor (`/api/v1/vendor/lands`)
| Method | Endpoint | Auth | Description |
|--------|----------|------|-------------|
| POST | `/api/v1/vendor/lands` | VENDOR | Create new land listing (status: PENDING_APPROVAL) |
| GET | `/api/v1/vendor/lands` | VENDOR | Get own land listings (all statuses, paginated) |
| PUT | `/api/v1/vendor/lands/{id}` | VENDOR | Update own land listing |
| DELETE | `/api/v1/vendor/lands/{id}` | VENDOR | Soft-delete own land listing |

### Subscriptions (`/api/v1/subscriptions`)
| Method | Endpoint | Auth | Description |
|--------|----------|------|-------------|
| POST | `/api/v1/subscriptions` | Bearer | Subscribe to a plan (FREE/BASIC/PREMIUM/ENTERPRISE) |
| GET | `/api/v1/subscriptions/current` | Bearer | Get current active subscription |
| POST | `/api/v1/subscriptions/cancel?reason=` | Bearer | Cancel current subscription |

### Subscription Plans

| Plan | Price | Duration | Vendor Views/mo | Saved Lands | Premium Access | Direct Contact |
|------|-------|----------|----------------|-------------|----------------|----------------|
| FREE | $0 | Unlimited | 5 | 10 | ❌ | ❌ |
| BASIC | $9.99 | 30 days | 20 | 50 | ❌ | ✅ |
| PREMIUM | $29.99 | 30 days | 100 | 200 | ✅ | ✅ |
| ENTERPRISE | $99.99 | 365 days | Unlimited | Unlimited | ✅ | ✅ |

### Utility
| Method | Endpoint | Auth | Description |
|--------|----------|------|-------------|
| GET | `/actuator/health` | Public | Application health check |
| GET | `/actuator/info` | Public | Application info |
| GET | `/actuator/metrics` | Public | Application metrics |
| GET | `/actuator/prometheus` | Public | Prometheus metrics |
| GET | `/swagger-ui.html` | Public | Interactive API documentation |
| GET | `/v3/api-docs` | Public | OpenAPI JSON specification |

> **Total: 27 endpoints** across 4 controllers + actuator + Swagger

### 📋 API Quick Reference (Copy-Paste Ready)

```
AUTH
  POST   /api/v1/auth/register                                    Public
  POST   /api/v1/auth/login                                       Public
  POST   /api/v1/auth/oauth2                                      Public
  POST   /api/v1/auth/forgot-password                             Public
  GET    /api/v1/auth/reset-password/validate?token=              Public
  POST   /api/v1/auth/reset-password                              Public
  GET    /api/v1/auth/me                                          Bearer

VENDOR
  POST   /api/v1/vendor/register                                  Bearer
  GET    /api/v1/vendor/profile                                   VENDOR
  PUT    /api/v1/vendor/profile                                   VENDOR
  GET    /api/v1/vendors                                          Public
  GET    /api/v1/vendors/{id}                                     Bearer + Paid Sub
  GET    /api/v1/vendors/search?query=                            Public

LANDS (Public)
  GET    /api/v1/lands                                            Public
  GET    /api/v1/lands/{id}                                       Public
  GET    /api/v1/lands/search?query=                              Public
  GET    /api/v1/lands/filter?city=&type=&minPrice=&maxPrice=     Public
  GET    /api/v1/lands/recent?limit=                              Public
  GET    /api/v1/lands/popular?limit=                             Public

LANDS (Vendor)
  POST   /api/v1/vendor/lands                                     VENDOR
  GET    /api/v1/vendor/lands                                     VENDOR
  PUT    /api/v1/vendor/lands/{id}                                VENDOR
  DELETE /api/v1/vendor/lands/{id}                                VENDOR

SUBSCRIPTIONS
  POST   /api/v1/subscriptions                                    Bearer
  GET    /api/v1/subscriptions/current                            Bearer
  POST   /api/v1/subscriptions/cancel?reason=                     Bearer

UTILITY
  GET    /actuator/health                                         Public
  GET    /actuator/info                                           Public
  GET    /swagger-ui.html                                         Public
  GET    /v3/api-docs                                             Public
```

## 🔧 Local Development

### Prerequisites

- **Java 21** (required)
- **PostgreSQL 16** running on `localhost:5432` with database `landgo` created
- **Maven 3.9+** (or use the included Maven Wrapper `./mvnw`)
- **Docker & Docker Compose** (optional — for containerized setup)

### Option 1: Run Locally with Maven

```bash
# 1. Clone the repository
git clone https://github.com/devadlandgo-prog/landgo-backend.git
cd landgo-backend

# 2. Make sure PostgreSQL is running and create the database
# macke sure while running the data base locally run the data base container before running the maven build command. 
docker-compose up -d postgres pgadmin


# 3. Build and run

# macOS / Linux
./mvnw clean compile spring-boot:run

# Windows (Command Prompt / PowerShell)
mvnw.cmd clean compile spring-boot:run

# 4. To Stop the app running in local 
lsof -ti:8080 | xargs kill -9 2>/dev/null && echo "App stopped" || echo "No process found on port 8080"

#stop local database 
docker-compose down

```

The app starts on `http://localhost:8080`. Tables are auto-created by Hibernate (`ddl-auto: create-drop`).

### Option 2: Run with Docker Compose

```bash
# Start all services (app + postgres + pgadmin)
docker-compose up -d

# Or start only database services (for local dev)
docker-compose up -d postgres pgadmin
```

| Service | URL | Credentials |
|---------|-----|-------------|
| API | http://localhost:8080 | - |
| PostgreSQL | localhost:5432 | postgres / postgres |
| pgAdmin | http://localhost:5050 | admin@landgo.com / admin |

### Environment Variables

| Variable | Description | Default |
|----------|-------------|---------|
| `DB_HOST` | Database host | `localhost` |
| `DB_PORT` | Database port | `5432` |
| `DB_NAME` | Database name | `landgo` |
| `DB_USERNAME` | Database user | `postgres` |
| `DB_PASSWORD` | Database password | `postgres` |
| `SERVER_PORT` | Application port | `8080` |
| `JWT_SECRET` | JWT signing key (HS256, min 256 bits) | Dev default provided |
| `JWT_ACCESS_EXPIRATION` | Access token expiry (ms) | `3600000` (1 hour) |
| `JWT_REFRESH_EXPIRATION` | Refresh token expiry (ms) | `604800000` (7 days) |
| `GOOGLE_CLIENT_ID` | Google OAuth2 client ID | *(empty)* |
| `GOOGLE_CLIENT_SECRET` | Google OAuth2 client secret | *(empty)* |
| `APPLE_CLIENT_ID` | Apple OAuth2 client ID | *(empty)* |
| `APPLE_CLIENT_SECRET` | Apple OAuth2 client secret | *(empty)* |
| `CORS_ORIGINS` | Allowed CORS origins | `http://localhost:3000,http://localhost:8080` |
| `MAIL_HOST` | SMTP server host | `smtp.gmail.com` |
| `MAIL_PORT` | SMTP server port | `587` |
| `MAIL_USERNAME` | SMTP username/email | *(empty)* |
| `MAIL_PASSWORD` | SMTP password / app password | *(empty)* |
| `APP_MAIL_FROM` | "From" address for reset emails | `noreply@landgo.com` |
| `APP_MAIL_RESET_URL` | Password reset frontend URL | `http://localhost:3000/reset-password` |

### API Documentation & Testing

| Resource | Location |
|----------|----------|
| Swagger UI | http://localhost:8080/swagger-ui.html |
| OpenAPI JSON | http://localhost:8080/v3/api-docs |
| API Documentation | [`docs/API_DOCUMENTATION.md`](docs/API_DOCUMENTATION.md) |
| API Flow Mind Map | [`docs/API_FLOW_MINDMAP.md`](docs/API_FLOW_MINDMAP.md) |
| Postman Collection | [`docs/LandGo_Postman_Collection.json`](docs/LandGo_Postman_Collection.json) — import directly into Postman |

### Postman Quick Start

1. Open **Postman** → click **Import**
2. Select `docs/LandGo_Postman_Collection.json`
3. Run requests in order: **Register → Login → Subscribe → Vendor Register → Re-Login → Create Land**
4. Tokens and IDs are auto-saved between requests via test scripts

## 🚀 Deployment

### AWS ECS (Fargate)

```bash
# Build and push Docker image
aws ecr get-login-password --region us-east-1 | docker login --username AWS --password-stdin ${AWS_ACCOUNT_ID}.dkr.ecr.us-east-1.amazonaws.com

docker build -t landgo-api .
docker tag landgo-api:latest ${AWS_ACCOUNT_ID}.dkr.ecr.us-east-1.amazonaws.com/landgo-api:latest
docker push ${AWS_ACCOUNT_ID}.dkr.ecr.us-east-1.amazonaws.com/landgo-api:latest

# Deploy CloudFormation stack
aws cloudformation deploy \
  --template-file aws/ecs/cloudformation.yml \
  --stack-name landgo-backend \
  --parameter-overrides Environment=prod \
  --capabilities CAPABILITY_IAM
```

### AWS EKS

```bash
# Configure kubectl
aws eks update-kubeconfig --name landgo-cluster --region us-east-1

# Apply Kubernetes manifests
kubectl apply -f aws/eks/secrets.yaml
kubectl apply -f aws/eks/deployment.yaml
```

## 🧪 Testing

```bash
# Run all tests
./mvnw test

# Run with coverage
./mvnw test jacoco:report
```

## 📊 Monitoring

| Endpoint | Description |
|----------|-------------|
| `/actuator/health` | Application health + database connectivity |
| `/actuator/info` | Application info |
| `/actuator/metrics` | Spring Boot metrics |
| `/actuator/prometheus` | Prometheus-format metrics |

## ⚠️ Important Notes

- **Database mode**: Currently uses `ddl-auto: create-drop` — tables are recreated on every restart. Change to `update` or enable Flyway for data persistence in production.
- **Flyway**: Present in dependencies but disabled (`flyway.enabled: false`). Migration file exists at `db/migration/V1__Initial_Schema.sql`.
- **Vendor Registration**: After registering as a vendor, the user's role changes from `USER` to `VENDOR`. You **must re-login** to get a new JWT token with the updated role.
- **Land Status**: New listings are created with `PENDING_APPROVAL` status. Only `ACTIVE` status lands appear in public browse/search/filter endpoints.
- **Vendor Details**: Viewing vendor details (`GET /vendors/{id}`) requires an active paid subscription (not FREE).
- **Soft Delete**: Land deletion sets `deleted = true` rather than removing the record.
- **Password Reset**: Available only for EMAIL auth provider users (not Google/Apple). Tokens expire after 30 minutes and are single-use. The forgot-password endpoint always returns 200 to prevent email enumeration.
- **CORS**: Configured to allow `http://localhost:3000` and `http://localhost:8080` by default.

## 📄 License

MIT License - see [LICENSE](LICENSE) for details.
