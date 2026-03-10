# PostgreSQL Database Schema Design

## Overview

This document provides the complete PostgreSQL schema for the Daily Personal Finance Tracker application.

## Database Design Principles

1. **Normalization:** 3NF (Third Normal Form)
2. **Constraints:** Foreign keys, unique constraints, check constraints
3. **Indexes:** On frequently queried and foreign key columns
4. **Audit Fields:** created_at, updated_at, created_by, updated_by
5. **Soft Delete:** deleted_at for logical deletion
6. **Partitioning:** Ready for partitioning on large tables

---

## Entity Relationship Diagram (ERD)

```
┌─────────────────┐
│     USERS       │
├─────────────────┤
│ id (PK)         │
│ username (UK)   │
│ email (UK)      │
│ password_hash   │
│ full_name       │
│ phone           │
│ country_code    │
│ currency        │
│ profile_pic_url │
│ is_active       │
│ created_at      │
│ updated_at      │
└────────┬────────┘
         │
         │ (1:N)
    ┌────┴──────────────┬──────────────────┬──────────────┐
    │                   │                  │              │
    ▼                   ▼                  ▼              ▼
┌──────────────┐  ┌──────────────┐  ┌──────────────┐  ┌─────────────┐
│  EXPENSES    │  │    INCOME    │  │   BUDGETS    │  │ INVESTMENTS │
└──────────────┘  └──────────────┘  └──────────────┘  └─────────────┘
    │                   │
    │ (N:1)             │ (N:1)
    ▼                   ▼
┌─────────────────┐ ┌─────────────────┐
│ EXPENSE_CTGY    │ │ INCOME_SOURCES  │
└─────────────────┘ └─────────────────┘

┌──────────────────────────────────────────────────────┐
│          Supporting Tables                            │
├──────────────────────────────────────────────────────┤
│ EXPENSE_TAGS │ RECURRING_EXPENSES │ SAVINGS_GOALS   │
│ TRANSACTIONS │ NOTIFICATIONS      │ AUDIT_LOGS      │
└──────────────────────────────────────────────────────┘
```

---

## Table Definitions

### 1. USERS Table

```sql
CREATE TABLE users (
    id BIGSERIAL PRIMARY KEY,
    username VARCHAR(100) NOT NULL UNIQUE,
    email VARCHAR(255) NOT NULL UNIQUE,
    password_hash VARCHAR(255) NOT NULL,
    full_name VARCHAR(255) NOT NULL,
    phone VARCHAR(20),
    country_code VARCHAR(3) DEFAULT 'USD',
    currency VARCHAR(3) DEFAULT 'USD',
    profile_pic_url TEXT,
    bio TEXT,
    date_of_birth DATE,
    is_active BOOLEAN DEFAULT TRUE,
    last_login TIMESTAMP,
    email_verified BOOLEAN DEFAULT FALSE,
    phone_verified BOOLEAN DEFAULT FALSE,
    two_factor_enabled BOOLEAN DEFAULT FALSE,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    created_by VARCHAR(100),
    updated_by VARCHAR(100),
    deleted_at TIMESTAMP,
    
    CONSTRAINT user_email_valid CHECK (email ~ '^[A-Za-z0-9._%+-]+@[A-Za-z0-9.-]+\.[A-Z|a-z]{2,}$')
);

CREATE INDEX idx_users_email ON users(email);
CREATE INDEX idx_users_username ON users(username);
CREATE INDEX idx_users_is_active ON users(is_active);
CREATE INDEX idx_users_created_at ON users(created_at);
CREATE INDEX idx_users_deleted_at ON users(deleted_at) WHERE deleted_at IS NULL;
```

### 2. ROLES Table

```sql
CREATE TABLE roles (
    id SERIAL PRIMARY KEY,
    name VARCHAR(50) NOT NULL UNIQUE,
    description TEXT,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    
    CONSTRAINT role_name_check CHECK (name IN ('USER', 'ADMIN', 'MODERATOR'))
);

INSERT INTO roles (name, description) VALUES 
    ('USER', 'Regular user with standard permissions'),
    ('ADMIN', 'Administrator with full access');
```

### 3. USER_ROLES Table

```sql
CREATE TABLE user_roles (
    user_id BIGINT NOT NULL,
    role_id INT NOT NULL,
    assigned_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    assigned_by VARCHAR(100),
    
    PRIMARY KEY (user_id, role_id),
    FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE,
    FOREIGN KEY (role_id) REFERENCES roles(id) ON DELETE CASCADE
);

CREATE INDEX idx_user_roles_user_id ON user_roles(user_id);
CREATE INDEX idx_user_roles_role_id ON user_roles(role_id);
```

### 4. EXPENSE_CATEGORIES Table

```sql
CREATE TABLE expense_categories (
    id BIGSERIAL PRIMARY KEY,
    user_id BIGINT NOT NULL,
    name VARCHAR(100) NOT NULL,
    description TEXT,
    icon_url TEXT,
    color_code VARCHAR(7),
    is_default BOOLEAN DEFAULT FALSE,
    monthly_budget DECIMAL(15, 2),
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    deleted_at TIMESTAMP,
    
    FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE,
    UNIQUE(user_id, name),
    CONSTRAINT amount_positive CHECK (monthly_budget > 0 OR monthly_budget IS NULL)
);

CREATE INDEX idx_expense_categories_user_id ON expense_categories(user_id);
CREATE INDEX idx_expense_categories_deleted_at ON expense_categories(deleted_at) WHERE deleted_at IS NULL;
```

### 5. EXPENSES Table

```sql
CREATE TABLE expenses (
    id BIGSERIAL PRIMARY KEY,
    user_id BIGINT NOT NULL,
    category_id BIGINT NOT NULL,
    description VARCHAR(500),
    amount DECIMAL(15, 2) NOT NULL,
    currency VARCHAR(3) NOT NULL DEFAULT 'USD',
    expense_date DATE NOT NULL,
    payment_method VARCHAR(50), -- CASH, CARD, BANK_TRANSFER, etc.
    reference_number VARCHAR(100),
    notes TEXT,
    receipt_url TEXT,
    is_recurring BOOLEAN DEFAULT FALSE,
    recurring_expense_id BIGINT,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    created_by VARCHAR(100),
    updated_by VARCHAR(100),
    deleted_at TIMESTAMP,
    
    FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE,
    FOREIGN KEY (category_id) REFERENCES expense_categories(id) ON DELETE RESTRICT,
    CONSTRAINT amount_positive CHECK (amount > 0),
    CONSTRAINT valid_payment_method CHECK (payment_method IN ('CASH', 'CARD', 'BANK_TRANSFER', 'UPI', 'OTHER'))
);

CREATE INDEX idx_expenses_user_id ON expenses(user_id);
CREATE INDEX idx_expenses_category_id ON expenses(category_id);
CREATE INDEX idx_expenses_expense_date ON expenses(expense_date);
CREATE INDEX idx_expenses_user_date ON expenses(user_id, expense_date);
CREATE INDEX idx_expenses_created_at ON expenses(created_at);
CREATE INDEX idx_expenses_deleted_at ON expenses(deleted_at) WHERE deleted_at IS NULL;
```

### 6. EXPENSE_TAGS Table

```sql
CREATE TABLE expense_tags (
    id BIGSERIAL PRIMARY KEY,
    expense_id BIGINT NOT NULL,
    tag_name VARCHAR(100) NOT NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    
    FOREIGN KEY (expense_id) REFERENCES expenses(id) ON DELETE CASCADE,
    UNIQUE(expense_id, tag_name)
);

CREATE INDEX idx_expense_tags_expense_id ON expense_tags(expense_id);
CREATE INDEX idx_expense_tags_tag_name ON expense_tags(tag_name);
```

### 7. RECURRING_EXPENSES Table

```sql
CREATE TABLE recurring_expenses (
    id BIGSERIAL PRIMARY KEY,
    user_id BIGINT NOT NULL,
    category_id BIGINT NOT NULL,
    description VARCHAR(500) NOT NULL,
    amount DECIMAL(15, 2) NOT NULL,
    currency VARCHAR(3) NOT NULL DEFAULT 'USD',
    frequency VARCHAR(50) NOT NULL, -- DAILY, WEEKLY, MONTHLY, QUARTERLY, YEARLY
    start_date DATE NOT NULL,
    end_date DATE,
    next_occurrence_date DATE NOT NULL,
    is_active BOOLEAN DEFAULT TRUE,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    deleted_at TIMESTAMP,
    
    FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE,
    FOREIGN KEY (category_id) REFERENCES expense_categories(id) ON DELETE RESTRICT,
    CONSTRAINT amount_positive CHECK (amount > 0),
    CONSTRAINT valid_frequency CHECK (frequency IN ('DAILY', 'WEEKLY', 'BIWEEKLY', 'MONTHLY', 'QUARTERLY', 'YEARLY')),
    CONSTRAINT dates_logical CHECK (end_date IS NULL OR end_date >= start_date)
);

CREATE INDEX idx_recurring_expenses_user_id ON recurring_expenses(user_id);
CREATE INDEX idx_recurring_expenses_next_date ON recurring_expenses(next_occurrence_date);
CREATE INDEX idx_recurring_expenses_active ON recurring_expenses(is_active);
```

### 8. INCOME_SOURCES Table

```sql
CREATE TABLE income_sources (
    id BIGSERIAL PRIMARY KEY,
    user_id BIGINT NOT NULL,
    name VARCHAR(100) NOT NULL,
    description TEXT,
    icon_url TEXT,
    color_code VARCHAR(7),
    is_default BOOLEAN DEFAULT FALSE,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    deleted_at TIMESTAMP,
    
    FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE,
    UNIQUE(user_id, name)
);

CREATE INDEX idx_income_sources_user_id ON income_sources(user_id);
CREATE INDEX idx_income_sources_deleted_at ON income_sources(deleted_at) WHERE deleted_at IS NULL;
```

### 9. INCOME Table

```sql
CREATE TABLE income (
    id BIGSERIAL PRIMARY KEY,
    user_id BIGINT NOT NULL,
    source_id BIGINT NOT NULL,
    description VARCHAR(500),
    amount DECIMAL(15, 2) NOT NULL,
    currency VARCHAR(3) NOT NULL DEFAULT 'USD',
    income_date DATE NOT NULL,
    reference_number VARCHAR(100),
    notes TEXT,
    is_recurring BOOLEAN DEFAULT FALSE,
    recurring_income_id BIGINT,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    created_by VARCHAR(100),
    updated_by VARCHAR(100),
    deleted_at TIMESTAMP,
    
    FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE,
    FOREIGN KEY (source_id) REFERENCES income_sources(id) ON DELETE RESTRICT,
    CONSTRAINT amount_positive CHECK (amount > 0)
);

CREATE INDEX idx_income_user_id ON income(user_id);
CREATE INDEX idx_income_source_id ON income(source_id);
CREATE INDEX idx_income_income_date ON income(income_date);
CREATE INDEX idx_income_user_date ON income(user_id, income_date);
CREATE INDEX idx_income_deleted_at ON income(deleted_at) WHERE deleted_at IS NULL;
```

### 10. BUDGETS Table

```sql
CREATE TABLE budgets (
    id BIGSERIAL PRIMARY KEY,
    user_id BIGINT NOT NULL,
    category_id BIGINT,
    name VARCHAR(255) NOT NULL,
    amount DECIMAL(15, 2) NOT NULL,
    currency VARCHAR(3) NOT NULL DEFAULT 'USD',
    period VARCHAR(50) NOT NULL, -- MONTHLY, QUARTERLY, YEARLY
    start_date DATE NOT NULL,
    end_date DATE NOT NULL,
    alert_threshold DECIMAL(3, 0) DEFAULT 80, -- 80% of budget
    alert_frequency VARCHAR(50) DEFAULT 'WEEKLY', -- DAILY, WEEKLY, MONTHLY
    is_active BOOLEAN DEFAULT TRUE,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    deleted_at TIMESTAMP,
    
    FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE,
    FOREIGN KEY (category_id) REFERENCES expense_categories(id) ON DELETE SET NULL,
    CONSTRAINT amount_positive CHECK (amount > 0),
    CONSTRAINT valid_period CHECK (period IN ('MONTHLY', 'QUARTERLY', 'YEARLY')),
    CONSTRAINT valid_threshold CHECK (alert_threshold > 0 AND alert_threshold <= 100),
    CONSTRAINT dates_logical CHECK (end_date >= start_date)
);

CREATE INDEX idx_budgets_user_id ON budgets(user_id);
CREATE INDEX idx_budgets_category_id ON budgets(category_id);
CREATE INDEX idx_budgets_is_active ON budgets(is_active);
CREATE INDEX idx_budgets_period_dates ON budgets(user_id, start_date, end_date);
```

### 11. BUDGET_ALERTS Table

```sql
CREATE TABLE budget_alerts (
    id BIGSERIAL PRIMARY KEY,
    budget_id BIGINT NOT NULL,
    user_id BIGINT NOT NULL,
    alert_type VARCHAR(50) NOT NULL, -- THRESHOLD_REACHED, BUDGET_EXCEEDED
    current_spending DECIMAL(15, 2) NOT NULL,
    budget_limit DECIMAL(15, 2) NOT NULL,
    percentage_used DECIMAL(5, 2) NOT NULL,
    is_acknowledged BOOLEAN DEFAULT FALSE,
    acknowledged_at TIMESTAMP,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    
    FOREIGN KEY (budget_id) REFERENCES budgets(id) ON DELETE CASCADE,
    FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE,
    CONSTRAINT valid_alert_type CHECK (alert_type IN ('THRESHOLD_REACHED', 'BUDGET_EXCEEDED')),
    CONSTRAINT valid_percentage CHECK (percentage_used >= 0 AND percentage_used <= 200)
);

CREATE INDEX idx_budget_alerts_budget_id ON budget_alerts(budget_id);
CREATE INDEX idx_budget_alerts_user_id ON budget_alerts(user_id);
CREATE INDEX idx_budget_alerts_created_at ON budget_alerts(created_at);
```

### 12. SAVINGS_GOALS Table

```sql
CREATE TABLE savings_goals (
    id BIGSERIAL PRIMARY KEY,
    user_id BIGINT NOT NULL,
    goal_name VARCHAR(255) NOT NULL,
    description TEXT,
    target_amount DECIMAL(15, 2) NOT NULL,
    current_amount DECIMAL(15, 2) DEFAULT 0,
    currency VARCHAR(3) NOT NULL DEFAULT 'USD',
    target_date DATE NOT NULL,
    priority VARCHAR(50) NOT NULL, -- LOW, MEDIUM, HIGH
    status VARCHAR(50) NOT NULL, -- ACTIVE, PAUSED, COMPLETED, CANCELLED
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    completed_at TIMESTAMP,
    deleted_at TIMESTAMP,
    
    FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE,
    CONSTRAINT target_positive CHECK (target_amount > 0),
    CONSTRAINT current_positive CHECK (current_amount >= 0),
    CONSTRAINT current_not_exceed_target CHECK (current_amount <= target_amount),
    CONSTRAINT valid_priority CHECK (priority IN ('LOW', 'MEDIUM', 'HIGH')),
    CONSTRAINT valid_status CHECK (status IN ('ACTIVE', 'PAUSED', 'COMPLETED', 'CANCELLED'))
);

CREATE INDEX idx_savings_goals_user_id ON savings_goals(user_id);
CREATE INDEX idx_savings_goals_status ON savings_goals(status);
CREATE INDEX idx_savings_goals_target_date ON savings_goals(target_date);
```

### 13. SAVINGS_TRANSACTIONS Table

```sql
CREATE TABLE savings_transactions (
    id BIGSERIAL PRIMARY KEY,
    goal_id BIGINT NOT NULL,
    amount DECIMAL(15, 2) NOT NULL,
    transaction_date DATE NOT NULL,
    transaction_type VARCHAR(50) NOT NULL, -- DEPOSIT, WITHDRAWAL
    description TEXT,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    
    FOREIGN KEY (goal_id) REFERENCES savings_goals(id) ON DELETE CASCADE,
    CONSTRAINT amount_positive CHECK (amount > 0),
    CONSTRAINT valid_type CHECK (transaction_type IN ('DEPOSIT', 'WITHDRAWAL'))
);

CREATE INDEX idx_savings_transactions_goal_id ON savings_transactions(goal_id);
CREATE INDEX idx_savings_transactions_date ON savings_transactions(transaction_date);
```

### 14. INVESTMENTS Table

```sql
CREATE TABLE investments (
    id BIGSERIAL PRIMARY KEY,
    user_id BIGINT NOT NULL,
    investment_type VARCHAR(50) NOT NULL, -- STOCKS, MUTUAL_FUNDS, SIP, CRYPTO, BONDS, FIXED_DEPOSIT
    name VARCHAR(255) NOT NULL,
    ticker_symbol VARCHAR(20),
    description TEXT,
    buy_date DATE NOT NULL,
    quantity DECIMAL(15, 4) NOT NULL,
    buy_price DECIMAL(15, 2) NOT NULL,
    current_price DECIMAL(15, 2) NOT NULL,
    currency VARCHAR(3) NOT NULL DEFAULT 'USD',
    broker_name VARCHAR(255),
    status VARCHAR(50) NOT NULL, -- ACTIVE, SOLD, MATURED
    notes TEXT,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    deleted_at TIMESTAMP,
    
    FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE,
    CONSTRAINT quantity_positive CHECK (quantity > 0),
    CONSTRAINT prices_positive CHECK (buy_price > 0 AND current_price > 0),
    CONSTRAINT valid_type CHECK (investment_type IN ('STOCKS', 'MUTUAL_FUNDS', 'SIP', 'CRYPTO', 'BONDS', 'FIXED_DEPOSIT')),
    CONSTRAINT valid_status CHECK (status IN ('ACTIVE', 'SOLD', 'MATURED'))
);

CREATE INDEX idx_investments_user_id ON investments(user_id);
CREATE INDEX idx_investments_status ON investments(status);
CREATE INDEX idx_investments_type ON investments(investment_type);
```

### 15. INVESTMENT_TRANSACTIONS Table

```sql
CREATE TABLE investment_transactions (
    id BIGSERIAL PRIMARY KEY,
    investment_id BIGINT NOT NULL,
    transaction_type VARCHAR(50) NOT NULL, -- BUY, SELL, DIVIDEND, INTEREST
    quantity DECIMAL(15, 4) NOT NULL,
    price DECIMAL(15, 2) NOT NULL,
    amount DECIMAL(15, 2) NOT NULL,
    transaction_date DATE NOT NULL,
    description TEXT,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    
    FOREIGN KEY (investment_id) REFERENCES investments(id) ON DELETE CASCADE,
    CONSTRAINT quantity_positive CHECK (quantity > 0),
    CONSTRAINT price_positive CHECK (price > 0),
    CONSTRAINT amount_positive CHECK (amount > 0),
    CONSTRAINT valid_type CHECK (transaction_type IN ('BUY', 'SELL', 'DIVIDEND', 'INTEREST'))
);

CREATE INDEX idx_investment_transactions_investment_id ON investment_transactions(investment_id);
CREATE INDEX idx_investment_transactions_date ON investment_transactions(transaction_date);
```

### 16. TRANSACTIONS Table (Unified view)

```sql
CREATE TABLE transactions (
    id BIGSERIAL PRIMARY KEY,
    user_id BIGINT NOT NULL,
    transaction_type VARCHAR(50) NOT NULL, -- EXPENSE, INCOME, INVESTMENT, SAVINGS
    reference_id BIGINT,
    description VARCHAR(500),
    amount DECIMAL(15, 2) NOT NULL,
    currency VARCHAR(3) NOT NULL DEFAULT 'USD',
    transaction_date DATE NOT NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    
    FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE,
    CONSTRAINT amount_not_zero CHECK (amount != 0),
    CONSTRAINT valid_type CHECK (transaction_type IN ('EXPENSE', 'INCOME', 'INVESTMENT', 'SAVINGS'))
);

CREATE INDEX idx_transactions_user_date ON transactions(user_id, transaction_date);
CREATE INDEX idx_transactions_type ON transactions(transaction_type);
```

### 17. NOTIFICATIONS Table

```sql
CREATE TABLE notifications (
    id BIGSERIAL PRIMARY KEY,
    user_id BIGINT NOT NULL,
    notification_type VARCHAR(100) NOT NULL, -- BUDGET_ALERT, GOAL_UPDATE, BILL_REMINDER, INVESTMENT_UPDATE
    title VARCHAR(255) NOT NULL,
    message TEXT NOT NULL,
    related_entity_id BIGINT,
    related_entity_type VARCHAR(50),
    is_read BOOLEAN DEFAULT FALSE,
    read_at TIMESTAMP,
    sent_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    
    FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE,
    CONSTRAINT valid_type CHECK (notification_type IN ('BUDGET_ALERT', 'GOAL_UPDATE', 'BILL_REMINDER', 'INVESTMENT_UPDATE', 'GENERAL'))
);

CREATE INDEX idx_notifications_user_id ON notifications(user_id);
CREATE INDEX idx_notifications_is_read ON notifications(is_read);
CREATE INDEX idx_notifications_created_at ON notifications(created_at DESC);
CREATE INDEX idx_notifications_user_unread ON notifications(user_id, is_read) WHERE is_read = FALSE;
```

### 18. AUDIT_LOGS Table

```sql
CREATE TABLE audit_logs (
    id BIGSERIAL PRIMARY KEY,
    user_id BIGINT,
    entity_type VARCHAR(100) NOT NULL,
    entity_id BIGINT NOT NULL,
    action VARCHAR(50) NOT NULL, -- CREATE, UPDATE, DELETE
    old_values JSONB,
    new_values JSONB,
    change_description TEXT,
    ip_address INET,
    user_agent TEXT,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    
    FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE SET NULL,
    CONSTRAINT valid_action CHECK (action IN ('CREATE', 'UPDATE', 'DELETE'))
);

CREATE INDEX idx_audit_logs_user_id ON audit_logs(user_id);
CREATE INDEX idx_audit_logs_entity ON audit_logs(entity_type, entity_id);
CREATE INDEX idx_audit_logs_created_at ON audit_logs(created_at DESC);
CREATE INDEX idx_audit_logs_action ON audit_logs(action);
```

### 19. PASSWORD_RESET_TOKENS Table

```sql
CREATE TABLE password_reset_tokens (
    id BIGSERIAL PRIMARY KEY,
    user_id BIGINT NOT NULL,
    token VARCHAR(255) NOT NULL UNIQUE,
    token_hash VARCHAR(255) NOT NULL UNIQUE,
    expires_at TIMESTAMP NOT NULL,
    used_at TIMESTAMP,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    
    FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE
);

CREATE INDEX idx_password_reset_tokens_user_id ON password_reset_tokens(user_id);
CREATE INDEX idx_password_reset_tokens_expires_at ON password_reset_tokens(expires_at);
CREATE INDEX idx_password_reset_tokens_token_hash ON password_reset_tokens(token_hash);
```

### 20. REFRESH_TOKENS Table

```sql
CREATE TABLE refresh_tokens (
    id BIGSERIAL PRIMARY KEY,
    user_id BIGINT NOT NULL,
    token VARCHAR(512) NOT NULL UNIQUE,
    token_hash VARCHAR(255) NOT NULL UNIQUE,
    expires_at TIMESTAMP NOT NULL,
    revoked_at TIMESTAMP,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    
    FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE
);

CREATE INDEX idx_refresh_tokens_user_id ON refresh_tokens(user_id);
CREATE INDEX idx_refresh_tokens_expires_at ON refresh_tokens(expires_at);
CREATE INDEX idx_refresh_tokens_token_hash ON refresh_tokens(token_hash);
CREATE INDEX idx_refresh_tokens_revoked ON refresh_tokens(revoked_at) WHERE revoked_at IS NULL;
```

---

## Database Constraints Summary

| Constraint Type | Count | Examples |
|-----------------|-------|----------|
| Primary Keys | 20 | All tables |
| Foreign Keys | 25+ | User relationships |
| Unique Constraints | 10+ | Email, username, category names |
| Check Constraints | 30+ | Amount validation, status enums |
| NOT NULL | 100+ | Required fields |
| Default Values | 20+ | Timestamps, active flags |

---

## Indexes Strategy

### High-Priority Indexes (Query Performance)
```sql
-- User lookup
CREATE INDEX idx_users_email ON users(email);
CREATE INDEX idx_users_username ON users(username);

-- Date range queries
CREATE INDEX idx_expenses_user_date ON expenses(user_id, expense_date);
CREATE INDEX idx_income_user_date ON income(user_id, income_date);

-- Budget tracking
CREATE INDEX idx_budgets_period_dates ON budgets(user_id, start_date, end_date);

-- Notifications
CREATE INDEX idx_notifications_user_unread ON notifications(user_id, is_read);
```

### Medium-Priority Indexes (Foreign Keys)
```sql
CREATE INDEX idx_expenses_category_id ON expense_categories(user_id);
CREATE INDEX idx_income_source_id ON income_sources(user_id);
```

### Low-Priority Indexes (Soft Delete Optimization)
```sql
CREATE INDEX idx_expenses_deleted_at ON expenses(deleted_at) WHERE deleted_at IS NULL;
CREATE INDEX idx_income_deleted_at ON income(deleted_at) WHERE deleted_at IS NULL;
```

---

## Partitioning Strategy (Future)

### Partition EXPENSES by year
```sql
CREATE TABLE expenses_2024 PARTITION OF expenses
    FOR VALUES FROM ('2024-01-01') TO ('2025-01-01');

CREATE TABLE expenses_2025 PARTITION OF expenses
    FOR VALUES FROM ('2025-01-01') TO ('2026-01-01');
```

### Benefits:
- Faster queries on specific date ranges
- Easier archival of old data
- Parallel query execution
- Smaller index sizes

---

## Backup and Recovery Strategy

### Backup Schedule
- **Full backup:** Daily at 2 AM UTC
- **Incremental backup:** Every 6 hours
- **Transaction logs:** Continuous archival

### Retention Policy
- **Daily backups:** 30 days
- **Weekly backups:** 12 weeks
- **Monthly backups:** 12 months

### Recovery Point Objective (RPO): 1 hour
### Recovery Time Objective (RTO): 4 hours

---

## Performance Optimization Tips

### 1. Query Optimization
```sql
-- Use EXPLAIN ANALYZE
EXPLAIN ANALYZE
SELECT * FROM expenses 
WHERE user_id = 1 AND expense_date BETWEEN '2026-01-01' AND '2026-03-31';
```

### 2. Connection Pooling
```
Max connections: 200
Min idle: 10
Max wait: 30s
```

### 3. Vacuum and Analyze
```sql
-- Weekly maintenance
VACUUM ANALYZE;
REINDEX DATABASE daily_finance_tracker;
```

---

## Security Considerations

1. **Row-Level Security (RLS):** Users see only their data
2. **Encryption at Rest:** Database encryption enabled
3. **Encryption in Transit:** SSL/TLS connections only
4. **Audit Logging:** All modifications logged
5. **Regular Backups:** Encrypted backup storage

---

## Future Enhancements

1. **Multi-currency Support:** Already in schema
2. **Time Zone Support:** Ready for implementation
3. **Data Masking:** For sensitive audit trails
4. **Archive Tables:** For historical data
5. **Materialized Views:** For analytics queries

