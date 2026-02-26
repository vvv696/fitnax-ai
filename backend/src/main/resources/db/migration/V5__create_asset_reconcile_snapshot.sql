-- V5__create_asset_reconcile_snapshot.sql
-- 资产对账快照表

CREATE TABLE asset_reconcile_snapshot (
    id                  BIGSERIAL       PRIMARY KEY,
    main_account_id     BIGINT          NOT NULL,
    sub_account_id      BIGINT          NOT NULL,
    currency            VARCHAR(50)     NOT NULL,
    system_total        DECIMAL(30, 18) DEFAULT 0,
    onchain_balance     DECIMAL(30, 18) DEFAULT 0,
    diff                DECIMAL(30, 18) DEFAULT 0,
    diff_abs            DECIMAL(30, 18) DEFAULT 0,
    diff_status         VARCHAR(20),
    snapshot_time       TIMESTAMP       NOT NULL,
    created_at          TIMESTAMP       DEFAULT CURRENT_TIMESTAMP,
    updated_at          TIMESTAMP       DEFAULT CURRENT_TIMESTAMP
);

-- 唯一约束：同子账户+币种+快照时间不可重复
CREATE UNIQUE INDEX uk_snapshot ON asset_reconcile_snapshot (sub_account_id, currency, snapshot_time);

-- 查询索引
CREATE INDEX idx_snapshot_main_account ON asset_reconcile_snapshot (main_account_id);
CREATE INDEX idx_snapshot_sub_account ON asset_reconcile_snapshot (sub_account_id);
CREATE INDEX idx_snapshot_time ON asset_reconcile_snapshot (snapshot_time);
CREATE INDEX idx_snapshot_currency ON asset_reconcile_snapshot (currency);
