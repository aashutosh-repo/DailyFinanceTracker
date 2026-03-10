# Quick Start Guide - Daily Personal Finance Tracker

## Table of Contents
1. [Local Development Setup](#local-development-setup)
2. [Running with Docker](#running-with-docker)
3. [First Steps](#first-steps)
4. [Common Tasks](#common-tasks)
5. [Troubleshooting](#troubleshooting)

---

## Local Development Setup

### Prerequisites

```bash
# Check Java version (should be 21+)
java -version

# Check Maven version (should be 3.9+)
mvn -version

# Check Node version (should be 18+)
node -version

# Check npm version (should be 9+)
npm -version

# Check PostgreSQL version (should be 15+)
psql --version

# Check Redis version (should be 7+)
redis-cli --version
```

### Step 1: Clone Repository

```bash
# Clone backend repository
git clone https://github.com/aashutosh-repo/DailyFinanceTracker.git
cd DailyFinanceTracker

# Clone frontend repository
git clone https://github.com/aashutosh-repo/Finance-tracker-UI.git
cd Finance-tracker-UI
```

### Step 2: Database Setup

```bash
# Start PostgreSQL service
# macOS
brew services start postgresql@15

# Ubuntu/Debian
sudo systemctl start postgresql

# Windows - PostgreSQL service should start automatically

# Connect to PostgreSQL
psql -U postgres

# Create database and user
CREATE USER finance_user WITH ENCRYPTED PASSWORD 'password123';
CREATE DATABASE daily_finance_tracker OWNER finance_user;
GRANT ALL PRIVILEGES ON DATABASE daily_finance_tracker TO finance_user;

# Load initial schema
\c daily_finance_tracker
\i docs/database/V1__Initial_Schema.sql

# Exit
\q
```

### Step 3: Redis Setup

```bash
# Start Redis
# macOS
brew services start redis

# Ubuntu/Debian
sudo systemctl start redis-server

# Windows - Download from https://github.com/microsoftarchive/redis/releases
redis-server

# Verify Redis is running
redis-cli ping
# Expected output: PONG
```

### Step 4: Backend Setup

```bash
# Navigate to backend directory
cd DailyFinanceTracker

# Create .env file
cat > .env << EOF
SPRING_PROFILE=dev
DB_HOST=localhost
DB_PORT=5432
DB_NAME=daily_finance_tracker
DB_USERNAME=finance_user
DB_PASSWORD=password123
JWT_SECRET=your-super-secret-key-with-min-32-characters-required-here
REDIS_HOST=localhost
REDIS_PORT=6379
CORS_ALLOWED_ORIGINS=http://localhost:4200,http://localhost:3000
EOF

# Build backend
mvn clean install

# Run backend
mvn spring-boot:run -Dspring-boot.run.arguments="--spring.profiles.active=dev"

# Backend should start on http://localhost:8080
# API documentation: http://localhost:8080/swagger-ui.html
```

### Step 5: Frontend Setup

```bash
# Navigate to frontend directory
cd Finance-tracker-UI

# Create .env file
cat > .env << EOF
REACT_APP_API_URL=http://localhost:8080/api
REACT_APP_API_VERSION=v1
NODE_ENV=development
EOF

# Install dependencies
npm install

# Run frontend
npm start

# Frontend should start on http://localhost:4200
```

### Step 6: Verify Installation

```bash
# Test backend API
curl -X GET http://localhost:8080/api/health

# Test database connection
psql -U finance_user -d daily_finance_tracker -c "SELECT version();"

# Test Redis connection
redis-cli ping

# Open in browser
# Frontend: http://localhost:4200
# Backend Swagger UI: http://localhost:8080/swagger-ui.html
```

---

## Running with Docker

### Quick Start with Docker Compose

```bash
# Navigate to project root
cd DailyFinanceTracker

# Create .env file
cat > .env << EOF
DB_USERNAME=finance_user
DB_PASSWORD=password123
JWT_SECRET=your-super-secret-key-min-32-chars
SPRING_PROFILE=prod
CORS_ALLOWED_ORIGINS=http://localhost:3000
EOF

# Build and start all services
docker-compose up -d

# Check services status
docker-compose ps

# View logs
docker-compose logs -f

# Stop all services
docker-compose down
```

### Verify Docker Setup

```bash
# Backend health check
curl http://localhost:8080/api/health

# Frontend check
curl http://localhost:3000

# View services
docker-compose ps

# View specific service logs
docker-compose logs backend
docker-compose logs frontend
docker-compose logs postgres
```

### Docker Service Ports

| Service | Port | URL |
|---------|------|-----|
| Frontend (Nginx) | 3000 | http://localhost:3000 |
| Backend (Spring) | 8080 | http://localhost:8080 |
| PostgreSQL | 5432 | localhost:5432 |
| Redis | 6379 | localhost:6379 |
| API Gateway | 80 | http://localhost |

---

## First Steps

### 1. Register a New Account

```bash
curl -X POST http://localhost:8080/api/v1/auth/register \
  -H "Content-Type: application/json" \
  -d '{
    "username": "john_doe",
    "email": "john@example.com",
    "fullName": "John Doe",
    "password": "SecurePassword123!",
    "passwordConfirm": "SecurePassword123!",
    "phone": "+1234567890",
    "currency": "USD"
  }'
```

### 2. Login

```bash
curl -X POST http://localhost:8080/api/v1/auth/login \
  -H "Content-Type: application/json" \
  -d '{
    "usernameOrEmail": "john_doe",
    "password": "SecurePassword123!",
    "rememberMe": true
  }'

# Response will contain accessToken and refreshToken
```

### 3. Create Expense Category

```bash
curl -X POST http://localhost:8080/api/v1/categories/expenses \
  -H "Authorization: Bearer YOUR_ACCESS_TOKEN" \
  -H "Content-Type: application/json" \
  -d '{
    "name": "Food & Dining",
    "description": "Groceries and restaurants",
    "colorCode": "#FF6B6B",
    "monthlyBudget": 500
  }'
```

### 4. Create First Expense

```bash
curl -X POST http://localhost:8080/api/v1/expenses \
  -H "Authorization: Bearer YOUR_ACCESS_TOKEN" \
  -H "Content-Type: application/json" \
  -d '{
    "description": "Grocery shopping",
    "amount": 45.99,
    "categoryId": 1,
    "expenseDate": "2026-03-07",
    "currency": "USD",
    "paymentMethod": "CARD",
    "tags": ["grocery", "food"]
  }'
```

### 5. View Dashboard

Visit http://localhost:4200/dashboard to see your data

---

## Common Tasks

### Add Sample Data

```bash
# Run sample data loader
mvn exec:java -Dexec.mainClass="com.personalfinance.SampleDataLoader"
```

### View Database

```bash
# Connect to database
psql -U finance_user -d daily_finance_tracker

# View tables
\dt

# View users
SELECT id, username, email, is_active FROM users;

# View expenses
SELECT * FROM expenses LIMIT 10;

# Exit
\q
```

### View Redis Cache

```bash
# Connect to Redis
redis-cli

# View all keys
KEYS *

# View specific key
GET "user:1:expenses"

# Clear all cache
FLUSHALL

# Exit
EXIT
```

### Check Logs

**Backend logs:**
```bash
# Tail logs
tail -f logs/application.log

# View specific error
grep ERROR logs/application.log
```

**Frontend logs:**
```bash
# Browser console (F12)
# Check browser developer tools for errors
```

### Database Backup

```bash
# Backup database
pg_dump -U finance_user daily_finance_tracker > backup.sql

# Restore database
psql -U finance_user daily_finance_tracker < backup.sql
```

### Reset Database

```bash
# Drop and recreate database
psql -U postgres -c "DROP DATABASE daily_finance_tracker;"
psql -U postgres -c "CREATE DATABASE daily_finance_tracker OWNER finance_user;"
psql -U finance_user -d daily_finance_tracker -f docs/database/V1__Initial_Schema.sql
```

### Update Dependencies

```bash
# Backend
mvn versions:display-dependency-updates
mvn versions:use-latest-releases

# Frontend
npm outdated
npm update
```

---

## Development Workflow

### Backend Development

```bash
# Navigate to backend directory
cd DailyFinanceTracker

# Run tests
mvn test

# Run with debugging
mvn spring-boot:run -Dspring-boot.run.jvmArguments="-Xdebug -Xrunjdwp:transport=dt_socket,server=y,suspend=y,address=5005"

# Build JAR
mvn clean package

# Run JAR
java -jar target/daily-finance-tracker-backend-1.0.0.jar
```

### Frontend Development

```bash
# Navigate to frontend directory
cd Finance-tracker-UI

# Start dev server
ng serve

# Build for production
ng build --configuration=production

# Run unit tests
ng test

# Run E2E tests
ng e2e
```

### Code Quality

```bash
# Backend code quality check
mvn clean verify

# Frontend linting
npm run lint

# Frontend build analysis
npm run build:stats
```

---

## Troubleshooting

### Backend Won't Start

```bash
# Check Java version
java -version

# Check if port 8080 is already in use
lsof -i :8080
kill -9 <PID>

# Check database connection
psql -U finance_user -d daily_finance_tracker -c "SELECT 1;"

# Clear Maven cache
mvn clean

# Check logs
cat logs/application.log
```

### Frontend Won't Start

```bash
# Check Node version
node -version

# Clear npm cache
npm cache clean --force

# Reinstall dependencies
rm -rf node_modules package-lock.json
npm install

# Check if port 4200 is in use
lsof -i :4200
kill -9 <PID>
```

### Database Connection Issues

```bash
# Check PostgreSQL is running
psql -U postgres -c "SELECT version();"

# Check database exists
psql -U postgres -l | grep daily_finance_tracker

# Check user permissions
psql -U postgres -c "\du"

# Reset password
psql -U postgres -c "ALTER USER finance_user WITH PASSWORD 'new_password';"
```

### Redis Connection Issues

```bash
# Check Redis is running
redis-cli ping

# Restart Redis
redis-cli shutdown
redis-server

# Check Redis logs
cat /var/log/redis.log
```

### Docker Issues

```bash
# Check Docker is running
docker ps

# Rebuild images
docker-compose build --no-cache

# View container logs
docker-compose logs <service-name>

# Remove all containers
docker-compose down -v

# Restart services
docker-compose restart
```

### API Calls Not Working

```bash
# Test API without token
curl -X GET http://localhost:8080/api/health

# Check CORS settings in application.yml
grep -A 5 "cors:" docs/deployment/DEPLOYMENT_GUIDE.md

# Test with valid token
# 1. Get token from login endpoint
# 2. Use in Authorization header: Authorization: Bearer <token>
```

### Memory Issues

```bash
# Increase JVM memory
JAVA_OPTS="-Xmx1024m -Xms512m"
mvn spring-boot:run

# Docker memory limit
docker-compose.yml: mem_limit: 1gb
```

---

## Performance Optimization

### Backend Performance

```bash
# Enable production profile
mvn spring-boot:run -Dspring-boot.run.arguments="--spring.profiles.active=prod"

# Monitor JVM
jvisualvm

# Profile application
mvn profiler:profile
```

### Frontend Performance

```bash
# Build optimization
ng build --configuration=production --build-optimizer

# Analyze bundle size
npm run build:stats

# Lazy load modules
# Configure in routing module with loadChildren
```

### Database Performance

```bash
# Check slow queries
psql -U finance_user -d daily_finance_tracker
SELECT query, calls, total_time FROM pg_stat_statements 
ORDER BY total_time DESC LIMIT 10;

# Analyze query plans
EXPLAIN ANALYZE SELECT * FROM expenses WHERE user_id = 1;

# Create indexes
CREATE INDEX idx_custom ON expenses(user_id, expense_date);
```

---

## Security Best Practices (Development)

1. **Keep JWT Secret Strong**
   - Min 32 characters
   - Use environment variable
   - Never commit to repository

2. **Database Credentials**
   - Use different credentials for dev/prod
   - Store in environment variables
   - Rotate regularly

3. **CORS Configuration**
   - Restrict to specific origins
   - Enable credentials only if needed

4. **SSL/TLS**
   - Use HTTPS in production
   - Generate SSL certificates

5. **API Keys**
   - Rotate regularly
   - Use role-based access
   - Monitor usage

---

## Environment Variables Summary

```bash
# Backend
SPRING_PROFILE=dev
DB_HOST=localhost
DB_PORT=5432
DB_NAME=daily_finance_tracker
DB_USERNAME=finance_user
DB_PASSWORD=password123
JWT_SECRET=your-32-char-secret-key
REDIS_HOST=localhost
REDIS_PORT=6379
CORS_ALLOWED_ORIGINS=http://localhost:4200

# Frontend
REACT_APP_API_URL=http://localhost:8080/api
REACT_APP_API_VERSION=v1
NODE_ENV=development
```

---

## Next Steps

1. ✅ Setup complete!
2. 📚 Read [SYSTEM_ARCHITECTURE.md](./SYSTEM_ARCHITECTURE.md)
3. 🔌 Review [API_DOCUMENTATION.md](./api/API_DOCUMENTATION.md)
4. 🧪 Check [TESTING_GUIDE.md](./TESTING_GUIDE.md)
5. 🚀 Deploy with [DEPLOYMENT_GUIDE.md](./deployment/DEPLOYMENT_GUIDE.md)

