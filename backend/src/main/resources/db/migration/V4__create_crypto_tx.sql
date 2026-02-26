-- V4__create_crypto_tx.sql
-- 加密货币交易明细表

CREATE TABLE crypto_tx (
    id                  BIGSERIAL       PRIMARY KEY,
    main_account_id     BIGINT          NOT NULL,
    sub_account_id      BIGINT          NOT NULL,
    tx_hash             VARCHAR(255),
    direction           VARCHAR(20)     NOT NULL,
    category            VARCHAR(50),
    business_type       VARCHAR(50),
    currency            VARCHAR(50)     NOT NULL,
    amount              DECIMAL(30, 18) NOT NULL,
    from_address        VARCHAR(255),
    to_address          VARCHAR(255),
    fee                 DECIMAL(30, 18) DEFAULT 0,
    fee_currency        VARCHAR(50),
    chain_name          VARCHAR(50),
    trade_time          TIMESTAMP       NOT NULL,
    description         TEXT,
    import_batch_id     VARCHAR(100),
    row_no              INTEGER,
    is_deleted          BOOLEAN         DEFAULT FALSE,
    created_at          TIMESTAMP       DEFAULT CURRENT_TIMESTAMP,
    updated_at          TIMESTAMP       DEFAULT CURRENT_TIMESTAMP
);

-- 去重唯一索引
CREATE UNIQUE INDEX uk_tx_dedupe ON crypto_tx (
    sub_account_id, tx_hash, direction, currency, amount, from_address, to_address, trade_time, row_no
) WHERE is_deleted = FALSE;

-- 常用查询索引
CREATE INDEX idx_tx_main_account ON crypto_tx (main_account_id) WHERE is_deleted = FALSE;
CREATE INDEX idx_tx_sub_account ON crypto_tx (sub_account_id) WHERE is_deleted = FALSE;
CREATE INDEX idx_tx_hash ON crypto_tx (tx_hash) WHERE is_deleted = FALSE;
CREATE INDEX idx_tx_trade_time ON crypto_tx (trade_time) WHERE is_deleted = FALSE;
CREATE INDEX idx_tx_batch ON crypto_tx (import_batch_id);
CREATE INDEX idx_tx_direction ON crypto_tx (direction) WHERE is_deleted = FALSE;
