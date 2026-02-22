# Intelligent Alert Escalation & Resolution System

Simple full-stack app for alert management.
- Backend: Spring Boot + PostgreSQL
- Frontend: React

## Quick Start

### 1) Prerequisites
- Java 17+
- Maven
- Node.js + npm
- PostgreSQL running on `localhost:5432`

Create database:
- DB name: `alertdb`
- User: `postgres`
- Password: `postgres`

### 2) Run Backend
```bash
cd backend
mvn spring-boot:run
```
Backend URL: http://localhost:8080

### 3) Run Frontend
Open a new terminal:
```bash
cd frontend
npm install
npm start
```
Frontend URL: http://localhost:3000

## Important URLs
- App: http://localhost:3000
- API docs (Swagger): http://localhost:8080/swagger-ui.html

## Main Features
- User login/register (JWT auth)
- Create and manage alerts
- Resolve alerts
- Dashboard summary and trends

## Project Structure
```text
demo/
  backend/   # Spring Boot API
  frontend/  # React UI
```

## Useful Commands

### Backend
```bash
cd backend
mvn test
mvn clean package
```

### Frontend
```bash
cd frontend
npm test
npm run build
```

## Configuration
- Backend config: `backend/src/main/resources/application.yaml`
- Frontend API config: `frontend/.env` with:

```env
REACT_APP_API_URL=http://localhost:8080
```

## Common Issues
- Backend not starting: check Java version and PostgreSQL connection.
- Frontend API error: verify backend is running on port `8080`.
- Frontend not starting: run `npm install` again in `frontend`.

## More Details
- Backend docs: `backend/README.md`
- Frontend docs: `frontend/README.md`