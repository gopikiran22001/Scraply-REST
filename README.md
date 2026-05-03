# Scraply REST API

A comprehensive Spring Boot REST API backend for the Scraply intelligent waste management platform. Handles user authentication, pickup requests, illegal dumping reports, collector management, and real-time task assignment orchestration.

## 📋 Overview

The Scraply REST API is a production-ready Spring Boot 3.2 application that serves as the central backend for the waste management platform. It manages:

- ✅ **Multi-Role Authentication**: Citizens, Collectors, Admins with JWT
- 📍 **Pickup Management**: Creation, tracking, and lifecycle management
- 🚨 **Illegal Dumping Reports**: Reporting and administrative tracking
- 👥 **User Management**: Profiles, roles, and permissions
- 🔄 **Task Assignment**: Integration with AI Agent for intelligent assignments
- 📊 **Analytics & Dashboards**: Real-time statistics and insights
- 🔐 **OAuth2**: Google authentication integration
- 📤 **Cloud Storage**: Image uploads via Cloudinary

## 🏗️ Architecture

```
┌──────────────────────────────────────────────────────┐
│         Scraply REST API (Spring Boot 3.2)           │
├──────────────────────────────────────────────────────┤
│                                                      │
│  ┌─────────────────────────────────────────────┐   │
│  │         REST Controllers                    │   │
│  │  - Auth Controller                          │   │
│  │  - Pickup Controller                        │   │
│  │  - Dumping Controller                       │   │
│  │  - User Controller                          │   │
│  │  - Admin Controller                         │   │
│  │  - Analytics Controller                     │   │
│  └────────────────────┬────────────────────────┘   │
│                       │                             │
│  ┌────────────────────▼────────────────────────┐   │
│  │  Service Layer                              │   │
│  │  - AuthService / JwtTokenProvider           │   │
│  │  - PickupService                            │   │
│  │  - DumpingService                           │   │
│  │  - AssignmentService                        │   │
│  │  - UserService                              │   │
│  │  - AnalyticsService                         │   │
│  └────────────────────┬────────────────────────┘   │
│                       │                             │
│  ┌────────────────────▼────────────────────────┐   │
│  │  Repository Layer (JPA)                     │   │
│  │  - PickupRequestRepository                  │   │
│  │  - IllegalDumpingRepository                 │   │
│  │  - AssignmentRepository                     │   │
│  │  - UserRepository                           │   │
│  │  - CollectionRepository                     │   │
│  └────────────────────┬────────────────────────┘   │
│                       │                             │
│       ┌───────────────┼───────────────┐             │
│       ▼               ▼               ▼             │
│  ┌─────────────┐ ┌─────────┐ ┌────────────────┐   │
│  │ PostgreSQL  │ │  Redis  │ │  Cloudinary    │   │
│  │  Database   │ │ (Cache) │ │ (Image Storage)│   │
│  └─────────────┘ └─────────┘ └────────────────┘   │
│                                                      │
│  ┌──────────────────────────────────────────────┐  │
│  │  Security & Configuration                   │  │
│  │  - JWT Authentication                       │  │
│  │  - CORS Configuration                       │  │
│  │  - Role-Based Access Control                │  │
│  │  - OAuth2 Configuration                     │  │
│  └──────────────────────────────────────────────┘  │
│                                                      │
└──────────────────────────────────────────────────────┘
```

### Project Structure

```
Rest-API/
├── src/main/java/com/scraply/rest/
│   ├── RestApplication.java                   # Spring Boot entry point
│   │
│   ├── controllers/                          # REST endpoints
│   │   ├── AuthController.java               # Login, register, OAuth
│   │   ├── PickupController.java             # Pickup CRUD operations
│   │   ├── DumpingController.java            # Dump report endpoints
│   │   ├── UserController.java               # User management
│   │   ├── AdminController.java              # Admin operations
│   │   └── AnalyticsController.java          # Dashboard statistics
│   │
│   ├── services/                             # Business logic
│   │   ├── AuthService.java                  # Authentication logic
│   │   ├── PickupService.java                # Pickup operations
│   │   ├── DumpingService.java               # Dump report operations
│   │   ├── AssignmentService.java            # Assignment logic
│   │   ├── UserService.java                  # User management
│   │   └── AnalyticsService.java             # Analytics calculations
│   │
│   ├── repositories/                         # Data access (JPA)
│   │   ├── PickupRequestRepository.java
│   │   ├── IllegalDumpingRepository.java
│   │   ├── AssignmentRepository.java
│   │   ├── UserRepository.java
│   │   ├── CollectionRepository.java
│   │   └── [Other repositories]
│   │
│   ├── models/                               # JPA entities
│   │   ├── User.java
│   │   ├── PickupRequest.java
│   │   ├── IllegalDumping.java
│   │   ├── Assignment.java
│   │   ├── Collection.java
│   │   └── [Other entities]
│   │
│   ├── dto/                                  # Data Transfer Objects
│   │   ├── request/
│   │   │   ├── LoginRequest.java
│   │   │   ├── CreatePickupRequest.java
│   │   │   └── [Other request DTOs]
│   │   ├── response/
│   │   │   ├── AuthResponse.java
│   │   │   ├── PickupResponse.java
│   │   │   └── [Other response DTOs]
│   │   └── projection/
│   │
│   ├── security/                             # Security configuration
│   │   ├── JwtTokenProvider.java             # JWT token handling
│   │   ├── SecurityConfig.java               # Spring Security setup
│   │   ├── JwtAuthFilter.java                # JWT authentication filter
│   │   └── CustomUserDetailsService.java
│   │
│   ├── config/                               # Configuration classes
│   │   ├── CloudinaryConfig.java             # Image storage setup
│   │   ├── CorsConfig.java                   # CORS configuration
│   │   ├── JpaAuditConfig.java               # Audit trail setup
│   │   └── [Other configs]
│   │
│   ├── exception/                            # Exception handling
│   │   ├── GlobalExceptionHandler.java       # Centralized exception handling
│   │   ├── ResourceNotFoundException.java
│   │   ├── ValidationException.java
│   │   └── [Other custom exceptions]
│   │
│   ├── cloudinary/                           # Image service
│   │   ├── CloudinaryService.java            # Image upload/management
│   │   └── CloudinaryUtil.java
│   │
│   └── util/                                 # Utility classes
│       ├── GeoUtil.java                      # Geographic calculations
│       ├── DateUtil.java
│       └── [Other utilities]
│
├── src/main/resources/
│   ├── application.properties                 # Main configuration
│   ├── secrets.properties                    # Secrets (env-based)
│   ├── logback-spring.xml                    # Logging configuration
│   ├── db/
│   │   └── migration/                        # Database migrations
│   └── static/                               # Static files
│
├── src/test/java/                           # Test files
│
├── pom.xml                                   # Maven configuration
├── Dockerfile                                # Container image
├── HELP.md                                   # Spring Boot help
└── README.md                                 # This file
```

## 🚀 Installation & Setup

### Prerequisites

- **Java 21 JDK**
- **Maven 3.9+** (mvnw included)
- **PostgreSQL 13+**
- **Redis 6+**
- **Cloudinary Account** (for image storage)
- **Google OAuth App** (optional, for social login)
- **Groq API Key** (for AI Agent integration)

### Step 1: Clone Repository

```bash
git clone <repository-url>
cd Scraply/Rest-API
```

### Step 2: Configure Environment

Create a `.env` file in the Rest-API directory (or set environment variables):

```env
# Server Configuration
SERVER_PORT=8080
SPRING_PROFILES_ACTIVE=dev

# Database Configuration
DB_HOST=localhost
DB_PORT=5432
DB_NAME=scraply
DB_USER=scraply
DB_PASSWORD=your_password
DATASOURCE_URL=jdbc:postgresql://localhost:5432/scraply

# JWT Configuration
JWT_SECRET=your_very_long_secret_key_here_min_32_chars
JWT_EXPIRATION_MS=86400000

# Redis Configuration
REDIS_HOST=localhost
REDIS_PORT=6379
REDIS_PASSWORD=

# Cloudinary Configuration
CLOUDINARY_CLOUD_NAME=your_cloud_name
CLOUDINARY_API_KEY=your_api_key
CLOUDINARY_API_SECRET=your_api_secret

# Google OAuth Configuration (optional)
GOOGLE_CLIENT_ID=your_google_client_id
GOOGLE_CLIENT_SECRET=your_google_client_secret

# CORS Configuration
WEBAPP_ORIGIN=http://localhost:5173

# AI Agent Configuration
AGENT_API_URL=http://localhost:8001
AGENT_API_KEY=agent_password
```

### Step 3: Setup Database

**Create PostgreSQL Database:**

```sql
-- Create database
CREATE DATABASE scraply;

-- Create application user
CREATE USER scraply WITH PASSWORD 'your_password';

-- Grant permissions
GRANT ALL PRIVILEGES ON DATABASE scraply TO scraply;

-- Create read-only user for AI Agent
CREATE USER scraply_readonly WITH PASSWORD 'readonly_password';
GRANT CONNECT ON DATABASE scraply TO scraply_readonly;
GRANT USAGE ON SCHEMA public TO scraply_readonly;
GRANT SELECT ON ALL TABLES IN SCHEMA public TO scraply_readonly;
ALTER DEFAULT PRIVILEGES IN SCHEMA public GRANT SELECT ON TABLES TO scraply_readonly;
```

### Step 4: Build Application

```bash
# Build with Maven
./mvnw clean package -DskipTests

# Or if mvnw not available
mvn clean package -DskipTests
```

### Step 5: Run Application

```bash
# Run Spring Boot application
./mvnw spring-boot:run

# Or run JAR directly
java -jar target/rest-0.0.1-SNAPSHOT.jar
```

The API will be available at: `http://localhost:8080`

### Step 6: Access Swagger UI

Open your browser to: `http://localhost:8080/swagger-ui.html`

## 📚 API Endpoints

### Authentication Endpoints

```
POST   /api/auth/register           - Register new user
POST   /api/auth/login              - Login with credentials
POST   /api/auth/refresh            - Refresh JWT token
POST   /api/auth/logout             - Logout user
POST   /api/auth/google             - Google OAuth login
GET    /api/auth/me                 - Get current user profile
```

### Pickup Request Endpoints

```
GET    /api/pickups                 - List all pickups (paginated)
POST   /api/pickups                 - Create new pickup request
GET    /api/pickups/{id}            - Get pickup details
PUT    /api/pickups/{id}            - Update pickup request
DELETE /api/pickups/{id}            - Cancel pickup request
POST   /api/pickups/{id}/status     - Update pickup status
GET    /api/pickups/user/{userId}   - Get user's pickups
GET    /api/pickups/status/{status} - Get pickups by status
```

### Illegal Dumping Endpoints

```
GET    /api/dumps                   - List all dump reports
POST   /api/dumps                   - Report illegal dumping
GET    /api/dumps/{id}              - Get dump report details
PUT    /api/dumps/{id}              - Update dump report
DELETE /api/dumps/{id}              - Delete dump report
POST   /api/dumps/{id}/status       - Update dump status
GET    /api/dumps/priority/{level}  - Get dumps by priority
GET    /api/dumps/nearby            - Get nearby dumps
```

### Assignment Endpoints

```
GET    /api/assignments             - List all assignments
POST   /api/assignments             - Create assignment
GET    /api/assignments/{id}        - Get assignment details
PUT    /api/assignments/{id}        - Update assignment
DELETE /api/assignments/{id}        - Cancel assignment
GET    /api/assignments/collector/{id} - Get collector's assignments
POST   /api/assignments/{id}/complete  - Mark assignment as complete
```

### User Management Endpoints

```
GET    /api/users                   - List all users
GET    /api/users/{id}              - Get user profile
PUT    /api/users/{id}              - Update user profile
DELETE /api/users/{id}              - Delete user account
GET    /api/users/{id}/profile      - Get extended profile
PUT    /api/users/{id}/password     - Change password
GET    /api/collectors              - List all collectors
GET    /api/collectors/available    - Get available collectors
```

### Analytics Endpoints

```
GET    /api/analytics/dashboard     - Main dashboard statistics
GET    /api/analytics/pickups       - Pickup analytics
GET    /api/analytics/dumps         - Dump report analytics
GET    /api/analytics/collectors    - Collector performance metrics
GET    /api/analytics/users         - User growth statistics
GET    /api/analytics/maps/heatmap  - Geographic heatmap data
```

### Admin Endpoints

```
GET    /api/admin/users             - Manage users
POST   /api/admin/users/{id}/role   - Update user role
POST   /api/admin/settings          - Update system settings
GET    /api/admin/logs              - System logs
POST   /api/admin/queue/status      - Check queue status
POST   /api/admin/queue/retry       - Retry failed items
```

## 🔐 Security Features

### JWT Authentication

- **Token Format**: Bearer Token in Authorization header
- **Expiration**: Configurable (default: 24 hours)
- **Refresh**: Automatic refresh token mechanism
- **Storage**: HttpOnly cookies for web app

### Role-Based Access Control (RBAC)

```
CITIZEN
├── Create pickup requests
├── View own requests
├── Report illegal dumping
└── View own profile

COLLECTOR
├── View assigned pickups
├── Update pickup status
├── Complete assignments
└── View performance metrics

ADMIN
├── Manage all users
├── View all requests
├── Update system settings
└── Access analytics
└── Manage assignments
```

### OAuth2 Integration

- **Google Authentication**: Social login via Google
- **Automatic User Creation**: Users created on first OAuth login
- **Token Mapping**: OAuth tokens mapped to JWT

### CORS Configuration

```properties
# Allowed origins configured in security config
webapp.origin=http://localhost:5173
```

### Password Security

- **Encryption**: BCrypt with salt
- **Validation**: Minimum 8 characters, complexity requirements
- **Reset Flow**: Email-based password reset

## 🗄️ Database Schema

### Core Tables

**users**
```
- id (UUID)
- name (String)
- email (String, unique)
- password (String, encrypted)
- role (CITIZEN, COLLECTOR, ADMIN)
- status (ACTIVE, INACTIVE, BANNED)
- phone (String)
- address (String)
- latitude, longitude (Double)
- vehicle_type (String, for collectors)
- pick_up_route (String, for collectors)
- rating (Double)
- created_at, updated_at (Timestamp)
```

**pickup_requests**
```
- id (UUID)
- user_id (FK)
- picker_id (FK, nullable)
- assigned_by (FK, nullable)
- description (Text)
- category (Enum)
- image_url (String)
- latitude, longitude (Double)
- address (String)
- status (PENDING, ASSIGNED, IN_PROGRESS, COMPLETED, CANCELLED)
- priority_level (Integer)
- requested_at, assigned_at, completed_at (Timestamp)
```

**illegal_dumping_requests**
```
- id (UUID)
- description (Text)
- category (Enum)
- latitude, longitude (Double)
- address, landmark (String)
- image_url (String)
- reported_by_id (FK)
- assigned_picker_id (FK, nullable)
- assigned_by (FK, nullable)
- status (PENDING, ASSIGNED, IN_PROGRESS, RESOLVED, ARCHIVED)
- priority_level (Integer)
- reported_at, assigned_at, resolved_at (Timestamp)
```

**assignments**
```
- id (UUID)
- pickup_request_id (FK, nullable)
- illegal_dumping_id (FK, nullable)
- collector_id (FK)
- assigned_by (FK)
- status (PENDING, ACCEPTED, IN_PROGRESS, COMPLETED, FAILED)
- assigned_at, completed_at (Timestamp)
```

**collections**
```
- id (UUID)
- assignment_id (FK)
- pickup_request_id (FK)
- collector_id (FK)
- completion_notes (Text)
- image_url (String)
- completed_at (Timestamp)
```

## 🔄 Data Flow

### Pickup Request Flow

```
1. User submits via Web App
   ↓
2. PickupController validates input
   ↓
3. PickupService creates request in DB
   ↓
4. Request pushed to Redis pickup_queue
   ↓
5. AI Agent evaluates request
   ↓
6. Valid → moved to pickup_assign_queue
   ↓
7. Assignment Agent selects collector
   ↓
8. POST /api/pickups/{id}/status to update
   ↓
9. Web App notified via polling/WebSocket
   ↓
10. Collector receives assignment
```

### Illegal Dump Report Flow

```
1. Citizen reports dump with image
   ↓
2. Image uploaded to Cloudinary
   ↓
3. DumpingService creates report
   ↓
4. Report pushed to Redis dump_queue
   ↓
5. AI Agent analyzes image & data
   ↓
6. Valid → moved to dump_assign_queue
   ↓
7. Assigned to cleanup crew or admin
   ↓
8. Status tracked in dashboard
```

## 🧪 Testing

### Run Tests

```bash
# Run all tests
./mvnw test

# Run specific test class
./mvnw test -Dtest=PickupServiceTest

# Run with coverage
./mvnw jacoco:report
```

### Testing Endpoints with Curl

```bash
# Register user
curl -X POST http://localhost:8080/api/auth/register \
  -H "Content-Type: application/json" \
  -d '{"email":"user@example.com","password":"password123","name":"John Doe"}'

# Login
curl -X POST http://localhost:8080/api/auth/login \
  -H "Content-Type: application/json" \
  -d '{"email":"user@example.com","password":"password123"}'

# Create pickup request (with JWT token)
curl -X POST http://localhost:8080/api/pickups \
  -H "Authorization: Bearer <token>" \
  -H "Content-Type: application/json" \
  -d '{"description":"Old furniture","category":"bulk","latitude":40.7128,"longitude":-74.0060}'

# Get pickups
curl http://localhost:8080/api/pickups \
  -H "Authorization: Bearer <token>"
```

## 📦 Dependencies

Key technologies and versions:

- **Spring Boot**: 3.2.3
- **Spring Data JPA**: For database access
- **Spring Security**: For authentication/authorization
- **PostgreSQL Driver**: Database connection
- **JWT (jjwt)**: Token-based authentication
- **Cloudinary**: Cloud image storage
- **Google API Client**: OAuth2 integration
- **SpringDoc OpenAPI**: Swagger/API documentation
- **Redis**: Caching and queue management

## 🐳 Docker Deployment

### Building Docker Image

```bash
# Build with Maven
mvnw clean package -DskipTests

# Build Docker image
docker build -t scraply-restapi:latest .

# Push to Docker Hub
docker tag scraply-restapi:latest <docker-user>/scraply-restapi:latest
docker push <docker-user>/scraply-restapi:latest
```

### Running Container

```bash
docker run -d \
  --name scraply-api \
  -p 8080:8080 \
  -e DATASOURCE_URL=jdbc:postgresql://postgres:5432/scraply \
  -e JWT_SECRET=your_secret \
  -e CLOUDINARY_CLOUD_NAME=your_name \
  scraply-restapi:latest
```

### Docker Compose

```yaml
services:
  rest-api:
    build: ./Rest-API
    ports:
      - "8080:8080"
    environment:
      DATASOURCE_URL: jdbc:postgresql://postgres:5432/scraply
      JWT_SECRET: ${JWT_SECRET}
      CLOUDINARY_CLOUD_NAME: ${CLOUDINARY_CLOUD_NAME}
    depends_on:
      - postgres
      - redis
    restart: unless-stopped
```

## 📊 Monitoring & Health Checks

### Health Check Endpoint

```
GET http://localhost:8080/actuator/health
```

### Application Monitoring

- **Logging**: Configurable via logback-spring.xml
- **Metrics**: Spring Boot Actuator metrics
- **Performance**: Database query optimization
- **Caching**: Redis caching layer

## ⚙️ Configuration Files

### application.properties

```properties
server.port=8080
spring.application.name=Scraply
spring.jpa.hibernate.ddl-auto=update
spring.datasource.driver-class-name=org.postgresql.Driver
logging.level.com.scraply=DEBUG
```

### secrets.properties

```properties
spring.datasource.url=jdbc:postgresql://localhost:5432/scraply
spring.datasource.username=scraply
spring.datasource.password=password
jwt.secret=your_secret_key
jwt.expiration=86400000
cloudinary.cloud_name=name
cloudinary.api_key=key
cloudinary.api_secret=secret
webapp.origin=http://localhost:5173
```

## 🔧 Troubleshooting

### Common Issues

**Issue**: Database connection failed
```
Solution: Verify PostgreSQL is running and credentials in .env are correct
```

**Issue**: CORS error in Web App
```
Solution: Check webapp.origin in secrets.properties (no trailing slash)
```

**Issue**: JWT token expired
```
Solution: Use refresh endpoint or login again
```

**Issue**: Cloudinary image upload fails
```
Solution: Verify Cloudinary credentials and API limits
```

## 🚀 Performance Optimization

### Database Optimization

- Use indexes on frequently queried columns
- Enable JPA query caching
- Use pagination for large result sets
- Optimize lazy vs eager loading

### Redis Caching

- Cache user profiles
- Cache frequently accessed data
- Use TTL for automatic cleanup

### API Response Optimization

- Use DTOs to minimize data transfer
- Implement pagination
- Use proper HTTP status codes
- Enable gzip compression

## 📝 API Documentation

### Swagger/OpenAPI

Access interactive documentation at:
```
http://localhost:8080/swagger-ui.html
```

## 🔐 Important Security Notes

From [rest-api-config.md](/memories/repo/rest-api-config.md):

- REST API imports secrets.properties via spring.config.import
- CORS allowed origin from webapp.origin (no trailing slash)
- Keep webapp.origin without trailing slash for exact Origin header matching
- JPA constructor projections must match DTO constructor types exactly
- Nullable entity fields should map to wrapper types in DTOs

## 🤝 Contributing

1. Create feature branch
2. Follow Spring Boot best practices
3. Add tests for new features
4. Submit pull request for review

## 📄 License

[Specify your license here]

---

**Version**: 0.0.1-SNAPSHOT  
**Last Updated**: May 2026  
**Spring Boot**: 3.2.3  
**Java**: 21
