-- V1__Initial_Schema.sql
-- Daily Personal Finance Tracker - Initial Database Schema
-- Version: 1.0.0
-- Created: 2026-03-07

-- ============================================================
-- ENUMS / TYPES
-- ============================================================

CREATE TYPE user_role AS ENUM ('USER', 'ADMIN', 'MODERATOR');
CREATE TYPE payment_method AS ENUM ('CASH', 'CARD', 'BANK_TRANSFER', 'UPI', 'OTHER');
CREATE TYPE budget_period AS ENUM ('MONTHLY', 'QUARTERLY', 'YEARLY');
CREATE TYPE investment_type AS ENUM ('STOCKS', 'MUTUAL_FUNDS', 'SIP', 'CRYPTO', 'BONDS', 'FIXED_DEPOSIT');
CREATE TYPE investment_status AS ENUM ('ACTIVE', 'SOLD', 'MATURED');
CREATE TYPE transaction_type AS ENUM ('BUY', 'SELL', 'DIVIDEND', 'INTEREST');
CREATE TYPE notification_type AS ENUM ('BUDGET_ALERT', 'GOAL_UPDATE', 'BILL_REMINDER', 'INVESTMENT_UPDATE', 'GENERAL');
CREATE TYPE audit_action AS ENUM ('CREATE', 'UPDATE', 'DELETE');
CREATE TYPE frequency_type AS ENUM ('DAILY', 'WEEKLY', 'BIWEEKLY', 'MONTHLY', 'QUARTERLY', 'YEARLY');
CREATE TYPE priority_level AS ENUM ('LOW', 'MEDIUM', 'HIGH');
CREATE TYPE goal_status AS ENUM ('ACTIVE', 'PAUSED', 'COMPLETED', 'CANCELLED');

-- ============================================================
-- USERS TABLE
-- ============================================================

CREATE TABLE IF NOT EXISTS users (
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

-- ============================================================
-- ROLES TABLE
-- ============================================================

CREATE TABLE IF NOT EXISTS roles (
    id SERIAL PRIMARY KEY,
    name user_role NOT NULL UNIQUE,
    description TEXT,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

INSERT INTO roles (name, description) VALUES 
    ('USER', 'Regular user with standard permissions'),
    ('ADMIN', 'Administrator with full access'),
    ('MODERATOR', 'Moderator with limited admin access')
ON CONFLICT DO NOTHING;

-- ============================================================
-- USER_ROLES TABLE
-- ============================================================

CREATE TABLE IF NOT EXISTS user_roles (
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

-- ============================================================
-- EXPENSE_CATEGORIES TABLE
-- ============================================================

CREATE TABLE IF NOT EXISTS expense_categories (
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

-- ============================================================
-- EXPENSES TABLE
-- ============================================================

CREATE TABLE IF NOT EXISTS expenses (
    id BIGSERIAL PRIMARY KEY,
    user_id BIGINT NOT NULL,
    category_id BIGINT NOT NULL,
    description VARCHAR(500),
    amount DECIMAL(15, 2) NOT NULL,
    currency VARCHAR(3) NOT NULL DEFAULT 'USD',
    expense_date DATE NOT NULL,
    payment_method payment_method,
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
    CONSTRAINT amount_positive CHECK (amount > 0)
);

CREATE INDEX idx_expenses_user_id ON expenses(user_id);
CREATE INDEX idx_expenses_category_id ON expenses(category_id);
CREATE INDEX idx_expenses_expense_date ON expenses(expense_date);
CREATE INDEX idx_expenses_user_date ON expenses(user_id, expense_date);
CREATE INDEX idx_expenses_created_at ON expenses(created_at);
CREATE INDEX idx_expenses_deleted_at ON expenses(deleted_at) WHERE deleted_at IS NULL;

-- ============================================================
-- EXPENSE_TAGS TABLE
-- ============================================================

CREATE TABLE IF NOT EXISTS expense_tags (
    id BIGSERIAL PRIMARY KEY,
    expense_id BIGINT NOT NULL,
    tag_name VARCHAR(100) NOT NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    
    FOREIGN KEY (expense_id) REFERENCES expenses(id) ON DELETE CASCADE,
    UNIQUE(expense_id, tag_name)
);

CREATE INDEX idx_expense_tags_expense_id ON expense_tags(expense_id);
CREATE INDEX idx_expense_tags_tag_name ON expense_tags(tag_name);

-- ============================================================
-- RECURRING_EXPENSES TABLE
-- ============================================================

CREATE TABLE IF NOT EXISTS recurring_expenses (
    id BIGSERIAL PRIMARY KEY,
    user_id BIGINT NOT NULL,
    category_id BIGINT NOT NULL,
    description VARCHAR(500) NOT NULL,
    amount DECIMAL(15, 2) NOT NULL,
    currency VARCHAR(3) NOT NULL DEFAULT 'USD',
    frequency frequency_type NOT NULL,
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
    CONSTRAINT dates_logical CHECK (end_date IS NULL OR end_date >= start_date)
);

CREATE INDEX idx_recurring_expenses_user_id ON recurring_expenses(user_id);
CREATE INDEX idx_recurring_expenses_next_date ON recurring_expenses(next_occurrence_date);
CREATE INDEX idx_recurring_expenses_active ON recurring_expenses(is_active);

-- ============================================================
-- INCOME_SOURCES TABLE
-- ============================================================

CREATE TABLE IF NOT EXISTS income_sources (
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

-- ============================================================
-- INCOME TABLE
-- ============================================================

CREATE TABLE IF NOT EXISTS income (
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

-- ============================================================
-- BUDGETS TABLE
-- ============================================================

CREATE TABLE IF NOT EXISTS budgets (
    id BIGSERIAL PRIMARY KEY,
    user_id BIGINT NOT NULL,
    category_id BIGINT,
    name VARCHAR(255) NOT NULL,
    amount DECIMAL(15, 2) NOT NULL,
    currency VARCHAR(3) NOT NULL DEFAULT 'USD',
    period budget_period NOT NULL,
    start_date DATE NOT NULL,
    end_date DATE NOT NULL,
    alert_threshold DECIMAL(3, 0) DEFAULT 80,
    alert_frequency VARCHAR(50) DEFAULT 'WEEKLY',
    is_active BOOLEAN DEFAULT TRUE,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    deleted_at TIMESTAMP,
    
    FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE,
    FOREIGN KEY (category_id) REFERENCES expense_categories(id) ON DELETE SET NULL,
    CONSTRAINT amount_positive CHECK (amount > 0),
    CONSTRAINT valid_threshold CHECK (alert_threshold > 0 AND alert_threshold <= 100),
    CONSTRAINT dates_logical CHECK (end_date >= start_date)
);

CREATE INDEX idx_budgets_user_id ON budgets(user_id);
CREATE INDEX idx_budgets_category_id ON budgets(category_id);
CREATE INDEX idx_budgets_is_active ON budgets(is_active);
CREATE INDEX idx_budgets_period_dates ON budgets(user_id, start_date, end_date);

-- ============================================================
-- BUDGET_ALERTS TABLE
-- ============================================================

CREATE TABLE IF NOT EXISTS budget_alerts (
    id BIGSERIAL PRIMARY KEY,
    budget_id BIGINT NOT NULL,
    user_id BIGINT NOT NULL,
    alert_type VARCHAR(50) NOT NULL,
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

-- ============================================================
-- SAVINGS_GOALS TABLE
-- ============================================================

CREATE TABLE IF NOT EXISTS savings_goals (
    id BIGSERIAL PRIMARY KEY,
    user_id BIGINT NOT NULL,
    goal_name VARCHAR(255) NOT NULL,
    description TEXT,
    target_amount DECIMAL(15, 2) NOT NULL,
    current_amount DECIMAL(15, 2) DEFAULT 0,
    currency VARCHAR(3) NOT NULL DEFAULT 'USD',
    target_date DATE NOT NULL,
    priority priority_level NOT NULL,
    status goal_status NOT NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    completed_at TIMESTAMP,
    deleted_at TIMESTAMP,
    
    FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE,
    CONSTRAINT target_positive CHECK (target_amount > 0),
    CONSTRAINT current_positive CHECK (current_amount >= 0),
    CONSTRAINT current_not_exceed_target CHECK (current_amount <= target_amount)
);

CREATE INDEX idx_savings_goals_user_id ON savings_goals(user_id);
CREATE INDEX idx_savings_goals_status ON savings_goals(status);
CREATE INDEX idx_savings_goals_target_date ON savings_goals(target_date);

-- ============================================================
-- SAVINGS_TRANSACTIONS TABLE
-- ============================================================

CREATE TABLE IF NOT EXISTS savings_transactions (
    id BIGSERIAL PRIMARY KEY,
    goal_id BIGINT NOT NULL,
    amount DECIMAL(15, 2) NOT NULL,
    transaction_date DATE NOT NULL,
    transaction_type VARCHAR(50) NOT NULL,
    description TEXT,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    
    FOREIGN KEY (goal_id) REFERENCES savings_goals(id) ON DELETE CASCADE,
    CONSTRAINT amount_positive CHECK (amount > 0),
    CONSTRAINT valid_type CHECK (transaction_type IN ('DEPOSIT', 'WITHDRAWAL'))
);

CREATE INDEX idx_savings_transactions_goal_id ON savings_transactions(goal_id);
CREATE INDEX idx_savings_transactions_date ON savings_transactions(transaction_date);

-- ============================================================
-- INVESTMENTS TABLE
-- ============================================================

CREATE TABLE IF NOT EXISTS investments (
    id BIGSERIAL PRIMARY KEY,
    user_id BIGINT NOT NULL,
    investment_type investment_type NOT NULL,
    name VARCHAR(255) NOT NULL,
    ticker_symbol VARCHAR(20),
    description TEXT,
    buy_date DATE NOT NULL,
    quantity DECIMAL(15, 4) NOT NULL,
    buy_price DECIMAL(15, 2) NOT NULL,
    current_price DECIMAL(15, 2) NOT NULL,
    currency VARCHAR(3) NOT NULL DEFAULT 'USD',
    broker_name VARCHAR(255),
    status investment_status NOT NULL,
    notes TEXT,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    deleted_at TIMESTAMP,
    
    FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE,
    CONSTRAINT quantity_positive CHECK (quantity > 0),
    CONSTRAINT prices_positive CHECK (buy_price > 0 AND current_price > 0)
);

CREATE INDEX idx_investments_user_id ON investments(user_id);
CREATE INDEX idx_investments_status ON investments(status);
CREATE INDEX idx_investments_type ON investments(investment_type);

-- ============================================================
-- INVESTMENT_TRANSACTIONS TABLE
-- ============================================================

CREATE TABLE IF NOT EXISTS investment_transactions (
    id BIGSERIAL PRIMARY KEY,
    investment_id BIGINT NOT NULL,
    transaction_type transaction_type NOT NULL,
    quantity DECIMAL(15, 4) NOT NULL,
    price DECIMAL(15, 2) NOT NULL,
    amount DECIMAL(15, 2) NOT NULL,
    transaction_date DATE NOT NULL,
    description TEXT,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    
    FOREIGN KEY (investment_id) REFERENCES investments(id) ON DELETE CASCADE,
    CONSTRAINT quantity_positive CHECK (quantity > 0),
    CONSTRAINT price_positive CHECK (price > 0),
    CONSTRAINT amount_positive CHECK (amount > 0)
);

CREATE INDEX idx_investment_transactions_investment_id ON investment_transactions(investment_id);
CREATE INDEX idx_investment_transactions_date ON investment_transactions(transaction_date);

-- ============================================================
-- TRANSACTIONS TABLE (Unified view)
-- ============================================================

CREATE TABLE IF NOT EXISTS transactions (
    id BIGSERIAL PRIMARY KEY,
    user_id BIGINT NOT NULL,
    transaction_type VARCHAR(50) NOT NULL,
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

-- ============================================================
-- NOTIFICATIONS TABLE
-- ============================================================

CREATE TABLE IF NOT EXISTS notifications (
    id BIGSERIAL PRIMARY KEY,
    user_id BIGINT NOT NULL,
    notification_type notification_type NOT NULL,
    title VARCHAR(255) NOT NULL,
    message TEXT NOT NULL,
    related_entity_id BIGINT,
    related_entity_type VARCHAR(50),
    is_read BOOLEAN DEFAULT FALSE,
    read_at TIMESTAMP,
    sent_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    
    FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE
);

CREATE INDEX idx_notifications_user_id ON notifications(user_id);
CREATE INDEX idx_notifications_is_read ON notifications(is_read);
CREATE INDEX idx_notifications_created_at ON notifications(created_at DESC);
CREATE INDEX idx_notifications_user_unread ON notifications(user_id, is_read) WHERE is_read = FALSE;

-- ============================================================
-- AUDIT_LOGS TABLE
-- ============================================================

CREATE TABLE IF NOT EXISTS audit_logs (
    id BIGSERIAL PRIMARY KEY,
    user_id BIGINT,
    entity_type VARCHAR(100) NOT NULL,
    entity_id BIGINT NOT NULL,
    action audit_action NOT NULL,
    old_values JSONB,
    new_values JSONB,
    change_description TEXT,
    ip_address INET,
    user_agent TEXT,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    
    FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE SET NULL
);

CREATE INDEX idx_audit_logs_user_id ON audit_logs(user_id);
CREATE INDEX idx_audit_logs_entity ON audit_logs(entity_type, entity_id);
CREATE INDEX idx_audit_logs_created_at ON audit_logs(created_at DESC);
CREATE INDEX idx_audit_logs_action ON audit_logs(action);

-- ============================================================
-- PASSWORD_RESET_TOKENS TABLE
-- ============================================================

CREATE TABLE IF NOT EXISTS password_reset_tokens (
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

-- ============================================================
-- REFRESH_TOKENS TABLE
-- ============================================================

CREATE TABLE IF NOT EXISTS refresh_tokens (
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

-- ============================================================
-- GRANT PERMISSIONS (PostSQL Role-Based Access)
-- ============================================================

-- Note: Configure these based on your deployment environment
-- GRANT ALL PRIVILEGES ON ALL TABLES IN SCHEMA public TO app_user;
-- GRANT ALL PRIVILEGES ON ALL SEQUENCES IN SCHEMA public TO app_user;

