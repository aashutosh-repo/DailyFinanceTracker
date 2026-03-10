# API Documentation - Daily Personal Finance Tracker

## Overview

RESTful API for managing personal finances with complete CRUD operations, advanced filtering, and analytics.

**Base URL:** `https://api.yourfinancetracker.com/api/v1`

**Authentication:** JWT Bearer Token

---

## Authentication Endpoints

### POST /auth/register
**Description:** Register a new user

**Request:**
```json
{
  "username": "john_doe",
  "email": "john@example.com",
  "fullName": "John Doe",
  "password": "SecurePassword123!",
  "passwordConfirm": "SecurePassword123!",
  "phone": "+1234567890",
  "currency": "USD"
}
```

**Response (201):**
```json
{
  "success": true,
  "statusCode": 201,
  "message": "User registered successfully",
  "data": {
    "userId": 1,
    "username": "john_doe",
    "email": "john@example.com",
    "fullName": "John Doe",
    "accessToken": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...",
    "refreshToken": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...",
    "tokenType": "Bearer",
    "expiresIn": 86400000,
    "roles": ["USER"]
  }
}
```

---

### POST /auth/login
**Description:** Authenticate user and get JWT token

**Request:**
```json
{
  "usernameOrEmail": "john_doe",
  "password": "SecurePassword123!",
  "rememberMe": true
}
```

**Response (200):**
```json
{
  "success": true,
  "statusCode": 200,
  "message": "Login successful",
  "data": {
    "userId": 1,
    "username": "john_doe",
    "email": "john@example.com",
    "fullName": "John Doe",
    "accessToken": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...",
    "refreshToken": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...",
    "tokenType": "Bearer",
    "expiresIn": 86400000,
    "roles": ["USER"]
  }
}
```

---

### POST /auth/refresh-token
**Description:** Refresh access token using refresh token

**Request:**
```json
{
  "refreshToken": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9..."
}
```

**Response (200):**
```json
{
  "success": true,
  "statusCode": 200,
  "message": "Token refreshed successfully",
  "data": {
    "accessToken": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...",
    "tokenType": "Bearer",
    "expiresIn": 86400000
  }
}
```

---

### POST /auth/logout
**Description:** Logout user (revoke tokens)

**Headers:**
```
Authorization: Bearer {accessToken}
```

**Response (200):**
```json
{
  "success": true,
  "statusCode": 200,
  "message": "Logged out successfully"
}
```

---

### POST /auth/password-reset
**Description:** Request password reset email

**Request:**
```json
{
  "email": "john@example.com"
}
```

**Response (200):**
```json
{
  "success": true,
  "statusCode": 200,
  "message": "Password reset email sent"
}
```

---

## Expense Endpoints

### GET /expenses
**Description:** Get user's expenses with pagination, filtering, and sorting

**Query Parameters:**
```
?page=0&size=10&sort=expenseDate,desc
&startDate=2026-01-01&endDate=2026-03-31
&categoryId=1&paymentMethod=CARD
&minAmount=10&maxAmount=100
&tags=food,grocery
```

**Response (200):**
```json
{
  "success": true,
  "statusCode": 200,
  "message": "Expenses retrieved successfully",
  "data": {
    "content": [
      {
        "id": 1,
        "description": "Grocery shopping",
        "amount": 45.99,
        "currency": "USD",
        "expenseDate": "2026-03-07",
        "paymentMethod": "CARD",
        "category": {
          "id": 1,
          "name": "Food & Dining",
          "colorCode": "#FF6B6B"
        },
        "tags": ["grocery", "food"],
        "createdAt": "2026-03-07T10:30:00Z",
        "updatedAt": "2026-03-07T10:30:00Z"
      }
    ],
    "pageNumber": 0,
    "pageSize": 10,
    "totalPages": 1,
    "totalElements": 1,
    "lastPage": true,
    "firstPage": true
  }
}
```

---

### POST /expenses
**Description:** Create a new expense

**Headers:**
```
Authorization: Bearer {accessToken}
Content-Type: application/json
```

**Request:**
```json
{
  "description": "Grocery shopping",
  "amount": 45.99,
  "categoryId": 1,
  "expenseDate": "2026-03-07",
  "currency": "USD",
  "paymentMethod": "CARD",
  "referenceNumber": "TXN123456",
  "notes": "Weekly groceries",
  "receiptUrl": "https://example.com/receipt.pdf",
  "isRecurring": false,
  "tags": ["grocery", "food"]
}
```

**Response (201):**
```json
{
  "success": true,
  "statusCode": 201,
  "message": "Expense created successfully",
  "data": {
    "id": 1,
    "description": "Grocery shopping",
    "amount": 45.99,
    "currency": "USD",
    "expenseDate": "2026-03-07",
    "paymentMethod": "CARD",
    "category": {
      "id": 1,
      "name": "Food & Dining"
    },
    "tags": ["grocery", "food"],
    "createdAt": "2026-03-07T10:30:00Z"
  }
}
```

---

### PUT /expenses/{id}
**Description:** Update an expense

**Response (200):** Updated expense object

---

### DELETE /expenses/{id}
**Description:** Delete an expense (soft delete)

**Response (200):**
```json
{
  "success": true,
  "statusCode": 200,
  "message": "Expense deleted successfully"
}
```

---

### GET /expenses/{id}
**Description:** Get single expense details

**Response (200):** Single expense object

---

## Income Endpoints

### GET /income
**Description:** Get user's income entries

**Query Parameters:**
```
?page=0&size=10&sort=incomeDate,desc
&startDate=2026-01-01&endDate=2026-03-31
&sourceId=1
```

**Response (200):** Paginated income list

---

### POST /income
**Description:** Create income entry

**Request:**
```json
{
  "description": "Monthly salary",
  "amount": 5000,
  "sourceId": 1,
  "incomeDate": "2026-03-01",
  "currency": "USD",
  "referenceNumber": "SALARY-2026-03",
  "notes": "March salary",
  "isRecurring": true
}
```

**Response (201):** Created income object

---

### PUT /income/{id}
**Description:** Update income entry

---

### DELETE /income/{id}
**Description:** Delete income entry

---

## Budget Endpoints

### GET /budgets
**Description:** Get user's budgets

**Query Parameters:**
```
?page=0&size=10&isActive=true&period=MONTHLY
```

**Response (200):**
```json
{
  "success": true,
  "statusCode": 200,
  "message": "Budgets retrieved successfully",
  "data": {
    "content": [
      {
        "id": 1,
        "name": "Food Budget",
        "amount": 500,
        "period": "MONTHLY",
        "startDate": "2026-03-01",
        "endDate": "2026-03-31",
        "currency": "USD",
        "alertThreshold": 80,
        "isActive": true,
        "currentSpending": 150.50,
        "budgetStatus": "SAFE",
        "percentageUsed": 30.10,
        "category": {
          "id": 1,
          "name": "Food & Dining"
        }
      }
    ],
    "pageNumber": 0,
    "pageSize": 10,
    "totalPages": 1,
    "totalElements": 1
  }
}
```

---

### POST /budgets
**Description:** Create a budget

**Request:**
```json
{
  "name": "Food Budget",
  "amount": 500,
  "period": "MONTHLY",
  "startDate": "2026-03-01",
  "endDate": "2026-03-31",
  "categoryId": 1,
  "currency": "USD",
  "alertThreshold": 80,
  "alertFrequency": "WEEKLY"
}
```

**Response (201):** Created budget object

---

### PUT /budgets/{id}
**Description:** Update budget

---

### DELETE /budgets/{id}
**Description:** Delete budget

---

## Investment Endpoints

### GET /investments
**Description:** Get user's investments

**Query Parameters:**
```
?page=0&size=10&status=ACTIVE&investmentType=STOCKS
```

**Response (200):**
```json
{
  "success": true,
  "statusCode": 200,
  "data": {
    "content": [
      {
        "id": 1,
        "name": "Apple Inc.",
        "tickerSymbol": "AAPL",
        "investmentType": "STOCKS",
        "quantity": 10,
        "buyPrice": 150.00,
        "currentPrice": 180.00,
        "currency": "USD",
        "buyDate": "2025-01-15",
        "status": "ACTIVE",
        "brokerName": "E-Trade",
        "currentValue": 1800.00,
        "gainLoss": 300.00,
        "gainLossPercentage": 20.00
      }
    ],
    "pageNumber": 0,
    "pageSize": 10,
    "totalElements": 1
  }
}
```

---

### POST /investments
**Description:** Create investment

**Request:**
```json
{
  "name": "Apple Inc.",
  "tickerSymbol": "AAPL",
  "investmentType": "STOCKS",
  "quantity": 10,
  "buyPrice": 150.00,
  "currentPrice": 180.00,
  "currency": "USD",
  "buyDate": "2025-01-15",
  "brokerName": "E-Trade",
  "notes": "Growth investment"
}
```

**Response (201):** Created investment object

---

### PUT /investments/{id}
**Description:** Update investment (price updates)

---

### DELETE /investments/{id}
**Description:** Sell/Delete investment

---

## Savings Goals Endpoints

### GET /savings-goals
**Description:** Get user's savings goals

**Query Parameters:**
```
?page=0&size=10&status=ACTIVE&priority=HIGH
```

**Response (200):**
```json
{
  "success": true,
  "statusCode": 200,
  "data": {
    "content": [
      {
        "id": 1,
        "goalName": "Buy a Car",
        "description": "Save for a new vehicle",
        "targetAmount": 25000,
        "currentAmount": 5000,
        "currency": "USD",
        "targetDate": "2027-12-31",
        "priority": "HIGH",
        "status": "ACTIVE",
        "progressPercentage": 20.00,
        "remainingAmount": 20000
      }
    ],
    "pageNumber": 0,
    "pageSize": 10,
    "totalElements": 1
  }
}
```

---

### POST /savings-goals
**Description:** Create savings goal

**Request:**
```json
{
  "goalName": "Buy a Car",
  "description": "Save for a new vehicle",
  "targetAmount": 25000,
  "currency": "USD",
  "targetDate": "2027-12-31",
  "priority": "HIGH"
}
```

**Response (201):** Created goal object

---

### POST /savings-goals/{id}/deposit
**Description:** Add money to savings goal

**Request:**
```json
{
  "amount": 500,
  "description": "Monthly savings"
}
```

**Response (200):** Updated goal object

---

## Analytics Endpoints

### GET /analytics/dashboard
**Description:** Get dashboard analytics overview

**Query Parameters:**
```
?startDate=2026-01-01&endDate=2026-03-31
```

**Response (200):**
```json
{
  "success": true,
  "statusCode": 200,
  "data": {
    "totalExpenses": 2500.50,
    "totalIncome": 5000.00,
    "totalSavings": 2499.50,
    "savingsRate": 49.99,
    "averageDailyExpense": 28.42,
    "largestExpense": {
      "id": 5,
      "description": "Rent",
      "amount": 1200,
      "category": "Housing"
    },
    "expensesByCategory": [
      {
        "category": "Food & Dining",
        "amount": 500,
        "percentage": 20.0
      },
      {
        "category": "Housing",
        "amount": 1200,
        "percentage": 48.0
      }
    ],
    "incomeBySource": [
      {
        "source": "Salary",
        "amount": 5000
      }
    ],
    "monthlyTrend": [
      {
        "month": "2026-01",
        "income": 5000,
        "expenses": 2500,
        "savings": 2500
      }
    ]
  }
}
```

---

### GET /analytics/expense-trends
**Description:** Get expense trend data for charts

**Query Parameters:**
```
?period=MONTHLY&months=12&groupBy=CATEGORY
```

**Response (200):** Trend data array

---

### GET /analytics/reports/{type}
**Description:** Generate and download report

**Path Parameters:**
```
type: MONTHLY | YEARLY | CUSTOM
```

**Query Parameters:**
```
?startDate=2026-01-01&endDate=2026-03-31&format=PDF
```

**Response (200):** PDF/Excel file download

---

## Categories Endpoints

### GET /categories/expenses
**Description:** Get all expense categories

**Response (200):**
```json
{
  "success": true,
  "statusCode": 200,
  "data": [
    {
      "id": 1,
      "name": "Food & Dining",
      "description": "Groceries and restaurants",
      "iconUrl": "...",
      "colorCode": "#FF6B6B",
      "isDefault": true,
      "monthlyBudget": 500
    }
  ]
}
```

---

### POST /categories/expenses
**Description:** Create custom expense category

**Request:**
```json
{
  "name": "Entertainment",
  "description": "Movies, games, etc.",
  "colorCode": "#4ECDC4",
  "monthlyBudget": 100
}
```

---

### GET /categories/income
**Description:** Get income sources

---

### POST /categories/income
**Description:** Create income source

---

## Notifications Endpoints

### GET /notifications
**Description:** Get user notifications

**Query Parameters:**
```
?page=0&size=10&isRead=false
```

**Response (200):**
```json
{
  "success": true,
  "statusCode": 200,
  "data": {
    "content": [
      {
        "id": 1,
        "title": "Budget Alert",
        "message": "You've reached 85% of your Food budget",
        "notificationType": "BUDGET_ALERT",
        "relatedEntityId": 1,
        "isRead": false,
        "createdAt": "2026-03-07T10:30:00Z"
      }
    ],
    "pageNumber": 0,
    "pageSize": 10,
    "totalElements": 1
  }
}
```

---

### PUT /notifications/{id}/read
**Description:** Mark notification as read

**Response (200):** Updated notification

---

### PUT /notifications/read-all
**Description:** Mark all notifications as read

**Response (200):**
```json
{
  "success": true,
  "statusCode": 200,
  "message": "All notifications marked as read"
}
```

---

## Error Responses

### 400 - Bad Request
```json
{
  "success": false,
  "statusCode": 400,
  "message": "Validation error",
  "errors": [
    {
      "field": "amount",
      "message": "Amount must be greater than 0"
    }
  ]
}
```

### 401 - Unauthorized
```json
{
  "success": false,
  "statusCode": 401,
  "message": "Unauthorized - Invalid or missing token"
}
```

### 403 - Forbidden
```json
{
  "success": false,
  "statusCode": 403,
  "message": "Forbidden - Insufficient permissions"
}
```

### 404 - Not Found
```json
{
  "success": false,
  "statusCode": 404,
  "message": "Resource not found"
}
```

### 500 - Internal Server Error
```json
{
  "success": false,
  "statusCode": 500,
  "message": "Internal server error"
}
```

---

## Rate Limiting

All endpoints are rate limited to prevent abuse:

**Headers in response:**
```
X-RateLimit-Limit: 1000
X-RateLimit-Remaining: 999
X-RateLimit-Reset: 1646686800
```

**Limits:**
- Unauthenticated: 100 requests/hour
- Authenticated: 1000 requests/hour
- Admin: 10000 requests/hour

---

## Pagination

All list endpoints support pagination:

**Query Parameters:**
```
page: 0-indexed page number (default: 0)
size: records per page (default: 10, max: 100)
sort: field,direction (e.g., createdAt,desc)
```

---

## Filtering

Common filter parameters:
```
startDate: Filter by start date (ISO format)
endDate: Filter by end date (ISO format)
status: Filter by status
category: Filter by category
search: Full-text search
```

---

## Sorting

All list endpoints support sorting:

**Common sort fields:**
```
createdAt, updatedAt, amount, date, name, status
Direction: asc | desc
```

