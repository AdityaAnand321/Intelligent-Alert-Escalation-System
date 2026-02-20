# Alert System - Frontend (React)

A modern React application for managing and monitoring alerts with real-time updates, user authentication, and comprehensive dashboard.

## 📋 Table of Contents

- [Features](#features)
- [Tech Stack](#tech-stack)
- [Project Structure](#project-structure)
- [Installation](#installation)
- [Configuration](#configuration)
- [Running the Application](#running-the-application)
- [Building for Production](#building-for-production)
- [API Integration](#api-integration)
- [Pages and Components](#pages-and-components)
- [Authentication](#authentication)
- [Troubleshooting](#troubleshooting)

## ✨ Features

- **User Authentication**: Secure login and registration with JWT tokens
- **Dashboard**: Real-time statistics and alerts overview with charts
- **Alert Management**: Create, view, filter, and resolve alerts
- **User Management**: View all registered users in the system
- **Responsive Design**: Works on desktop and mobile devices
- **Protected Routes**: Role-based access control for authenticated users
- **Error Handling**: Comprehensive error messages and user feedback

## 🛠️ Tech Stack

- **React 19.2**: Modern UI library
- **React Router DOM 7.13**: Client-side routing
- **Axios 1.13**: HTTP client for API calls
- **CSS 3**: Responsive styling with flexbox and grid
- **JavaScript (ES6+)**: Modern JavaScript features

## 📁 Project Structure

```
frontend/
├── public/
│   └── index.html            # HTML entry point
├── src/
│   ├── api.js                # Axios configuration and API calls
│   ├── index.js              # React entry point
│   ├── App.js                # Main app component with routing
│   ├── pages/
│   │   ├── LoginPage.js      # Authentication page
│   │   ├── DashboardPage.js  # Dashboard with statistics
│   │   ├── AlertsPage.js     # Alert management
│   │   └── UsersPage.js      # User listing
│   ├── components/
│   │   └── Navigation.js     # Header/navbar
│   └── styles/
│       ├── index.css         # Global styles
│       ├── App.css           # App layout styles
│       ├── Navigation.css    # Navbar styles
│       ├── Auth.css          # Login/Register styles
│       ├── Dashboard.css     # Dashboard styles
│       ├── Alerts.css        # Alerts page styles
│       └── Users.css         # Users page styles
├── package.json              # NPM dependencies and scripts
├── .env                      # Environment variables
├── .env.example              # Example environment file
└── .gitignore                # Git ignore file
```

## 🚀 Installation

### Prerequisites

- **Node.js**: Version 14+ (includes npm)
- **Backend API**: Running on http://localhost:8080

### Steps

1. **Navigate to frontend directory**:
   ```bash
   cd frontend
   ```

2. **Install dependencies**:
   ```bash
   npm install
   ```

   The following packages will be installed:
   - react & react-dom (UI library)
   - react-router-dom (routing)
   - axios (HTTP client)
   - react-scripts (build tools)
   - web-vitals (performance monitoring)

3. **Verify installation**:
   ```bash
   npm list
   ```

## ⚙️ Configuration

### Environment Variables

1. **Copy the example environment file**:
   ```bash
   cp .env.example .env
   ```

2. **Configure API URL** (in `.env`):
   ```
   REACT_APP_API_URL=http://localhost:8080
   ```

   Default value is already set to `http://localhost:8080`, update if your backend runs on a different URL.

### Backend Requirements

The backend Spring Boot API must be:
- Running on `http://localhost:8080`
- Have enabled CORS for `http://localhost:3000`
- Provide these endpoints:
  - `POST /api/auth/register` - User registration
  - `POST /api/auth/login` - User login
  - `GET /api/auth/users` - Get all users
  - `GET /api/alerts` - Get all alerts
  - `POST /api/alerts` - Create alert
  - `GET /api/alerts/{id}` - Get alert details
  - `PATCH /api/alerts/{id}/resolve` - Resolve alert
  - `GET /api/dashboard/summary` - Dashboard stats
  - `GET /api/dashboard/top-offenders` - Top offenders
  - `GET /api/dashboard/events` - Recent events

## ▶️ Running the Application

### Development Server

1. **Start the React development server**:
   ```bash
   npm start
   ```

   The application will automatically open in your browser at `http://localhost:3000`

2. **Features in development mode**:
   - Hot reload: Changes are reflected instantly
   - Error overlay: Compilation errors displayed in browser
   - DevTools: React and Redux DevTools support

### Accessing the Application

- **URL**: `http://localhost:3000`
- **Login Page**: Direct from home if not authenticated
- **Default Credentials**: Create an account or use test account
  - Username: `testuser`
  - Password: `Test123` (if it exists from backend)

## 🏗️ Building for Production

### Create Optimized Build

```bash
npm run build
```

This creates a `build/` folder with optimized production files:
- JavaScript and CSS minified
- Code splitting for better performance
- HTML optimized and ready to serve

### Serve Production Build Locally

```bash
npx serve -s build
```

The app will be available at `http://localhost:3000` (or next available port)

### Deploy to Production

The `build/` folder can be:
- Deployed to static hosting (Vercel, Netlify, GitHub Pages)
- Served from any web server (Apache, Nginx)
- Containerized with Docker

## 🔌 API Integration

### How API Calls Work

1. **Axios Configuration** (`src/api.js`):
   - Base URL set from environment variable
   - JWT token automatically added to headers
   - Token stored in localStorage

2. **Request Interceptors**:
   ```javascript
   api.interceptors.request.use(config => {
     const token = localStorage.getItem('token');
     if (token) {
       config.headers.Authorization = `Bearer ${token}`;
     }
     return config;
   });
   ```

3. **Example API Call**:
   ```javascript
   import { authAPI } from '../api';

   // Login
   const response = await authAPI.login(username, password);
   localStorage.setItem('token', response.data.token);
   ```

## 📄 Pages and Components

### 1. Login Page (`pages/LoginPage.js`)
- User registration and login
- Form validation
- JWT token storage
- Redirect to dashboard on success

### 2. Dashboard Page (`pages/DashboardPage.js`)
- Statistics cards (total, critical, escalated, resolved alerts)
- Recent alerts table
- Top offenders list
- Real-time data from API

### 3. Alerts Page (`pages/AlertsPage.js`)
- Create new alerts with form
- View all alerts in table
- Alert severity badges
- Click alert to view details modal
- Resolve alerts with confirmation
- Metadata viewing

### 4. Users Page (`pages/UsersPage.js`)
- List all registered users
- Add new user form
- User role display
- Account creation date

### 5. Navigation Component (`components/Navigation.js`)
- Header with app title
- Navigation menu (Dashboard, Alerts, Users)
- User profile display
- Logout button with confirmation

## 🔐 Authentication

### Login Flow

1. User enters credentials on login page
2. POST request to `/api/auth/login`
3. Backend returns JWT token
4. Token stored in localStorage as `token`
5. Token added to ALL API requests via interceptor
6. User redirected to dashboard

### Protected Routes

Routes requiring authentication:
- `/dashboard`
- `/alerts`
- `/users`

Unauthenticated users redirected to login page.

### Logout

1. User clicks logout (navbar)
2. localStorage cleared (token and username)
3. App redirected to login page

## 🐛 Troubleshooting

### Port Already in Use

**Problem**: "Port 3000 is already in use"

**Solution**:
```bash
# Kill process on port 3000
# On Windows:
netstat -ano | findstr :3000
taskkill /PID <PID> /F

# On Mac/Linux:
lsof -ti:3000 | xargs kill -9
```

### Backend Connection Error

**Problem**: "Failed to load dashboard data" or "Cannot connect to API"

**Solution**:
1. Check backend is running: `curl http://localhost:8080/api/auth/users`
2. Update `.env` file with correct API URL
3. Verify CORS is enabled on backend
4. Check console (F12) for detailed error messages

### npm Install Issues

**Problem**: "npm WARN peer dep"

**Solution**: These are already handled with `--legacy-peer-deps`. NPM install is working correctly.

### Blank Page or Not Loading

**Problem**: Page stays blank after login

**Solution**:
1. Open browser console (F12)
2. Check for JavaScript errors
3. Verify backend APIs are working
4. Clear browser cache: Ctrl+Shift+Del

### Token Expires

**Problem**: "401 Unauthorized" errors after some time

**Solution**:
1. Backend JWT tokens expire after 24 hours
2. User must log in again
3. To extend expiration, update backend SecurityConfig

## 📚 Useful Commands

```bash
# Install dependencies
npm install

# Start development server
npm start

# Build for production
npm run build

# Run tests
npm test

# View npm size
npm run build && ls -lh build/

# Audit dependencies for vulnerabilities
npm audit

# Fix vulnerabilities (be careful)
npm audit fix
```

## 🤝 Support

For frontend-specific issues:
1. Check browser console (F12) for errors
2. Verify backend is running and accessible
3. Check network tab for failed API calls
4. Review environment configuration

For backend API issues, see `../backend/README.md`

---

**Last Updated**: February 20, 2026
**Version**: 1.0.0
**Status**: Production Ready
