-- V2__create_main_account.sql
-- 主账户表

CREATE TABLE main_account (
    id              BIGSERIAL       PRIMARY KEY,
    account_name    VARCHAR(255)    NOT NULL,
    account_type    VARCHAR(50),
    account_source  VARCHAR(50),
    address         VARCHAR(255),
    market_value    DECIMAL(30, 8)  DEFAULT 0,
    status          VARCHAR(20)     DEFAULT 'active',
    organization    VARCHAR(255),
    is_deleted      BOOLEAN         DEFAULT FALSE,
    created_at      TIMESTAMP       DEFAULT CURRENT_TIMESTAMP,
    updated_at      TIMESTAMP       DEFAULT CURRENT_TIMESTAMP
);

-- 索引
CREATE INDEX idx_main_account_status ON main_account (status) WHERE is_deleted = FALSE;
CREATE INDEX idx_main_account_name ON main_account (account_name) WHERE is_deleted = FALSE;

-- 唯一约束：同组织下账户名不可重复
CREATE UNIQUE INDEX uk_main_account_name ON main_account (account_name, organization) WHERE is_deleted = FALSE;
