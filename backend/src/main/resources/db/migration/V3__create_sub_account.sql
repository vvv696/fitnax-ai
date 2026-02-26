-- V3__create_sub_account.sql
-- 子账户表

CREATE TABLE sub_account (
    id                  BIGSERIAL       PRIMARY KEY,
    main_account_id     BIGINT          NOT NULL REFERENCES main_account(id),
    sub_account_name    VARCHAR(255)    NOT NULL,
    category            VARCHAR(50),
    account_source      VARCHAR(50),
    import_type         VARCHAR(50),
    import_file_name    VARCHAR(500),
    import_batch_id     VARCHAR(100),
    address             VARCHAR(255),
    status              VARCHAR(20)     DEFAULT 'active',
    is_deleted          BOOLEAN         DEFAULT FALSE,
    created_at          TIMESTAMP       DEFAULT CURRENT_TIMESTAMP,
    updated_at          TIMESTAMP       DEFAULT CURRENT_TIMESTAMP
);

-- 索引
CREATE INDEX idx_sub_account_main_id ON sub_account (main_account_id) WHERE is_deleted = FALSE;
CREATE INDEX idx_sub_account_batch ON sub_account (import_batch_id);
CREATE INDEX idx_sub_account_status ON sub_account (status) WHERE is_deleted = FALSE;

-- 唯一约束：同主账户下子账户名不可重复
CREATE UNIQUE INDEX uk_sub_account_name ON sub_account (main_account_id, sub_account_name) WHERE is_deleted = FALSE;
