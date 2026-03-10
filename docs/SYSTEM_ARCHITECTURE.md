# Daily Personal Finance Tracker - Complete System Architecture

## Table of Contents
1. [System Architecture Overview](#system-architecture-overview)
2. [Architecture Decisions](#architecture-decisions)
3. [Layered Architecture](#layered-architecture)
4. [Folder Structure](#folder-structure)
5. [Design Patterns](#design-patterns)
6. [Technology Stack](#technology-stack)

---

## System Architecture Overview

### High-Level Architecture Diagram

```
┌─────────────────────────────────────────────────────────────────┐
│                     Client Layer (Angular 17+)                   │
│  ┌──────────────────────────────────────────────────────────┐   │
│  │ Components │ Pages │ Services │ Guards │ Interceptors   │   │
│  │ State Mgmt │ Routing │ Forms │ HTTP Client              │   │
│  └──────────────────────────────────────────────────────────┘   │
└─────────────────────────────────────────────────────────────────┘
                              ↓ HTTPS
┌─────────────────────────────────────────────────────────────────┐
│                   API Gateway / Load Balancer                     │
│                        (Nginx / AWS ALB)                          │
└─────────────────────────────────────────────────────────────────┘
                              ↓
┌─────────────────────────────────────────────────────────────────┐
│              Backend Layer (Spring Boot 3+, Java 21)              │
│  ┌──────────────────────────────────────────────────────────┐   │
│  │  REST Controllers  │  Security  │  Exception Handling   │   │
│  │  Service Layer     │  Validation │ Interceptors         │   │
│  │  Repository Layer  │  Mappers    │ Batch Processing     │   │
│  └──────────────────────────────────────────────────────────┘   │
└─────────────────────────────────────────────────────────────────┘
                              ↓
┌─────────────────────────────────────────────────────────────────┐
│                  Data Access Layer (JPA/Hibernate)               │
│  ┌──────────────────────────────────────────────────────────┐   │
│  │  Entity Classes  │  Repositories  │ Query Methods       │   │
│  │  Caching Layer   │ Pagination     │ Filtering           │   │
│  └──────────────────────────────────────────────────────────┘   │
└─────────────────────────────────────────────────────────────────┘
                              ↓
┌──────────────────────────────────────────────────────┐
│           Persistence Layer (PostgreSQL)             │
│  ┌──────────────────────────────────────────────┐   │
│  │  Users │ Transactions │ Budgets │ Goals    │   │
│  │  Investments │ Notifications │ Audit Logs │   │
│  └──────────────────────────────────────────────┘   │
└──────────────────────────────────────────────────────┘
                      ↓          ↓
         ┌─────────────────┐  ┌──────────────┐
         │   Redis Cache   │  │  PostgreSQL  │
         └─────────────────┘  └──────────────┘
```

---

## Architecture Decisions

### 1. Monolith vs Microservices

**Decision: Modular Monolith (Future-Ready for Microservices)**

**Rationale:**
- **Monolith Advantages:**
  - Simpler deployment for initial release
  - Easier transaction management across modules
  - Unified testing and debugging
  - Lower operational overhead
  - Perfect for teams < 10 members

- **Future Microservices Plan:**
  - Service-oriented architecture (packages organized as potential services)
  - Independent data stores (separated by domain)
  - Clear service boundaries (User, Transaction, Budget, Investment services)
  - Event-driven communication ready (Kafka/RabbitMQ ready)

**Modules (Future Microservices):**
```
├── user-service (Authentication, Profile)
├── transaction-service (Expenses, Income)
├── budget-service (Budgets, Alerts)
├── investment-service (Portfolio, Tracking)
├── notification-service (Alerts, Reminders)
├── analytics-service (Reports, Insights)
└── gateway-service (API Gateway)
```

### 2. Layered Architecture

```
┌─────────────────────────────────────────────┐
│           Presentation Layer                 │
│  Controllers │ DTOs │ Validation            │
├─────────────────────────────────────────────┤
│           Application/Service Layer          │
│  Business Logic │ Orchestration             │
├─────────────────────────────────────────────┤
│           Domain/Entity Layer                │
│  Domain Models │ Business Rules             │
├─────────────────────────────────────────────┤
│           Persistence Layer                  │
│  Repositories │ Data Access Logic           │
├─────────────────────────────────────────────┤
│           Database Layer                     │
│  PostgreSQL │ Redis │ Caching               │
└─────────────────────────────────────────────┘
```

---

## Layered Architecture Details

### Layer 1: Presentation Layer (Controllers)
**Responsibility:** Handle HTTP requests/responses

```java
@RestController
@RequestMapping("/api/v1/expenses")
public class ExpenseController {
    // Receives HTTP requests
    // Validates input
    // Returns standardized responses
}
```

**Key Components:**
- REST Controllers
- Request DTOs (DTO = Data Transfer Object)
- Response DTOs
- Input validation annotations (@Valid, @NotNull, etc.)
- Swagger/OpenAPI documentation

---

### Layer 2: Application/Service Layer
**Responsibility:** Business logic and orchestration

```java
@Service
public class ExpenseService {
    // Business logic
    // Transactions management
    // Caching
    // Event publishing
}
```

**Key Components:**
- Service classes
- Transaction management
- Caching logic
- Business rule enforcement
- Event publishing

---

### Layer 3: Domain/Entity Layer
**Responsibility:** Domain models and business entities

```java
@Entity
public class Expense {
    // Core business entity
    // JPA annotations
    // Business methods
}
```

**Key Components:**
- JPA Entity classes
- Value Objects
- Domain exceptions
- Business logic

---

### Layer 4: Persistence Layer (Repositories)
**Responsibility:** Data access abstraction

```java
@Repository
public interface ExpenseRepository extends JpaRepository<Expense, Long> {
    // Data access methods
    // Query methods
}
```

**Key Components:**
- Spring Data JPA repositories
- Custom query methods
- Pagination and sorting
- Specification for complex queries

---

### Layer 5: Database Layer
**Responsibility:** Physical data storage

**Components:**
- PostgreSQL (Primary database)
- Redis (Caching layer)
- Database migrations (Flyway/Liquibase)
- Indexes and constraints

---

## Folder Structure

### Backend (Spring Boot) Structure

```
daily-finance-tracker-backend/
│
├── src/main/java/com/personalfinance/
│   │
│   ├── config/                          # Configuration classes
│   │   ├── SecurityConfig.java
│   │   ├── JwtConfig.java
│   │   ├── CacheConfig.java
│   │   └── SwaggerConfig.java
│   │
│   ├── controller/                      # Presentation Layer
│   │   ├── auth/
│   │   │   ├── AuthController.java
│   │   │   └── ProfileController.java
│   │   ├── expense/
│   │   │   └── ExpenseController.java
│   │   ├── income/
│   │   │   └── IncomeController.java
│   │   ├── budget/
│   │   │   └── BudgetController.java
│   │   ├── investment/
│   │   │   └── InvestmentController.java
│   │   ├── analytics/
│   │   │   └── AnalyticsController.java
│   │   └── report/
│   │       └── ReportController.java
│   │
│   ├── dto/                             # Data Transfer Objects
│   │   ├── auth/
│   │   │   ├── LoginRequest.java
│   │   │   ├── RegisterRequest.java
│   │   │   ├── AuthResponse.java
│   │   │   └── TokenRefreshRequest.java
│   │   ├── expense/
│   │   │   ├── ExpenseRequest.java
│   │   │   ├── ExpenseResponse.java
│   │   │   └── ExpenseSummary.java
│   │   ├── common/
│   │   │   ├── ApiResponse.java
│   │   │   ├── PageResponse.java
│   │   │   └── ErrorResponse.java
│   │   └── ...
│   │
│   ├── entity/                          # Domain/Entity Layer
│   │   ├── User.java
│   │   ├── Expense.java
│   │   ├── ExpenseCategory.java
│   │   ├── Income.java
│   │   ├── IncomeSource.java
│   │   ├── Budget.java
│   │   ├── SavingsGoal.java
│   │   ├── Investment.java
│   │   ├── Notification.java
│   │   ├── AuditLog.java
│   │   └── BaseEntity.java
│   │
│   ├── repository/                      # Persistence Layer
│   │   ├── UserRepository.java
│   │   ├── ExpenseRepository.java
│   │   ├── ExpenseCategoryRepository.java
│   │   ├── IncomeRepository.java
│   │   ├── BudgetRepository.java
│   │   ├── SavingsGoalRepository.java
│   │   ├── InvestmentRepository.java
│   │   ├── NotificationRepository.java
│   │   └── custom/
│   │       ├── ExpenseRepositoryCustom.java
│   │       └── ExpenseRepositoryImpl.java
│   │
│   ├── service/                         # Application/Service Layer
│   │   ├── auth/
│   │   │   ├── AuthService.java
│   │   │   ├── JwtService.java
│   │   │   ├── UserService.java
│   │   │   └── PasswordResetService.java
│   │   ├── expense/
│   │   │   ├── ExpenseService.java
│   │   │   └── ExpenseCategoryService.java
│   │   ├── income/
│   │   │   ├── IncomeService.java
│   │   │   └── IncomeSourceService.java
│   │   ├── budget/
│   │   │   ├── BudgetService.java
│   │   │   └── BudgetAlertService.java
│   │   ├── investment/
│   │   │   ├── InvestmentService.java
│   │   │   └── PortfolioService.java
│   │   ├── notification/
│   │   │   └── NotificationService.java
│   │   ├── analytics/
│   │   │   ├── AnalyticsService.java
│   │   │   └── ReportService.java
│   │   ├── file/
│   │   │   ├── CsvImportService.java
│   │   │   └── ExcelExportService.java
│   │   └── cache/
│   │       └── CacheService.java
│   │
│   ├── mapper/                          # MapStruct Mappers
│   │   ├── UserMapper.java
│   │   ├── ExpenseMapper.java
│   │   ├── IncomeMapper.java
│   │   ├── BudgetMapper.java
│   │   └── ...
│   │
│   ├── exception/                       # Exception Handling
│   │   ├── GlobalExceptionHandler.java
│   │   ├── ApiException.java
│   │   ├── ResourceNotFoundException.java
│   │   ├── InvalidCredentialsException.java
│   │   ├── ValidationException.java
│   │   └── ...
│   │
│   ├── security/                        # Security Layer
│   │   ├── JwtAuthenticationFilter.java
│   │   ├── JwtAuthorizationFilter.java
│   │   ├── CustomUserDetails.java
│   │   ├── CustomUserDetailsService.java
│   │   ├── JwtUtil.java
│   │   ├── PasswordEncoder.java
│   │   └── RoleBasedAccessControl.java
│   │
│   ├── validator/                       # Custom Validators
│   │   ├── EmailValidator.java
│   │   ├── PhoneValidator.java
│   │   ├── DateValidator.java
│   │   └── AmountValidator.java
│   │
│   ├── util/                            # Utility Classes
│   │   ├── DateUtil.java
│   │   ├── CurrencyUtil.java
│   │   ├── NumberFormatter.java
│   │   ├── FileUtil.java
│   │   └── CsvParser.java
│   │
│   ├── batch/                           # Spring Batch
│   │   ├── TransactionImportBatch.java
│   │   ├── NotificationBatch.java
│   │   └── ReportGenerationBatch.java
│   │
│   ├── event/                           # Event Handling
│   │   ├── ExpenseCreatedEvent.java
│   │   ├── BudgetExceededEvent.java
│   │   ├── GoalAchievedEvent.java
│   │   └── EventListener.java
│   │
│   ├── aop/                             # Aspect-Oriented Programming
│   │   ├── LoggingAspect.java
│   │   ├── PerformanceMonitoringAspect.java
│   │   └── ValidationAspect.java
│   │
│   ├── audit/                           # Audit Logging
│   │   ├── AuditLogService.java
│   │   ├── AuditableEntity.java
│   │   └── EntityAuditListener.java
│   │
│   ├── constant/                        # Constants
│   │   ├── AppConstants.java
│   │   ├── ErrorMessages.java
│   │   ├── ValidationMessages.java
│   │   └── CacheKeys.java
│   │
│   └── DailyFinanceTrackerApplication.java
│
├── src/main/resources/
│   ├── application.yml                  # Main configuration
│   ├── application-dev.yml              # Development profile
│   ├── application-prod.yml             # Production profile
│   ├── db/
│   │   └── migration/
│   │       ├── V1__Initial_schema.sql
│   │       ├── V2__Add_audit_tables.sql
│   │       ├── V3__Add_indexes.sql
│   │       └── ...
│   └── logback-spring.xml               # Logging configuration
│
├── src/test/java/com/personalfinance/
│   ├── controller/
│   │   ├── ExpenseControllerTest.java
│   │   └── AuthControllerTest.java
│   ├── service/
│   │   ├── ExpenseServiceTest.java
│   │   └── AuthServiceTest.java
│   ├── repository/
│   │   ├── ExpenseRepositoryTest.java
│   │   └── UserRepositoryTest.java
│   ├── integration/
│   │   ├── ExpenseIntegrationTest.java
│   │   └── AuthIntegrationTest.java
│   └── util/
│       └── TestDataBuilder.java
│
├── pom.xml
├── Dockerfile
└── docker-compose.yml
```

### Frontend (Angular) Structure

```
daily-finance-tracker-frontend/
│
├── src/
│   ├── app/
│   │   ├── core/                        # Core Module (Singleton services)
│   │   │   ├── services/
│   │   │   │   ├── auth.service.ts
│   │   │   │   ├── http-client.service.ts
│   │   │   │   ├── storage.service.ts
│   │   │   │   ├── notification.service.ts
│   │   │   │   └── analytics.service.ts
│   │   │   ├── guards/
│   │   │   │   ├── auth.guard.ts
│   │   │   │   ├── role.guard.ts
│   │   │   │   └── unsaved-changes.guard.ts
│   │   │   ├── interceptors/
│   │   │   │   ├── jwt.interceptor.ts
│   │   │   │   ├── error-handler.interceptor.ts
│   │   │   │   └── loading.interceptor.ts
│   │   │   ├── models/
│   │   │   │   ├── user.model.ts
│   │   │   │   ├── auth.model.ts
│   │   │   │   └── api.model.ts
│   │   │   └── core.module.ts
│   │   │
│   │   ├── shared/                      # Shared Module (Reusable components)
│   │   │   ├── components/
│   │   │   │   ├── header/
│   │   │   │   │   ├── header.component.ts
│   │   │   │   │   ├── header.component.html
│   │   │   │   │   └── header.component.scss
│   │   │   │   ├── sidebar/
│   │   │   │   │   ├── sidebar.component.ts
│   │   │   │   │   ├── sidebar.component.html
│   │   │   │   │   └── sidebar.component.scss
│   │   │   │   ├── footer/
│   │   │   │   ├── loading-spinner/
│   │   │   │   ├── error-message/
│   │   │   │   ├── success-message/
│   │   │   │   └── financial-card/
│   │   │   ├── directives/
│   │   │   │   ├── currency.directive.ts
│   │   │   │   ├── number-format.directive.ts
│   │   │   │   └── permissions.directive.ts
│   │   │   ├── pipes/
│   │   │   │   ├── currency.pipe.ts
│   │   │   │   ├── date-format.pipe.ts
│   │   │   │   └── category-icon.pipe.ts
│   │   │   ├── shared.module.ts
│   │   │   └── material.module.ts               # Material imports
│   │   │
│   │   ├── features/                    # Feature Modules (Lazy loaded)
│   │   │   ├── auth/
│   │   │   │   ├── components/
│   │   │   │   │   ├── login/
│   │   │   │   │   │   ├── login.component.ts
│   │   │   │   │   │   ├── login.component.html
│   │   │   │   │   │   └── login.component.scss
│   │   │   │   │   ├── register/
│   │   │   │   │   └── password-reset/
│   │   │   │   ├── services/
│   │   │   │   │   └── auth-feature.service.ts
│   │   │   │   ├── auth-routing.module.ts
│   │   │   │   └── auth.module.ts
│   │   │   │
│   │   │   ├── dashboard/
│   │   │   │   ├── pages/
│   │   │   │   │   └── dashboard.component.ts
│   │   │   │   ├── components/
│   │   │   │   │   ├── financial-summary/
│   │   │   │   │   ├── expense-chart/
│   │   │   │   │   ├── income-chart/
│   │   │   │   │   ├── savings-progress/
│   │   │   │   │   └── quick-actions/
│   │   │   │   ├── dashboard-routing.module.ts
│   │   │   │   └── dashboard.module.ts
│   │   │   │
│   │   │   ├── expenses/
│   │   │   │   ├── pages/
│   │   │   │   │   └── expenses.component.ts
│   │   │   │   ├── components/
│   │   │   │   │   ├── expense-list/
│   │   │   │   │   ├── expense-form/
│   │   │   │   │   ├── expense-filters/
│   │   │   │   │   ├── category-selector/
│   │   │   │   │   └── recurring-expense/
│   │   │   │   ├── services/
│   │   │   │   │   └── expense.service.ts
│   │   │   │   ├── store/                  # NgRx or Signals state
│   │   │   │   │   ├── expense.actions.ts
│   │   │   │   │   ├── expense.reducer.ts
│   │   │   │   │   ├── expense.selector.ts
│   │   │   │   │   └── expense.effects.ts
│   │   │   │   ├── expenses-routing.module.ts
│   │   │   │   └── expenses.module.ts
│   │   │   │
│   │   │   ├── income/
│   │   │   │   ├── pages/
│   │   │   │   ├── components/
│   │   │   │   ├── services/
│   │   │   │   ├── store/
│   │   │   │   └── income.module.ts
│   │   │   │
│   │   │   ├── budgets/
│   │   │   │   ├── pages/
│   │   │   │   ├── components/
│   │   │   │   ├── services/
│   │   │   │   ├── store/
│   │   │   │   └── budgets.module.ts
│   │   │   │
│   │   │   ├── investments/
│   │   │   │   ├── pages/
│   │   │   │   ├── components/
│   │   │   │   ├── services/
│   │   │   │   ├── store/
│   │   │   │   └── investments.module.ts
│   │   │   │
│   │   │   ├── reports/
│   │   │   │   ├── pages/
│   │   │   │   ├── components/
│   │   │   │   ├── services/
│   │   │   │   └── reports.module.ts
│   │   │   │
│   │   │   ├── settings/
│   │   │   │   ├── pages/
│   │   │   │   ├── components/
│   │   │   │   ├── services/
│   │   │   │   └── settings.module.ts
│   │   │   │
│   │   │   └── analytics/
│   │   │       ├── pages/
│   │   │       ├── components/
│   │   │       │   ├── expense-trend-chart/
│   │   │       │   ├── category-breakdown/
│   │   │       │   ├── income-vs-expense/
│   │   │       │   └── portfolio-distribution/
│   │   │       ├── services/
│   │   │       └── analytics.module.ts
│   │   │
│   │   ├── app.routes.ts                # Main routing module
│   │   ├── app.component.ts
│   │   ├── app.config.ts
│   │   └── app.module.ts
│   │
│   ├── assets/
│   │   ├── icons/
│   │   ├── images/
│   │   ├── logos/
│   │   └── data/
│   │
│   ├── environments/
│   │   ├── environment.ts
│   │   ├── environment.prod.ts
│   │   └── environment.staging.ts
│   │
│   ├── styles/
│   │   ├── styles.scss
│   │   ├── variables.scss
│   │   ├── mixins.scss
│   │   ├── themes/
│   │   │   ├── light-theme.scss
│   │   │   └── dark-theme.scss
│   │   └── components/
│   │       └── common.scss
│   │
│   ├── main.ts
│   ├── index.html
│   └── favicon.ico
│
├── angular.json
├── tsconfig.json
├── tsconfig.app.json
├── tsconfig.spec.json
├── package.json
├── .env
├── .env.prod
├── Dockerfile
├── .dockerignore
└── nginx.conf
```

---

## Design Patterns

### 1. Repository Pattern
**Purpose:** Abstraction over data access logic

```java
// Interface
public interface ExpenseRepository extends JpaRepository<Expense, Long> {
    List<Expense> findByUserIdAndDateBetween(Long userId, LocalDate start, LocalDate end);
}

// Usage in Service
@Service
public class ExpenseService {
    @Autowired
    private ExpenseRepository repository;
    
    public List<Expense> getMonthlyExpenses(Long userId, Month month) {
        return repository.findByUserIdAndMonthYear(userId, month);
    }
}
```

### 2. Service/Business Logic Pattern
**Purpose:** Encapsulate business logic, separate from infrastructure

```java
@Service
public class BudgetService {
    @Autowired
    private BudgetRepository budgetRepository;
    
    @Autowired
    private NotificationService notificationService;
    
    @Transactional
    public void checkBudgetExceeded(Long budgetId, BigDecimal newAmount) {
        Budget budget = budgetRepository.findById(budgetId).orElseThrow();
        
        if (newAmount > budget.getLimit()) {
            notificationService.sendBudgetAlert(budget);
        }
    }
}
```

### 3. DTO (Data Transfer Object) Pattern
**Purpose:** Decouple API contracts from internal entity structure

```java
// Request DTO
@Data
@Valid
public class ExpenseRequest {
    @NotNull
    private String description;
    
    @NotNull
    @Min(0)
    private BigDecimal amount;
    
    @NotNull
    private Long categoryId;
    
    private LocalDate date;
}

// Response DTO
@Data
public class ExpenseResponse {
    private Long id;
    private String description;
    private BigDecimal amount;
    private CategoryResponse category;
    private LocalDateTime createdAt;
}
```

### 4. Mapper Pattern (MapStruct)
**Purpose:** Convert between entities and DTOs

```java
@Mapper(componentModel = "spring")
public interface ExpenseMapper {
    ExpenseResponse toResponse(Expense entity);
    List<ExpenseResponse> toResponseList(List<Expense> entities);
    
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "user", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    Expense toEntity(ExpenseRequest request);
}
```

### 5. Singleton Pattern (Services)
**Purpose:** Ensure only one instance of service classes

```java
@Service
public class JwtService {
    // Spring automatically manages singleton instance
    public String generateToken(UserDetails userDetails) { }
    public String extractUsername(String token) { }
}
```

### 6. Strategy Pattern (Different expense categories)
**Purpose:** Encapsulate algorithms that can vary

```java
public interface ExpenseCategoryStrategy {
    void validate(Expense expense);
    void categorize(Expense expense);
}

@Component
public class FoodExpenseStrategy implements ExpenseCategoryStrategy {
    @Override
    public void validate(Expense expense) { }
    
    @Override
    public void categorize(Expense expense) { }
}
```

### 7. Observer Pattern (Event Publishing)
**Purpose:** Decouple components through events

```java
@Service
public class ExpenseService {
    @Autowired
    private ApplicationEventPublisher eventPublisher;
    
    @Transactional
    public Expense createExpense(ExpenseRequest request) {
        Expense expense = new Expense(request);
        expenseRepository.save(expense);
        
        // Publish event
        eventPublisher.publishEvent(new ExpenseCreatedEvent(expense));
        return expense;
    }
}

@Component
public class BudgetAlertListener {
    @EventListener
    public void onExpenseCreated(ExpenseCreatedEvent event) {
        // Handle event
    }
}
```

### 8. Factory Pattern (Different report types)
**Purpose:** Create objects without specifying exact classes

```java
@Component
public class ReportFactory {
    public Report createReport(ReportType type) {
        switch(type) {
            case MONTHLY:
                return new MonthlyReport();
            case YEARLY:
                return new YearlyReport();
            default:
                throw new InvalidReportTypeException();
        }
    }
}
```

---

## Technology Stack Details

### Backend Technologies

| Component | Technology | Version | Purpose |
|-----------|-----------|---------|---------|
| Language | Java | 21 | Latest LTS version with modern features |
| Framework | Spring Boot | 3.2+ | Enterprise application framework |
| Security | Spring Security | 6.1+ | Authentication & Authorization |
| ORM | Spring Data JPA | Latest | Data persistence |
| Database | PostgreSQL | 15+ | Relational database |
| Cache | Redis | 7.0+ | In-memory cache |
| Mapping | MapStruct | 1.5+ | Entity to DTO mapping |
| Validation | Jakarta Bean Validation | Latest | Input validation |
| API Docs | Springdoc OpenAPI | Latest | Swagger/OpenAPI documentation |
| Testing | JUnit5, Mockito | Latest | Unit testing |
| Build | Maven | 3.9+ | Dependency management |
| Container | Docker | Latest | Containerization |

### Frontend Technologies

| Component | Technology | Version | Purpose |
|-----------|-----------|---------|---------|
| Framework | Angular | 17+ | Frontend framework |
| Language | TypeScript | 5.2+ | Type-safe JavaScript |
| UI Framework | Angular Material / PrimeNG | Latest | Component library |
| State Mgmt | NgRx / Signals | Latest | State management |
| HTTP | RxJS | 7.8+ | Reactive programming |
| Charts | Chart.js + Ng2-Charts | Latest | Data visualization |
| Forms | Reactive Forms | Latest | Form handling |
| Build | Angular CLI | Latest | Development tools |
| Package Mgr | npm/yarn | Latest | Dependency management |
| Container | Docker | Latest | Containerization |

### DevOps Stack

| Component | Technology | Purpose |
|-----------|-----------|---------|
| Containerization | Docker | Container images |
| Orchestration | Docker Compose | Multi-container setup |
| CI/CD | GitHub Actions | Automated workflows |
| Reverse Proxy | Nginx | Load balancing |
| Cloud Ready | AWS | Cloud deployment ready |

---

## Cross-Cutting Concerns

### 1. Exception Handling (Global)
- Centralized exception handler
- Consistent error responses
- Proper HTTP status codes

### 2. Logging and Monitoring
- AOP-based logging
- Performance monitoring
- Error tracking (Sentry/ELK)

### 3. Validation
- Input validation at controller level
- Business logic validation at service level
- Custom validators

### 4. Security
- JWT-based authentication
- Role-based access control
- Password hashing with BCrypt
- CORS configuration

### 5. Caching Strategy
- Controller-level caching
- Service-level caching
- Repository-level caching
- Cache invalidation strategy

### 6. Auditing
- Track who made changes
- Track when changes were made
- Track what was changed
- Audit logs in database

---

## Data Flow Example: Creating an Expense

```
1. Client (Angular)
   ├─ User fills expense form
   ├─ Form validation (client-side)
   └─ POST /api/v1/expenses with ExpenseRequest

2. API Gateway / Load Balancer
   └─ Routes request to backend

3. Spring Boot Backend
   ├─ ExpenseController receives request
   │  ├─ Validates request (@Valid)
   │  └─ Calls ExpenseService.createExpense()
   │
   ├─ ExpenseService (Business Logic)
   │  ├─ Additional validation
   │  ├─ Check budget not exceeded
   │  ├─ Create Expense entity
   │  └─ Calls ExpenseRepository.save()
   │
   ├─ ExpenseRepository (Data Access)
   │  └─ Saves to PostgreSQL via Hibernate/JPA
   │
   ├─ Database Layer
   │  ├─ Insert into expense table
   │  ├─ Update expense_category foreign key
   │  └─ Return saved entity
   │
   ├─ ExpenseMapper converts entity to DTO
   ├─ Controller returns ExpenseResponse
   └─ Response sent via API Gateway

4. Client (Angular)
   ├─ Receives response
   ├─ Updates local state (NgRx/Signals)
   ├─ Updates UI
   └─ Shows success message
```

---

## API Response Pattern

```json
{
  "success": true,
  "statusCode": 200,
  "message": "Expense created successfully",
  "data": {
    "id": 1,
    "description": "Grocery shopping",
    "amount": 45.99,
    "category": {
      "id": 1,
      "name": "Food & Dining"
    },
    "date": "2026-03-07",
    "createdAt": "2026-03-07T10:30:00Z"
  },
  "timestamp": "2026-03-07T10:30:00Z"
}
```

---

## Security Architecture

### Authentication Flow
```
1. User enters credentials
2. Backend validates credentials
3. If valid: Generate JWT token
4. Return token to client
5. Client stores token (localStorage/sessionStorage)
6. Client sends token in Authorization header
7. Backend validates token signature and expiry
8. Grant access to protected resources
```

### Token Structure
```
Header.Payload.Signature

Header:
{
  "alg": "HS256",
  "typ": "JWT"
}

Payload:
{
  "sub": "user_id",
  "username": "ashutosh",
  "email": "ashutosh@example.com",
  "roles": ["USER"],
  "iat": 1609459200,
  "exp": 1609545600
}

Signature: HMACSHA256(header.payload, secret)
```

---

## Caching Strategy

### Cache Levels
1. **Controller-level cache** (@Cacheable)
   - Cache GET requests
   - TTL: 15 minutes

2. **Service-level cache**
   - Cache expensive computations
   - Cache reference data (categories, sources)
   - TTL: 1 hour

3. **Repository-level cache**
   - Query result caching
   - TTL: 30 minutes

### Cache Invalidation
- **Time-based:** TTL expiration
- **Event-based:** Invalidate on CREATE/UPDATE/DELETE
- **Manual:** Cache.clear()

---

## Performance Optimization Strategies

1. **Database Optimization**
   - Proper indexing on frequently queried columns
   - Query optimization and pagination
   - Connection pooling (HikariCP)

2. **API Optimization**
   - Response compression (gzip)
   - Pagination for large datasets
   - Lazy loading in Angular

3. **Frontend Optimization**
   - Tree shaking
   - Code splitting
   - Change detection optimization
   - Virtual scrolling for large lists

4. **Caching Strategy**
   - Redis for distributed caching
   - Browser caching for static assets
   - API response caching

---

## Scalability Considerations

### Horizontal Scaling (Multiple instances)
- Stateless backend services
- Distributed session management (Redis)
- Load balancer (Nginx/AWS ALB)

### Vertical Scaling
- Database optimization
- JVM tuning
- Connection pooling

### Database Scaling
- Read replicas for read-heavy operations
- Partitioning large tables
- Archive old data

### Asynchronous Processing
- Spring Batch for bulk operations
- Message queues for async tasks
- Event-driven architecture

---

## Future Architecture Enhancements

### 1. Microservices Migration
- Break monolith into independent services
- Service mesh (Istio/Linkerd)
- API Gateway pattern

### 2. Event-Driven Architecture
- Event bus (Kafka/RabbitMQ)
- Event sourcing
- CQRS pattern

### 3. GraphQL API
- GraphQL gateway
- Subscription support

### 4. Real-time Features
- WebSockets
- Server-Sent Events (SSE)
- Real-time notifications

### 5. Machine Learning Integration
- Spending pattern analysis
- Fraud detection
- Automated categorization
- Investment recommendations

---

## Best Practices Summary

✅ **Backend Best Practices:**
- Clean code principles
- SOLID principles
- Design patterns
- Proper exception handling
- Input validation
- Security best practices
- Comprehensive testing

✅ **Frontend Best Practices:**
- Component reusability
- Smart/dumb components pattern
- Reactive programming
- Lazy loading
- Performance optimization
- Accessibility (a11y)
- Responsive design

✅ **DevOps Best Practices:**
- Infrastructure as Code
- Containerization
- Automated testing
- Continuous Integration/Deployment
- Monitoring and logging
- Blue-green deployments

---

## Conclusion

This architecture provides:
- ✅ Scalable monolith structure
- ✅ Future-ready for microservices
- ✅ Enterprise-grade security
- ✅ High performance
- ✅ Maintainable codebase
- ✅ Production-ready setup
- ✅ Complete documentation

The modular approach allows teams to work independently on different features while maintaining clean separation of concerns.

