# Intelligent Alert Escalation & Resolution System

A full-stack application for managing and monitoring alerts with automated escalation rules, user authentication, and real-time dashboard. Built with Spring Boot (backend) and React (frontend).

## 🚀 Quick Start

### Prerequisites
- **Backend**: Java 17+, Maven, PostgreSQL 12+
- **Frontend**: Node.js 14+, npm

### Quick Setup (5 minutes)

1. **Start PostgreSQL Database**
   ```bash
   # Ensure PostgreSQL is running on localhost:5432
   # Create database: alertdb
   # User: postgres, Password: postgres
   ```

2. **Start Backend**
   ```bash
   cd backend
   mvn clean install
   mvn spring-boot:run
   # Backend running on http://localhost:8080
   ```

3. **Start Frontend** (in another terminal)
   ```bash
   cd frontend
   npm install  # (if not already installed)
   npm start
   # Frontend running on http://localhost:3000
   # Browser will open automatically
   ```

4. **Access Application**
   - Frontend: [http://localhost:3000](http://localhost:3000)
   - API Docs: [http://localhost:8080/swagger-ui.html](http://localhost:8080/swagger-ui.html)
   - pgAdmin: [http://localhost:5050](http://localhost:5050)

## 📁 Project Structure

```
demo/
├── backend/                      # Spring Boot REST API
│   ├── src/main/java/
│   │   └── com/example/demo/
│   │       ├── alerts/          # Alert management system
│   │       ├── auth/            # Authentication & security
│   │       └── DemoApplication.java
│   ├── src/main/resources/
│   │   ├── application.yaml     # Database & server config
│   │   └── rules.json           # Alert escalation rules
│   ├── pom.xml                  # Maven dependencies
│   └── README.md                # Backend documentation
│
├── frontend/                     # React SPA
│   ├── src/
│   │   ├── pages/               # Page components
│   │   ├── components/          # Reusable components
│   │   ├── styles/              # CSS stylesheets
│   │   ├── api.js               # Axios HTTP client
│   │   └── App.js               # Main app component
│   ├── public/
│   │   └── index.html           # HTML entry point
│   ├── package.json             # npm dependencies
│   ├── .env                     # Environment variables
│   └── README.md                # Frontend documentation
│
└── README.md                     # This file
```

## 🎯 Key Features

### Backend (Spring Boot)
- ✅ **Alert Management**: Create, update, resolve alerts
- ✅ **Escalation Rules**: Automatic alert escalation based on conditions
- ✅ **User Authentication**: JWT-based authentication with BCrypt passwords
- ✅ **PostgreSQL Database**: Persistent storage with 4 tables
- ✅ **REST APIs**: 12 endpoints for full functionality
- ✅ **Exception Handling**: Comprehensive error handling with GlobalExceptionHandler
- ✅ **Auto-close Scheduler**: Automatic alert closure based on rules
- ✅ **Dashboard API**: Statistics and analytics endpoints

### Frontend (React)
- ✅ **User Authentication**: Login/Register pages
- ✅ **Dashboard**: Real-time statistics and alerts overview
- ✅ **Alert Management**: Create, view, filter, and resolve alerts
- ✅ **User Management**: View all users in the system
- ✅ **Responsive Design**: Mobile-friendly UI
- ✅ **Navigation**: Intuitive navigation with protected routes

## 🔄 API Endpoints

### Authentication
- `POST /api/auth/register` - Register new user
- `POST /api/auth/login` - User login
- `GET /api/auth/users` - Get all users

### Alerts
- `POST /api/alerts` - Create new alert
- `GET /api/alerts` - Get all alerts
- `GET /api/alerts/{id}` - Get alert details
- `PATCH /api/alerts/{id}/resolve` - Resolve alert
- `POST /api/alerts/compliance-renewed` - Mark compliance renewed

### Dashboard
- `GET /api/dashboard/summary` - Get dashboard statistics
- `GET /api/dashboard/top-offenders` - Get top offenders
- `GET /api/dashboard/events` - Get recent events
- `GET /api/dashboard/trend` - Get alert trends

## 🗄️ Database Schema

### Users Table
- `id` (UUID) - Primary key
- `username` (String) - Unique username
- `password` (String) - BCrypt hashed password
- `role` (String) - User role
- `created_at` (Timestamp) - Creation timestamp

### Alerts Table
- `id` (UUID) - Primary key
- `source_type` (String) - Alert source
- `severity` (Enum) - LOW, MEDIUM, HIGH, CRITICAL
- `status` (Enum) - OPEN, ESCALATED, AUTO_CLOSED, RESOLVED
- `metadata` (JSON) - Custom metadata
- `created_at` (Timestamp) - Creation timestamp
- `updated_at` (Timestamp) - Last update timestamp

### Alert Lifecycle Events Table
- `id` (UUID) - Primary key
- `alert_id` (UUID) - Reference to Alert
- `event_type` (String) - Event type
- `description` (String) - Event description
- `timestamp` (Timestamp) - Event timestamp

### Alert Metadata Table
- `id` (UUID) - Primary key
- `alert_id` (UUID) - Reference to Alert
- `key` (String) - Metadata key
- `value` (String) - Metadata value

## 🔐 Authentication Flow

1. **Register/Login**: User submits credentials
2. **JWT Token**: Backend returns JWT token (24-hour validity)
3. **Storage**: Token stored in localStorage on frontend
4. **Authorization**: Token included in all API requests via Axios interceptor
5. **Validation**: Backend validates token on each request
6. **Auto-logout**: Token expires after 24 hours (user must re-login)

## 🛠️ Tech Stack

### Backend
- **Language**: Java 17
- **Framework**: Spring Boot 4.0.3
- **Database**: PostgreSQL 18
- **Build Tool**: Maven
- **Authentication**: JWT + Spring Security
- **ORM**: Spring Data JPA + Hibernate
- **Password Encoding**: BCrypt

### Frontend
- **Framework**: React 19.2
- **Routing**: React Router DOM 7.13
- **HTTP Client**: Axios 1.13
- **Styling**: CSS 3 (Flexbox, Grid)
- **Build Tool**: create-react-app (react-scripts)

## 📊 Development Workflow

### Backend Development
```bash
cd backend
mvn clean compile        # Compile code
mvn test                 # Run tests
mvn spring-boot:run      # Start server
mvn clean package        # Build JAR
```

### Frontend Development
```bash
cd frontend
npm install              # Install dependencies
npm start                # Start dev server
npm run build            # Build production bundle
npm test                 # Run tests
```

## 🧪 Testing

### Backend Testing
```bash
cd backend
mvn test                 # Run all tests
mvn test -Dtest=AlertControllerTest  # Run specific test
```

### Frontend Testing
```bash
cd frontend
npm test                 # Run all tests
npm test -- --coverage   # Run with coverage report
```

## 📈 Performance Considerations

1. **Database Indexing**: Alerts table indexed on severity and status
2. **Connection Pooling**: HikariCP configured for optimal performance
3. **Caching**: Spring Cache for frequently accessed data
4. **Pagination**: API supports pagination for large datasets
5. **Frontend Optimization**: Code splitting and lazy loading

## 🔒 Security Features

1. **JWT Authentication**: Stateless authentication
2. **Password Encryption**: BCrypt for password hashing
3. **CORS Configuration**: Restricted to localhost:3000
4. **CSRF Protection**: Disabled for stateless JWT (standard practice)
5. **Input Validation**: Request DTO validation
6. **Exception Handling**: No sensitive information leaked in errors

## 🚀 Deployment

### Backend Deployment
```bash
# Build JAR
cd backend
mvn clean package

# Run JAR
java -jar target/demo-0.0.1-SNAPSHOT.jar

# Or deploy to cloud (AWS, Azure, Heroku)
```

### Frontend Deployment
```bash
# Build optimized bundle
cd frontend
npm run build

# Deploy build/ folder to:
# - Vercel: `vercel deploy`
# - Netlify: Drag & drop build folder
# - GitHub Pages: Push build to gh-pages branch
```

## 📝 Configuration

### Backend (backend/src/main/resources/application.yaml)
```yaml
spring:
  datasource:
    url: jdbc:postgresql://localhost:5432/alertdb
    username: postgres
    password: postgres
  jpa:
    hibernate:
      ddl-auto: validate
    show-sql: false
  security:
    jwt:
      secret: your-secret-key
      expiration: 86400000  # 24 hours
```

### Frontend (frontend/.env)
```
REACT_APP_API_URL=http://localhost:8080
```

## 🤝 Contributing

1. Create a feature branch: `git checkout -b feature/amazing-feature`
2. Commit changes: `git commit -m 'Add amazing feature'`
3. Push to branch: `git push origin feature/amazing-feature`
4. Open Pull Request

## 📚 Documentation

- [Backend README](backend/README.md) - Detailed backend documentation
- [Frontend README](frontend/README.md) - Detailed frontend documentation
- [Complexity Analysis](backend/COMPLEXITY_ANALYSIS.md) - Code architecture details

## 🐛 Troubleshooting

### Backend Won't Start
```bash
# Check if port 8080 is in use
# Check database connection: User=postgres, Password=postgres, DB=alertdb
# Check Java version: java -version (should be 17+)
```

### Frontend Won't Start
```bash
# Check if Node.js is installed: node --version
# Clear npm cache: npm cache clean --force
# Reinstall dependencies: rm -rf node_modules && npm install
```

### API Connection Error
```bash
# Ensure backend is running on http://localhost:8080
# Check .env file: REACT_APP_API_URL=http://localhost:8080
# Check CORS: backend SecurityConfig should allow localhost:3000
```

## 📞 Support

For issues or questions:
1. Check the troubleshooting section above
2. Review detailed READMEs in `/backend` and `/frontend`
3. Check browser console (F12) for error messages
4. Review backend logs: `tail -f nohup.out`

## 📄 License

This project is open source and available under the MIT License.

## 👨‍💻 Author

Built as an intelligent alert escalation system for vehicle compliance monitoring.

---

**Last Updated**: February 20, 2026  
**Project Status**: ✅ Production Ready  
**Version**: 1.0.0

### Quick Command Reference

```bash
# Terminal 1: Start Backend
cd backend && mvn spring-boot:run

# Terminal 2: Start Frontend
cd frontend && npm start

# Terminal 3: Access Database
psql -U postgres -d alertdb

# View Logs
cd backend && tail -f nohup.out

# Git Operations
git add -A
git commit -m "Your message"
git push origin main
```

---

**Last Updated**: February 20, 2026  
**Project Status**: ✅ Production Ready  
**Version**: 1.0.0

### Quick Command Reference

```bash
# Terminal 1: Start Backend
cd backend && mvn spring-boot:run

# Terminal 2: Start Frontend
cd frontend && npm start

# Terminal 3: Access Database
psql -U postgres -d alertdb

# View Logs
cd backend && tail -f nohup.out

# Git Operations
git add -A
git commit -m "Your message"
git push origin main
```