# Intelligent Alert Escalation & Resolution System

Smart alert engine that automatically escalates and closes alerts based on dynamic JSON rules, with JWT authentication and dashboard analytics.

## Tech Stack
- Java 17
- Spring Boot 4.0.3
- Spring Security (JWT)
- PostgreSQL 18+
- Spring Data JPA
- Maven

## Features
- Centralized Alert API
- Dynamic Rule Engine (JSON-based)
- Auto-Close Scheduler (120s interval)
- JWT Authentication
- Dashboard Analytics
- User Management (List all users)
- Lifecycle Tracking: `OPEN → ESCALATED → AUTO_CLOSED → RESOLVED`
- Full audit trail with AlertLifecycleEvent tracking

## Prerequisites
- Java 17+
- Maven 3.6+
- PostgreSQL 12+ running on `localhost:5432`
- pgAdmin (optional, for database management)

## Database Setup

**Create PostgreSQL Database:**
```sql
CREATE DATABASE alertdb;
```

Or use pgAdmin:
1. Open pgAdmin (http://localhost:5050)
2. Right-click Databases → Create → Database
3. Name: `alertdb`
4. Click Save

**Configuration in application.yaml:**
```yaml
spring:
  datasource:
    url: jdbc:postgresql://localhost:5432/alertdb
    username: postgres
    password: 1234
  jpa:
    hibernate:
      ddl-auto: update
```

## How to Run

**Start PostgreSQL:**
```powershell
# Windows Services
net start postgresql-18

# Or check pgAdmin is running
# http://localhost:5050
```

**Run Application:**
```powershell
mvn spring-boot:run
```

**Access:** `http://localhost:8080`

## Quick Start

**1. Register User:**
```powershell
$body = '{"username":"admin","password":"admin123"}'
Invoke-WebRequest -Uri http://localhost:8080/api/auth/register -Method POST -Body $body -ContentType "application/json" -UseBasicParsing
```

**2. Get JWT Token:**
```powershell
$loginBody = '{"username":"admin","password":"admin123"}'
$response = Invoke-WebRequest -Uri http://localhost:8080/api/auth/login -Method POST -Body $loginBody -ContentType "application/json" -UseBasicParsing
$token = ($response.Content | ConvertFrom-Json).token
$headers = @{"Authorization"="Bearer $token"}
```

**3. View All Users:**
```powershell
Invoke-WebRequest -Uri http://localhost:8080/api/auth/users -Method Get -Headers $headers -UseBasicParsing | Select-Object -ExpandProperty Content | ConvertFrom-Json
```

**4. Create Alert:**
```powershell
$alertBody = '{"sourceType":"OVERSPEEDING","severity":"CRITICAL","metadata":{"driverId":"D123","speed":"145"}}'
Invoke-WebRequest -Uri http://localhost:8080/api/alerts -Method POST -Headers $headers -Body $alertBody -ContentType "application/json" -UseBasicParsing
```

**5. Get All Alerts:**
```powershell
Invoke-WebRequest -Uri http://localhost:8080/api/alerts -Method Get -Headers $headers -UseBasicParsing | Select-Object -ExpandProperty Content | ConvertFrom-Json
```

## API Endpoints

**Authentication:**
- `POST /api/auth/register` - Register new user
- `POST /api/auth/login` - Login and get JWT token
- `GET /api/auth/users` - Get all registered users *(new)*

**Alerts:**
- `POST /api/alerts` - Create alert
- `GET /api/alerts` - List all alerts (requires JWT)
- `GET /api/alerts/{alertId}` - Get alert with lifecycle history
- `PATCH /api/alerts/{alertId}/resolve` - Manually resolve alert
- `POST /api/alerts/compliance-renewed` - Mark compliance alerts as renewed

**Dashboard:**
- `GET /api/dashboard/summary` - Overall statistics
- `GET /api/dashboard/top-offenders?limit=10` - Top drivers with open alerts
- `GET /api/dashboard/events?limit=20` - Recent lifecycle events
- `GET /api/dashboard/trend?days=7` - Alert trends over N days

## Database Tables

### users
Stores user accounts with JWT roles
```
- id (UUID, Primary Key)
- username (VARCHAR, Unique)
- password (VARCHAR, encrypted)
- role (VARCHAR)
- created_at (TIMESTAMP)
```

### alerts
Stores alert records
```
- alertid (UUID, Primary Key)
- sourcetype (ENUM: OVERSPEEDING, COMPLIANCE, NEGATIVE_FEEDBACK)
- severity (ENUM: INFO, WARNING, CRITICAL)
- status (ENUM: OPEN, ESCALATED, AUTO_CLOSED, RESOLVED)
- timestamp (TIMESTAMP)
- updated_at (TIMESTAMP)
- escalation_triggered (BOOLEAN)
- auto_close_reason (VARCHAR)
- metadata (Map of key-value pairs)
```

### alert_lifecycle_events
Audit trail of alert status changes
```
- eventid (UUID, Primary Key)
- alertid (UUID, Foreign Key)
- event_type (VARCHAR)
- from_status (ENUM)
- to_status (ENUM)
- timestamp (TIMESTAMP)
- reason (VARCHAR)
```

### alert_metadata
Stores metadata key-value pairs for alerts
```
- alert_alertid (UUID)
- metadata_key (VARCHAR)
- metadata_value (VARCHAR)
```

## Rule Engine

Rules in `src/main/resources/rules.json`:
```json
{
  "OVERSPEEDING": {
    "escalate_if_count": 3,
    "window_mins": 60,
    "auto_close_if": null
  },
  "COMPLIANCE": {
    "escalate_if_count": 2,
    "window_mins": 1440,
    "auto_close_if": "document_valid"
  },
  "NEGATIVE_FEEDBACK": {
    "escalate_if_count": 1,
    "window_mins": 60,
    "auto_close_if": null
  }
}
```

## Configuration

**application.yaml:**
```yaml
spring:
  application:
    name: demo
  datasource:
    url: jdbc:postgresql://localhost:5432/alertdb
    username: postgres
    password: 1234
    driver-class-name: org.postgresql.Driver
  jpa:
    hibernate:
      ddl-auto: update
    properties:
      hibernate:
        dialect: org.hibernate.dialect.PostgreSQLDialect
  scheduler:
    fixed-delay-ms: 120000

jwt:
  secret: your-secret-key-here
  expiry: 86400000  # 24 hours
```

## Project Structure
```
src/main/java/com/example/demo/
├── DemoApplication.java          # Spring Boot main
├── alerts/
│   ├── api/
│   │   ├── AlertController.java
│   │   ├── DashboardController.java
│   │   ├── CreateAlertRequest.java
│   │   └── ComplianceRenewalRequest.java
│   ├── model/
│   │   ├── Alert.java
│   │   ├── AlertStatus.java
│   │   ├── AlertLifecycleEvent.java
│   │   ├── Severity.java
│   │   └── SourceType.java
│   ├── repo/
│   │   ├── AlertRepository.java
│   │   └── AlertLifecycleEventRepository.java
│   ├── rules/
│   │   ├── RuleProvider.java
│   │   ├── RuleConfig.java
│   │   └── RuleDefinition.java
│   └── service/
│       ├── AlertService.java
│       ├── AlertLifecycleService.java
│       ├── AlertAutoCloseScheduler.java
│       ├── DashboardService.java
│       └── GlobalExceptionHandler.java
└── auth/
    ├── AuthController.java
    ├── User.java
    ├── UserRepository.java
    ├── JwtUtil.java
    ├── JwtAuthenticationFilter.java
    ├── SecurityConfig.java
    ├── RegisterRequest.java
    ├── LoginRequest.java
    └── AuthResponse.java

src/main/resources/
├── application.yaml
├── rules.json
├── static/
└── templates/
```

## Performance & Complexity

See [COMPLEXITY_ANALYSIS.md](COMPLEXITY_ANALYSIS.md) for detailed time/space complexity analysis of all services.

**Key Optimizations:**
- JPA with PostgreSQL indexed queries
- Rule-based escalation: O(n) per alert
- Auto-close scheduler: Fixed 120s interval
- Lifecycle tracking: O(1) append-only
- User queries: O(1) with indexed lookup

## Migration Notes

**From MongoDB to PostgreSQL:**
- All entities updated from `@Document` to `@Entity`
- Repositories changed from `MongoRepository` to `JpaRepository`
- IDs auto-generated using `@GeneratedValue(GenerationType.UUID)`
- Metadata stored in `@ElementCollection` table
- Full schema auto-created by Hibernate (ddl-auto: update)

## Testing the System

**Integration Test Example:**
```powershell
# 1. Register 3 users
for ($i=1; $i -le 3; $i++) {
    $body = "{`"username`":`"testuser$i`",`"password`":`"Test123`"}"
    Invoke-WebRequest -Uri http://localhost:8080/api/auth/register -Method POST -Body $body -ContentType "application/json" -UseBasicParsing
}

# 2. Create alerts and trigger escalation
for ($i=1; $i -le 3; $i++) {
    $alertBody = "{`"sourceType`":`"OVERSPEEDING`",`"severity`":`"WARNING`",`"metadata`":{`"driverId`":`"D001`",`"speed`":`"$($80+$i*10)`"}}"
    Invoke-WebRequest -Uri http://localhost:8080/api/alerts -Method Post -Headers $headers -Body $alertBody -ContentType "application/json" -UseBasicParsing
}

# 3. View dashboard
Invoke-WebRequest -Uri http://localhost:8080/api/dashboard/summary -Headers $headers -UseBasicParsing
```

## Security

- JWT Authentication with 24-hour expiry
- Bcrypt password encryption
- Role-based access control (USER, ADMIN)
- HTTP-only token handling
- Spring Security with custom filter

## Additional Documentation
- [COMPLEXITY_ANALYSIS.md](COMPLEXITY_ANALYSIS.md) - Algorithm complexity analysis
- See PostgreSQL queries in pgAdmin: Servers → Aditya → alertdb → Schemas → public → Tables