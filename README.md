# Intelligent Alert Escalation & Resolution System

Smart alert engine that automatically escalates and closes alerts based on dynamic JSON rules, with JWT authentication and dashboard analytics.

## Tech Stack
- Java 17
- Spring Boot 4.0.3
- Spring Security (JWT)
- MongoDB
- Maven

## Features
- Centralized Alert API
- Dynamic Rule Engine (JSON-based)
- Auto-Close Scheduler (120s interval)
- JWT Authentication
- Dashboard Analytics
- Lifecycle Tracking: `OPEN → ESCALATED → AUTO_CLOSED → RESOLVED`

## Prerequisites
- Java 17+
- Maven 3.6+
- MongoDB running on `localhost:27017`

## How to Run

**Start MongoDB:**
```powershell
net start MongoDB
```

**Run Application:**
```powershell
mvn spring-boot:run
```

**Access:** `http://localhost:8080`

## Quick Start

**1. Register:**
```powershell
Invoke-RestMethod -Uri http://localhost:8080/api/auth/register -Method POST -Body '{"username":"admin","password":"admin123","role":"ADMIN"}' -ContentType "application/json"
```

**2. Login:**
```powershell
$token = (Invoke-RestMethod -Uri http://localhost:8080/api/auth/login -Method POST -Body '{"username":"admin","password":"admin123"}' -ContentType "application/json").token
$headers = @{Authorization="Bearer $token"}
```

**3. Create Alert:**
```powershell
Invoke-RestMethod -Uri http://localhost:8080/api/alerts -Method POST -Headers $headers -Body '{"sourceType":"OVERSPEEDING","severity":"WARNING","metadata":{"driverId":"D123","speed":95}}' -ContentType "application/json"
```

**4. View Dashboard:**
```powershell
Invoke-RestMethod -Uri http://localhost:8080/api/dashboard/summary -Headers $headers
```

## API Endpoints

**Authentication:**
- `POST /api/auth/register` - Register user
- `POST /api/auth/login` - Login and get JWT token

**Alerts:**
- `POST /api/alerts` - Create alert
- `GET /api/alerts` - List all alerts
- `GET /api/alerts/{id}` - Get alert with history
- `PATCH /api/alerts/{id}/resolve` - Resolve alert

**Dashboard:**
- `GET /api/dashboard/summary` - Overall stats
- `GET /api/dashboard/top-offenders?limit=10` - Top drivers
- `GET /api/dashboard/events?limit=20` - Recent events
- `GET /api/dashboard/trend?days=7` - Weekly trend

## Rule Engine

Rules in `src/main/resources/rules.json`:
```json
{
  "overspeed": {"escalate_if_count": 3, "window_mins": 60},
  "feedback_negative": {"escalate_if_count": 2, "window_mins": 1440},
  "compliance": {"auto_close_if": "document_valid"}
}
```

## Configuration

`src/main/resources/application.yaml`:
```yaml
spring.data.mongodb.uri: mongodb://localhost:27017/alertdb
alert.expiry-mins: 1440
scheduler.fixed-delay: 120000
```

## Project Structure
```
src/main/java/com/example/demo/
├── alerts/
│   ├── api/          # Controllers
│   ├── model/        # Entities
│   ├── repo/         # Repositories
│   ├── rules/        # Rule Engine
│   └── service/      # Business Logic
└── auth/             # JWT Security
```

## Additional Documentation
- [COMPLEXITY_ANALYSIS.md](COMPLEXITY_ANALYSIS.md) - Time/Space complexity analysis