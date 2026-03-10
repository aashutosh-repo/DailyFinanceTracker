# Complete Deployment and Configuration Guide

## Table of Contents
1. [Backend Setup](#backend-setup)
2. [Frontend Setup](#frontend-setup)
3. [Docker Setup](#docker-setup)
4. [Database Setup](#database-setup)
5. [Environment Configuration](#environment-configuration)
6. [Security Setup](#security-setup)
7. [Deployment](#deployment)
8. [Monitoring](#monitoring)

---

## Backend Setup

### Prerequisites
- Java 21 JDK installed
- Maven 3.9+
- PostgreSQL 15+
- Redis 7.0+ (optional for caching)

### Project Structure Setup

```bash
# Clone or create backend project
git clone https://github.com/aashutosh-repo/DailyFinanceTracker.git
cd DailyFinanceTracker

# Create Maven project structure
mvn archetype:generate -DgroupId=com.personalfinance \
  -DartifactId=daily-finance-tracker-backend \
  -DarchetypeArtifactId=maven-archetype-quickstart \
  -DinteractiveMode=false
```

### pom.xml Configuration

```xml
<?xml version="1.0" encoding="UTF-8"?>
<project xmlns="http://maven.apache.org/POM/4.0.0"
         xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
         xsi:schemaLocation="http://maven.apache.org/POM/4.0.0 
                             http://maven.apache.org/xsd/maven-4.0.0.xsd">
    <modelVersion>4.0.0</modelVersion>

    <parent>
        <groupId>org.springframework.boot</groupId>
        <artifactId>spring-boot-starter-parent</artifactId>
        <version>3.2.0</version>
        <relativePath/>
    </parent>

    <groupId>com.personalfinance</groupId>
    <artifactId>daily-finance-tracker-backend</artifactId>
    <version>1.0.0</version>
    <name>Daily Finance Tracker Backend</name>
    <description>Spring Boot backend for Daily Personal Finance Tracker</description>

    <properties>
        <java.version>21</java.version>
        <maven.compiler.source>21</maven.compiler.source>
        <maven.compiler.target>21</maven.compiler.target>
        <project.build.sourceEncoding>UTF-8</project.build.sourceEncoding>
        <mapstruct.version>1.5.5.Final</mapstruct.version>
        <springdoc.version>2.0.2</springdoc.version>
    </properties>

    <dependencies>
        <!-- Spring Boot Starters -->
        <dependency>
            <groupId>org.springframework.boot</groupId>
            <artifactId>spring-boot-starter-web</artifactId>
        </dependency>
        
        <dependency>
            <groupId>org.springframework.boot</groupId>
            <artifactId>spring-boot-starter-data-jpa</artifactId>
        </dependency>
        
        <dependency>
            <groupId>org.springframework.boot</groupId>
            <artifactId>spring-boot-starter-security</artifactId>
        </dependency>
        
        <dependency>
            <groupId>org.springframework.boot</groupId>
            <artifactId>spring-boot-starter-validation</artifactId>
        </dependency>
        
        <dependency>
            <groupId>org.springframework.boot</groupId>
            <artifactId>spring-boot-starter-cache</artifactId>
        </dependency>

        <!-- Database -->
        <dependency>
            <groupId>org.postgresql</groupId>
            <artifactId>postgresql</artifactId>
            <version>42.7.0</version>
            <scope>runtime</scope>
        </dependency>

        <!-- Flyway for Database Migration -->
        <dependency>
            <groupId>org.flywaydb</groupId>
            <artifactId>flyway-core</artifactId>
        </dependency>

        <!-- Redis -->
        <dependency>
            <groupId>org.springframework.boot</groupId>
            <artifactId>spring-boot-starter-data-redis</artifactId>
        </dependency>

        <!-- JWT -->
        <dependency>
            <groupId>io.jsonwebtoken</groupId>
            <artifactId>jjwt-api</artifactId>
            <version>0.12.3</version>
        </dependency>
        <dependency>
            <groupId>io.jsonwebtoken</groupId>
            <artifactId>jjwt-impl</artifactId>
            <version>0.12.3</version>
            <scope>runtime</scope>
        </dependency>
        <dependency>
            <groupId>io.jsonwebtoken</groupId>
            <artifactId>jjwt-jackson</artifactId>
            <version>0.12.3</version>
            <scope>runtime</scope>
        </dependency>

        <!-- MapStruct -->
        <dependency>
            <groupId>org.mapstruct</groupId>
            <artifactId>mapstruct</artifactId>
            <version>${mapstruct.version}</version>
        </dependency>

        <!-- Swagger/OpenAPI -->
        <dependency>
            <groupId>org.springdoc</groupId>
            <artifactId>springdoc-openapi-starter-webmvc-ui</artifactId>
            <version>${springdoc.version}</version>
        </dependency>

        <!-- Lombok -->
        <dependency>
            <groupId>org.projectlombok</groupId>
            <artifactId>lombok</artifactId>
            <optional>true</optional>
        </dependency>

        <!-- Testing -->
        <dependency>
            <groupId>org.springframework.boot</groupId>
            <artifactId>spring-boot-starter-test</artifactId>
            <scope>test</scope>
        </dependency>
        
        <dependency>
            <groupId>org.springframework.security</groupId>
            <artifactId>spring-security-test</artifactId>
            <scope>test</scope>
        </dependency>
    </dependencies>

    <build>
        <plugins>
            <plugin>
                <groupId>org.springframework.boot</groupId>
                <artifactId>spring-boot-maven-plugin</artifactId>
                <configuration>
                    <excludes>
                        <exclude>
                            <groupId>org.projectlombok</groupId>
                            <artifactId>lombok</artifactId>
                        </exclude>
                    </excludes>
                </configuration>
            </plugin>

            <!-- MapStruct Annotation Processor -->
            <plugin>
                <groupId>org.apache.maven.plugins</groupId>
                <artifactId>maven-compiler-plugin</artifactId>
                <configuration>
                    <annotationProcessorPaths>
                        <path>
                            <groupId>org.mapstruct</groupId>
                            <artifactId>mapstruct-processor</artifactId>
                            <version>${mapstruct.version}</version>
                        </path>
                        <path>
                            <groupId>org.projectlombok</groupId>
                            <artifactId>lombok</artifactId>
                            <version>${lombok.version}</version>
                        </path>
                    </annotationProcessorPaths>
                </configuration>
            </plugin>
        </plugins>
    </build>
</project>
```

### application.yml Configuration

```yaml
spring:
  application:
    name: daily-finance-tracker-backend
    version: 1.0.0

  # JPA Configuration
  jpa:
    hibernate:
      ddl-auto: validate
    properties:
      hibernate:
        dialect: org.hibernate.dialect.PostgreSQLDialect
        format_sql: true
        jdbc:
          batch_size: 20
          fetch_size: 50
        order_inserts: true
        order_updates: true
    show-sql: false
    open-in-view: false

  # DataSource Configuration
  datasource:
    url: jdbc:postgresql://localhost:5432/daily_finance_tracker
    username: ${DB_USERNAME:finance_user}
    password: ${DB_PASSWORD:password123}
    driver-class-name: org.postgresql.Driver
    hikari:
      maximum-pool-size: 20
      minimum-idle: 5
      connection-timeout: 30000
      idle-timeout: 600000
      max-lifetime: 1800000
      auto-commit: true

  # Redis Configuration
  data:
    redis:
      host: ${REDIS_HOST:localhost}
      port: ${REDIS_PORT:6379}
      password: ${REDIS_PASSWORD:}
      timeout: 60000
      jedis:
        pool:
          max-active: 20
          max-idle: 10
          min-idle: 5

  # Cache Configuration
  cache:
    type: redis
    redis:
      time-to-live: 600000
    cache-names:
      - users
      - categories
      - budgets
      - notifications

  # Servlet
  servlet:
    multipart:
      max-file-size: 10MB
      max-request-size: 10MB

  # Jackson Configuration
  jackson:
    serialization:
      write-dates-as-timestamps: false
      indent-output: true
    deserialization:
      fail-on-unknown-properties: false

server:
  port: 8080
  servlet:
    context-path: /api
  error:
    include-message: always
    include-binding-errors: always
    include-stacktrace: on_param
    include-exception: false

# Security Configuration
app:
  security:
    jwt:
      secret: ${JWT_SECRET:your-super-secret-key-min-32-chars-required}
      expiration: 86400000  # 24 hours in milliseconds
      refresh-expiration: 604800000  # 7 days in milliseconds
  cors:
    allowed-origins: ${CORS_ALLOWED_ORIGINS:http://localhost:4200,http://localhost:3000}
    allowed-methods: GET,POST,PUT,DELETE,PATCH,OPTIONS
    allowed-headers: "*"
    allow-credentials: true
    max-age: 3600

# Logging Configuration
logging:
  level:
    root: INFO
    com.personalfinance: DEBUG
    org.springframework.web: DEBUG
    org.springframework.security: DEBUG
  pattern:
    console: "%d{HH:mm:ss.SSS} [%thread] %-5level %logger{36} - %msg%n"
    file: "%d{yyyy-MM-dd HH:mm:ss} [%thread] %-5level %logger{36} - %msg%n"
  file:
    name: logs/application.log
    max-size: 10MB
    max-history: 30

# Swagger/OpenAPI Configuration
springdoc:
  api-docs:
    path: /v3/api-docs
  swagger-ui:
    path: /swagger-ui.html
    enabled: true
    operations-sorter: method
    tags-sorter: alpha
  paths-to-match:
    - /api/**
  show-actuator: false

# Actuator Configuration
management:
  endpoints:
    web:
      exposure:
        include: health,metrics,prometheus
  endpoint:
    health:
      show-details: when-authorized
  metrics:
    enable:
      jvm: true
      process: true
      system: true

---
# Development Profile
spring:
  config:
    activate:
      on-profile: dev
  datasource:
    url: jdbc:postgresql://localhost:5432/daily_finance_tracker_dev
  jpa:
    hibernate:
      ddl-auto: update
    show-sql: true

logging:
  level:
    root: DEBUG
    com.personalfinance: DEBUG

---
# Production Profile
spring:
  config:
    activate:
      on-profile: prod
  datasource:
    url: jdbc:postgresql://${DB_HOST}:${DB_PORT}/${DB_NAME}
  jpa:
    hibernate:
      ddl-auto: validate
    show-sql: false

logging:
  level:
    root: WARN
    com.personalfinance: INFO
```

---

## Frontend Setup

### Prerequisites
- Node.js 18+ LTS
- npm 9+ or yarn
- Angular CLI 17+

### Project Setup

```bash
# Create Angular 17 project
ng new daily-finance-tracker-frontend --routing --style=scss --standalone=true

cd daily-finance-tracker-frontend

# Install Angular Material
ng add @angular/material

# Install additional dependencies
npm install \
  @ngrx/store@^17 \
  @ngrx/effects@^17 \
  @ngrx/store-devtools@^17 \
  chart.js \
  ng2-charts \
  rxjs \
  axios \
  date-fns

# Install development dependencies
npm install --save-dev \
  @angular/cdk \
  @angular/compiler-cli \
  typescript
```

### Angular Configuration

```json
{
  "$schema": "./node_modules/@angular/cli/lib/config/schema.json",
  "version": 1,
  "newProjectRoot": "projects",
  "projects": {
    "daily-finance-tracker": {
      "projectType": "application",
      "schematics": {
        "@schematics/angular:component": {
          "style": "scss"
        }
      },
      "root": "",
      "sourceRoot": "src",
      "prefix": "app",
      "architect": {
        "build": {
          "builder": "@angular/build:browser",
          "options": {
            "outputPath": "dist/daily-finance-tracker",
            "index": "src/index.html",
            "main": "src/main.ts",
            "polyfills": ["zone.js"],
            "tsConfig": "tsconfig.app.json",
            "assets": [
              "src/favicon.ico",
              "src/assets"
            ],
            "styles": [
              "src/styles.scss"
            ],
            "scripts": [],
            "optimization": true,
            "sourceMap": false,
            "aot": true,
            "vendorChunk": false,
            "buildOptimizer": true
          }
        },
        "serve": {
          "builder": "@angular/build:dev-server",
          "options": {
            "buildTarget": "daily-finance-tracker:build"
          },
          "configurations": {
            "production": {
              "buildTarget": "daily-finance-tracker:build:production"
            },
            "development": {
              "buildTarget": "daily-finance-tracker:build:development"
            }
          },
          "defaultConfiguration": "development"
        }
      }
    }
  }
}
```

### Environment Configuration

**src/environments/environment.ts**
```typescript
export const environment = {
  production: false,
  apiUrl: 'http://localhost:8080/api',
  apiVersion: 'v1',
  jwtTokenKey: 'access_token',
  refreshTokenKey: 'refresh_token',
  tokenExpirationTime: 24 * 60 * 60 * 1000, // 24 hours
  cacheDuration: 5 * 60 * 1000, // 5 minutes
};
```

**src/environments/environment.prod.ts**
```typescript
export const environment = {
  production: true,
  apiUrl: 'https://api.yourfinancetracker.com/api',
  apiVersion: 'v1',
  jwtTokenKey: 'access_token',
  refreshTokenKey: 'refresh_token',
  tokenExpirationTime: 24 * 60 * 60 * 1000,
  cacheDuration: 5 * 60 * 1000,
};
```

---

## Docker Setup

### Backend Dockerfile

```dockerfile
# Build stage
FROM maven:3.9.0-eclipse-temurin-21 AS builder

WORKDIR /build

COPY pom.xml .
RUN mvn dependency:go-offline

COPY src src
RUN mvn clean package -DskipTests

# Runtime stage
FROM eclipse-temurin:21-jre-alpine

WORKDIR /app

COPY --from=builder /build/target/*.jar app.jar

EXPOSE 8080

HEALTHCHECK --interval=30s --timeout=10s --start-period=40s --retries=3 \
    CMD wget --quiet --tries=1 --spider http://localhost:8080/api/health || exit 1

ENTRYPOINT ["java", "-jar", "app.jar"]
```

### Frontend Dockerfile

```dockerfile
# Build stage
FROM node:18-alpine AS builder

WORKDIR /build

COPY package*.json ./
RUN npm ci

COPY . .
RUN npm run build -- --configuration production

# Runtime stage
FROM nginx:alpine

COPY nginx.conf /etc/nginx/nginx.conf
COPY --from=builder /build/dist/daily-finance-tracker /usr/share/nginx/html

EXPOSE 80

HEALTHCHECK --interval=30s --timeout=10s --start-period=40s --retries=3 \
    CMD wget --quiet --tries=1 --spider http://localhost/health || exit 1

CMD ["nginx", "-g", "daemon off;"]
```

### Docker Compose

```yaml
version: '3.8'

services:
  postgres:
    image: postgres:15-alpine
    container_name: finance_db
    environment:
      POSTGRES_DB: daily_finance_tracker
      POSTGRES_USER: ${DB_USERNAME:-finance_user}
      POSTGRES_PASSWORD: ${DB_PASSWORD:-password123}
    ports:
      - "5432:5432"
    volumes:
      - postgres_data:/var/lib/postgresql/data
      - ./docs/database/V1__Initial_Schema.sql:/docker-entrypoint-initdb.d/01-init.sql
    healthcheck:
      test: ["CMD-SHELL", "pg_isready -U ${DB_USERNAME:-finance_user}"]
      interval: 10s
      timeout: 5s
      retries: 5
    networks:
      - finance-network

  redis:
    image: redis:7-alpine
    container_name: finance_cache
    ports:
      - "6379:6379"
    command: redis-server --appendonly yes
    volumes:
      - redis_data:/data
    healthcheck:
      test: ["CMD", "redis-cli", "ping"]
      interval: 10s
      timeout: 5s
      retries: 5
    networks:
      - finance-network

  backend:
    build:
      context: .
      dockerfile: Dockerfile
    container_name: finance_backend
    environment:
      SPRING_PROFILES_ACTIVE: ${SPRING_PROFILE:-prod}
      DB_HOST: postgres
      DB_PORT: 5432
      DB_NAME: daily_finance_tracker
      DB_USERNAME: ${DB_USERNAME:-finance_user}
      DB_PASSWORD: ${DB_PASSWORD:-password123}
      REDIS_HOST: redis
      REDIS_PORT: 6379
      JWT_SECRET: ${JWT_SECRET:-your-super-secret-key-min-32-chars-required}
      CORS_ALLOWED_ORIGINS: ${CORS_ALLOWED_ORIGINS:-http://localhost:3000}
    ports:
      - "8080:8080"
    depends_on:
      postgres:
        condition: service_healthy
      redis:
        condition: service_healthy
    networks:
      - finance-network
    restart: unless-stopped

  frontend:
    build:
      context: ./frontend
      dockerfile: Dockerfile
    container_name: finance_frontend
    ports:
      - "3000:80"
    depends_on:
      - backend
    networks:
      - finance-network
    restart: unless-stopped

  nginx:
    image: nginx:alpine
    container_name: finance_gateway
    ports:
      - "80:80"
      - "443:443"
    volumes:
      - ./nginx.conf:/etc/nginx/nginx.conf:ro
      - ./ssl/cert.pem:/etc/nginx/ssl/cert.pem:ro
      - ./ssl/key.pem:/etc/nginx/ssl/key.pem:ro
    depends_on:
      - backend
      - frontend
    networks:
      - finance-network
    restart: unless-stopped

volumes:
  postgres_data:
  redis_data:

networks:
  finance-network:
    driver: bridge
```

---

## Database Setup

### PostgreSQL Installation

```bash
# macOS
brew install postgresql@15
brew services start postgresql@15

# Ubuntu/Debian
sudo apt-get install postgresql postgresql-contrib
sudo systemctl start postgresql

# Windows
# Download from https://www.postgresql.org/download/windows/
```

### Create Database and User

```sql
-- Create user
CREATE USER finance_user WITH ENCRYPTED PASSWORD 'password123';

-- Create database
CREATE DATABASE daily_finance_tracker OWNER finance_user;

-- Grant privileges
GRANT ALL PRIVILEGES ON DATABASE daily_finance_tracker TO finance_user;

-- Connect to database
\c daily_finance_tracker

-- Grant schema privileges
GRANT ALL ON SCHEMA public TO finance_user;
ALTER DEFAULT PRIVILEGES IN SCHEMA public GRANT ALL ON TABLES TO finance_user;
ALTER DEFAULT PRIVILEGES IN SCHEMA public GRANT ALL ON SEQUENCES TO finance_user;
```

### Run Initial Schema

```bash
# Using psql
psql -U finance_user -d daily_finance_tracker < docs/database/V1__Initial_Schema.sql

# Using Flyway (automatic with Spring Boot)
# Just run the application, Flyway will automatically migrate
```

---

## Environment Configuration

### .env File

```bash
# Backend Configuration
SPRING_PROFILE=prod
DB_HOST=localhost
DB_PORT=5432
DB_NAME=daily_finance_tracker
DB_USERNAME=finance_user
DB_PASSWORD=your_secure_password_here
JWT_SECRET=your-super-secret-key-with-min-32-characters-required-here
CORS_ALLOWED_ORIGINS=http://localhost:3000,https://yourfrontend.com

# Redis Configuration
REDIS_HOST=localhost
REDIS_PORT=6379
REDIS_PASSWORD=

# Frontend Configuration
REACT_APP_API_URL=http://localhost:8080/api
NODE_ENV=production

# Email Configuration (for notifications)
MAIL_HOST=smtp.gmail.com
MAIL_PORT=587
MAIL_USERNAME=your-email@gmail.com
MAIL_PASSWORD=your-app-password
MAIL_FROM=noreply@yourfinancetracker.com

# AWS Configuration (if using AWS)
AWS_ACCESS_KEY_ID=your-access-key
AWS_SECRET_ACCESS_KEY=your-secret-key
AWS_REGION=us-east-1
```

---

## Security Setup

### JWT Configuration

```java
@Configuration
public class JwtConfig {
    
    @Value("${app.security.jwt.secret}")
    private String jwtSecret;
    
    @Value("${app.security.jwt.expiration}")
    private long jwtExpiration;
    
    public String generateToken(UserDetails userDetails) {
        return Jwts.builder()
            .subject(userDetails.getUsername())
            .issuedAt(new Date())
            .expiration(new Date(System.currentTimeMillis() + jwtExpiration))
            .signWith(SignatureAlgorithm.HS256, jwtSecret)
            .compact();
    }
    
    public String extractUsername(String token) {
        return Jwts.parserBuilder()
            .setSigningKey(jwtSecret)
            .build()
            .parseClaimsJws(token)
            .getBody()
            .getSubject();
    }
    
    public boolean validateToken(String token) {
        try {
            Jwts.parserBuilder()
                .setSigningKey(jwtSecret)
                .build()
                .parseClaimsJws(token);
            return true;
        } catch (JwtException | IllegalArgumentException e) {
            return false;
        }
    }
}
```

### CORS Configuration

```java
@Configuration
public class CorsConfig implements WebMvcConfigurer {
    
    @Value("${app.cors.allowed-origins}")
    private String[] allowedOrigins;
    
    @Override
    public void addCorsMappings(CorsRegistry registry) {
        registry.addMapping("/api/**")
            .allowedOrigins(allowedOrigins)
            .allowedMethods("GET", "POST", "PUT", "DELETE", "PATCH", "OPTIONS")
            .allowedHeaders("*")
            .allowCredentials(true)
            .maxAge(3600);
    }
}
```

---

## Deployment

### AWS Deployment

1. **Create RDS PostgreSQL Instance**
   ```bash
   aws rds create-db-instance \
       --db-instance-identifier daily-finance-tracker \
       --db-instance-class db.t3.micro \
       --engine postgres \
       --master-username finance_user \
       --master-user-password YourSecurePassword123!
   ```

2. **Create ElastiCache Redis**
   ```bash
   aws elasticache create-cache-cluster \
       --cache-cluster-id daily-finance-redis \
       --cache-node-type cache.t3.micro \
       --engine redis
   ```

3. **Deploy with ECS/Fargate or EC2**
   - Push Docker images to ECR
   - Create task definitions
   - Deploy services

### Docker Deployment

```bash
# Build images
docker-compose build

# Deploy
docker-compose up -d

# Check logs
docker-compose logs -f backend
```

### Kubernetes Deployment (Optional)

```yaml
apiVersion: apps/v1
kind: Deployment
metadata:
  name: finance-backend
spec:
  replicas: 3
  selector:
    matchLabels:
      app: finance-backend
  template:
    metadata:
      labels:
        app: finance-backend
    spec:
      containers:
      - name: backend
        image: your-registry/daily-finance-tracker-backend:latest
        ports:
        - containerPort: 8080
        env:
        - name: DB_HOST
          valueFrom:
            configMapKeyRef:
              name: db-config
              key: host
        - name: DB_PASSWORD
          valueFrom:
            secretKeyRef:
              name: db-secret
              key: password
        livenessProbe:
          httpGet:
            path: /api/health
            port: 8080
          initialDelaySeconds: 30
          periodSeconds: 10
        readinessProbe:
          httpGet:
            path: /api/health
            port: 8080
          initialDelaySeconds: 5
          periodSeconds: 5
```

---

## Monitoring

### Prometheus Configuration

```yaml
global:
  scrape_interval: 15s

scrape_configs:
  - job_name: 'finance-backend'
    metrics_path: '/api/actuator/prometheus'
    static_configs:
      - targets: ['localhost:8080']
```

### Grafana Dashboards

- JVM Metrics
- Application Metrics
- PostgreSQL Metrics
- Redis Metrics
- Request Duration

---

## Scaling Considerations

### Horizontal Scaling
- Use load balancer (Nginx, AWS ALB)
- Stateless backend services
- Distributed session management (Redis)

### Database Optimization
- Connection pooling (HikariCP)
- Query optimization
- Read replicas for reporting

### Caching Strategy
- Redis for distributed cache
- Cache invalidation on updates
- TTL-based expiration

