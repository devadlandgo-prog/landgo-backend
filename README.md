# LandGo Backend

A Spring Boot backend API for the LandGo land listing platform with vendor management, user subscriptions, and OAuth2 authentication.

## 🏗️ Architecture Overview

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
- **Vendor System**: Users can register as vendors to list lands
- **Land Listings**: Full CRUD operations for land properties
- **Subscription Plans**: FREE, BASIC, PREMIUM, ENTERPRISE tiers
- **JWT Authentication**: Secure stateless authentication
- **OAuth2 Integration**: Google and Apple Sign-In
- **API Documentation**: OpenAPI/Swagger UI

## 🛠️ Tech Stack

- **Framework**: Spring Boot 3.2.2
- **Language**: Java 21
- **Database**: PostgreSQL 16
- **ORM**: Spring Data JPA + Hibernate
- **Migration**: Flyway
- **Security**: Spring Security + JWT + OAuth2
- **Documentation**: SpringDoc OpenAPI
- **Mapping**: MapStruct
- **Build**: Maven

## 📁 Project Structure

```
src/main/java/com/landgo/
├── config/              # Configuration classes
├── controller/          # REST API controllers
├── dto/                 # Data Transfer Objects
│   ├── request/         # Request DTOs
│   └── response/        # Response DTOs
├── entity/              # JPA entities
├── enums/               # Enumerations
├── exception/           # Custom exceptions & handlers
├── factory/             # Factory pattern implementations
├── mapper/              # MapStruct mappers
├── repository/          # Spring Data JPA repositories
├── security/            # Security configuration & JWT
├── service/             # Business logic services
└── strategy/            # Strategy pattern (OAuth providers)
```

## 🎯 Design Patterns Used

| Pattern | Usage |
|---------|-------|
| **Strategy** | OAuth2 authentication providers (Google, Apple) |
| **Factory** | OAuth2 strategy selection |
| **Repository** | Data access abstraction |
| **DTO** | Data transfer between layers |
| **Builder** | Complex object construction |

## 📋 SOLID Principles

- **S**ingle Responsibility: Each service handles one domain
- **O**pen/Closed: Strategy pattern for OAuth providers
- **L**iskov Substitution: Interface-based dependencies
- **I**nterface Segregation: Focused repository interfaces
- **D**ependency Inversion: Constructor injection throughout

## 🚦 API Endpoints

### Authentication
| Method | Endpoint | Description |
|--------|----------|-------------|
| POST | `/api/v1/auth/register` | Register new user |
| POST | `/api/v1/auth/login` | Login with email |
| POST | `/api/v1/auth/oauth2` | OAuth2 login (Google/Apple) |
| GET | `/api/v1/auth/me` | Get current user |

### Vendors
| Method | Endpoint | Description |
|--------|----------|-------------|
| POST | `/api/v1/vendor/register` | Register as vendor |
| GET | `/api/v1/vendors` | List all vendors |
| GET | `/api/v1/vendors/{id}` | Get vendor details* |
| GET | `/api/v1/vendor/profile` | Get own profile |
| PUT | `/api/v1/vendor/profile` | Update profile |

### Land Listings
| Method | Endpoint | Description |
|--------|----------|-------------|
| GET | `/api/v1/lands` | List all lands |
| GET | `/api/v1/lands/{id}` | Get land details |
| GET | `/api/v1/lands/search` | Search lands |
| GET | `/api/v1/lands/filter` | Filter lands |
| POST | `/api/v1/vendor/lands` | Create listing |
| PUT | `/api/v1/vendor/lands/{id}` | Update listing |
| DELETE | `/api/v1/vendor/lands/{id}` | Delete listing |

### Subscriptions
| Method | Endpoint | Description |
|--------|----------|-------------|
| POST | `/api/v1/subscriptions` | Subscribe to plan |
| GET | `/api/v1/subscriptions/current` | Get subscription |
| POST | `/api/v1/subscriptions/cancel` | Cancel subscription |

*Requires paid subscription

## 🔧 Local Development

### Prerequisites
- Java 21
- Docker & Docker Compose
- Maven 3.9+

### Quick Start

```bash
# Clone the repository
git clone https://github.com/your-org/landgo-backend.git
cd landgo-backend

# Start with Docker Compose
docker-compose up -d

# Or run locally with Maven
./mvnw spring-boot:run
```

### Environment Variables

| Variable | Description | Default |
|----------|-------------|---------|
| `DB_HOST` | Database host | localhost |
| `DB_PORT` | Database port | 5432 |
| `DB_NAME` | Database name | landgo |
| `DB_USERNAME` | Database user | postgres |
| `DB_PASSWORD` | Database password | postgres |
| `JWT_SECRET` | JWT signing key | - |
| `GOOGLE_CLIENT_ID` | Google OAuth client ID | - |
| `APPLE_CLIENT_ID` | Apple OAuth client ID | - |

### API Documentation

Once running, access Swagger UI at:
- http://localhost:8080/swagger-ui.html

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

- **Health Check**: `/actuator/health`
- **Metrics**: `/actuator/metrics`
- **Prometheus**: `/actuator/prometheus`

## 📄 License

MIT License - see [LICENSE](LICENSE) for details.
