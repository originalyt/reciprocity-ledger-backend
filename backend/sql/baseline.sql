BEGIN;

-- =========================================================
-- Reciprocity Ledger Baseline Schema
-- 说明：
-- 1) 本文件是当前唯一基线 SQL，适用于全新数据库直接初始化
-- 2) 后续结构调整请新增增量 SQL，不再回拆成多份基线脚本
-- 3) 所有关联采用逻辑关联（*_id / code），不使用 FOREIGN KEY
-- =========================================================

CREATE TABLE IF NOT EXISTS app_user (
  id                  BIGINT PRIMARY KEY,
  phone               VARCHAR(20) NOT NULL,
  nickname            VARCHAR(64),
  status              VARCHAR(16) NOT NULL DEFAULT 'ACTIVE',
  last_login_at       TIMESTAMPTZ,
  created_at          TIMESTAMPTZ NOT NULL DEFAULT NOW(),
  updated_at          TIMESTAMPTZ NOT NULL DEFAULT NOW(),
  created_by          BIGINT,
  updated_by          BIGINT,
  CONSTRAINT uq_app_user_phone UNIQUE (phone),
  CONSTRAINT ck_app_user_status CHECK (status IN ('ACTIVE', 'DISABLED'))
);

COMMENT ON TABLE app_user IS '应用用户表（多用户隔离主体）';
COMMENT ON COLUMN app_user.id IS '主键ID（应用生成，非自增）';
COMMENT ON COLUMN app_user.phone IS '登录手机号，唯一';
COMMENT ON COLUMN app_user.nickname IS '用户昵称';
COMMENT ON COLUMN app_user.status IS '用户状态：ACTIVE启用，DISABLED禁用';
COMMENT ON COLUMN app_user.last_login_at IS '最近登录时间';
COMMENT ON COLUMN app_user.created_at IS '创建时间（审计）';
COMMENT ON COLUMN app_user.updated_at IS '修改时间（审计）';
COMMENT ON COLUMN app_user.created_by IS '创建人ID（审计）';
COMMENT ON COLUMN app_user.updated_by IS '修改人ID（审计）';

CREATE TABLE IF NOT EXISTS event_type_dict (
  id                  BIGINT PRIMARY KEY,
  code                VARCHAR(32) NOT NULL,
  name                VARCHAR(64) NOT NULL,
  sort_order          INTEGER NOT NULL DEFAULT 0,
  enabled             BOOLEAN NOT NULL DEFAULT TRUE,
  built_in            BOOLEAN NOT NULL DEFAULT TRUE,
  created_at          TIMESTAMPTZ NOT NULL DEFAULT NOW(),
  updated_at          TIMESTAMPTZ NOT NULL DEFAULT NOW(),
  created_by          BIGINT,
  updated_by          BIGINT,
  CONSTRAINT uq_event_type_dict_code UNIQUE (code)
);

COMMENT ON TABLE event_type_dict IS '事件类型字典表（前后端统一口径）';
COMMENT ON COLUMN event_type_dict.id IS '主键ID（手工或应用生成，非自增）';
COMMENT ON COLUMN event_type_dict.code IS '事件类型编码（业务唯一）';
COMMENT ON COLUMN event_type_dict.name IS '事件类型名称';
COMMENT ON COLUMN event_type_dict.sort_order IS '排序值，越小越靠前';
COMMENT ON COLUMN event_type_dict.enabled IS '是否启用';
COMMENT ON COLUMN event_type_dict.built_in IS '是否系统内置';
COMMENT ON COLUMN event_type_dict.created_at IS '创建时间（审计）';
COMMENT ON COLUMN event_type_dict.updated_at IS '修改时间（审计）';
COMMENT ON COLUMN event_type_dict.created_by IS '创建人ID（审计）';
COMMENT ON COLUMN event_type_dict.updated_by IS '修改人ID（审计）';

CREATE TABLE IF NOT EXISTS contact (
  id                  BIGINT PRIMARY KEY,
  user_id             BIGINT NOT NULL,
  name                VARCHAR(64) NOT NULL,
  relation            VARCHAR(64),
  phone               VARCHAR(20),
  note                VARCHAR(500),
  last_interaction_on DATE,
  created_at          TIMESTAMPTZ NOT NULL DEFAULT NOW(),
  updated_at          TIMESTAMPTZ NOT NULL DEFAULT NOW(),
  created_by          BIGINT,
  updated_by          BIGINT,
  CONSTRAINT ck_contact_name_not_blank CHECK (BTRIM(name) <> ''),
  CONSTRAINT uq_contact_user_id_id UNIQUE (user_id, id)
);

COMMENT ON TABLE contact IS '联系人主数据表';
COMMENT ON COLUMN contact.id IS '主键ID（应用生成，非自增）';
COMMENT ON COLUMN contact.user_id IS '所属用户ID（逻辑关联 app_user.id）';
COMMENT ON COLUMN contact.name IS '联系人姓名（必填）';
COMMENT ON COLUMN contact.relation IS '与我的关系';
COMMENT ON COLUMN contact.phone IS '联系人手机号';
COMMENT ON COLUMN contact.note IS '备注';
COMMENT ON COLUMN contact.last_interaction_on IS '最近往来日期（便于排序）';
COMMENT ON COLUMN contact.created_at IS '创建时间（审计）';
COMMENT ON COLUMN contact.updated_at IS '修改时间（审计）';
COMMENT ON COLUMN contact.created_by IS '创建人ID（审计）';
COMMENT ON COLUMN contact.updated_by IS '修改人ID（审计）';

CREATE INDEX IF NOT EXISTS idx_contact_user_name
  ON contact (user_id, name);

CREATE INDEX IF NOT EXISTS idx_contact_user_last_interaction
  ON contact (user_id, last_interaction_on DESC NULLS LAST);

CREATE INDEX IF NOT EXISTS idx_contact_user_name_phone
  ON contact (user_id, name, phone);

CREATE TABLE IF NOT EXISTS event_exchange (
  id                  BIGINT PRIMARY KEY,
  user_id             BIGINT NOT NULL,
  contact_id          BIGINT NOT NULL,
  event_type_code     VARCHAR(32) NOT NULL,
  event_note          VARCHAR(200) NOT NULL DEFAULT '',
  give_record_id      BIGINT,
  receive_record_id   BIGINT,
  give_amount         NUMERIC(12,2) NOT NULL DEFAULT 0,
  receive_amount      NUMERIC(12,2) NOT NULL DEFAULT 0,
  net_amount          NUMERIC(12,2) GENERATED ALWAYS AS (receive_amount - give_amount) STORED,
  latest_occurred_on  DATE NOT NULL,
  reciprocity_status  VARCHAR(16) NOT NULL,
  created_at          TIMESTAMPTZ NOT NULL DEFAULT NOW(),
  updated_at          TIMESTAMPTZ NOT NULL DEFAULT NOW(),
  created_by          BIGINT,
  updated_by          BIGINT,
  CONSTRAINT uq_event_exchange_key UNIQUE (user_id, contact_id, event_type_code, event_note),
  CONSTRAINT uq_event_exchange_user_id_id UNIQUE (user_id, id),
  CONSTRAINT ck_event_exchange_status CHECK (reciprocity_status IN ('MUTUAL', 'WAIT_OTHER', 'WAIT_ME')),
  CONSTRAINT ck_event_exchange_amount_non_negative CHECK (give_amount >= 0 AND receive_amount >= 0)
);

COMMENT ON TABLE event_exchange IS '事件往来聚合表（同联系人+同事件配对结果）';
COMMENT ON COLUMN event_exchange.id IS '主键ID（应用生成，非自增）';
COMMENT ON COLUMN event_exchange.user_id IS '所属用户ID（逻辑关联 app_user.id）';
COMMENT ON COLUMN event_exchange.contact_id IS '联系人ID（逻辑关联 contact.id）';
COMMENT ON COLUMN event_exchange.event_type_code IS '事件类型编码（逻辑关联 event_type_dict.code）';
COMMENT ON COLUMN event_exchange.event_note IS '事件说明，空串参与唯一键';
COMMENT ON COLUMN event_exchange.give_record_id IS '随礼记录ID（逻辑关联 ledger_record.id）';
COMMENT ON COLUMN event_exchange.receive_record_id IS '收礼记录ID（逻辑关联 ledger_record.id）';
COMMENT ON COLUMN event_exchange.give_amount IS '随礼金额（聚合快照）';
COMMENT ON COLUMN event_exchange.receive_amount IS '收礼金额（聚合快照）';
COMMENT ON COLUMN event_exchange.net_amount IS '事件净额=收礼金额-随礼金额（生成列）';
COMMENT ON COLUMN event_exchange.latest_occurred_on IS '最近发生日期（两条记录取最大）';
COMMENT ON COLUMN event_exchange.reciprocity_status IS '回礼状态：MUTUAL/WAIT_OTHER/WAIT_ME';
COMMENT ON COLUMN event_exchange.created_at IS '创建时间（审计）';
COMMENT ON COLUMN event_exchange.updated_at IS '修改时间（审计）';
COMMENT ON COLUMN event_exchange.created_by IS '创建人ID（审计）';
COMMENT ON COLUMN event_exchange.updated_by IS '修改人ID（审计）';

CREATE INDEX IF NOT EXISTS idx_event_exchange_user_status_latest
  ON event_exchange (user_id, reciprocity_status, latest_occurred_on DESC);

CREATE INDEX IF NOT EXISTS idx_event_exchange_user_contact
  ON event_exchange (user_id, contact_id, latest_occurred_on DESC);

CREATE INDEX IF NOT EXISTS idx_event_exchange_user_event_type
  ON event_exchange (user_id, event_type_code, latest_occurred_on DESC);

CREATE TABLE IF NOT EXISTS ledger_record (
  id                  BIGINT PRIMARY KEY,
  user_id             BIGINT NOT NULL,
  event_exchange_id   BIGINT NOT NULL,
  record_type         VARCHAR(16) NOT NULL,
  occurred_on         DATE NOT NULL,
  amount              NUMERIC(12,2) NOT NULL,
  remark              VARCHAR(500),
  created_at          TIMESTAMPTZ NOT NULL DEFAULT NOW(),
  updated_at          TIMESTAMPTZ NOT NULL DEFAULT NOW(),
  created_by          BIGINT,
  updated_by          BIGINT,
  CONSTRAINT ck_ledger_record_type CHECK (record_type IN ('GIVE', 'RECEIVE')),
  CONSTRAINT ck_ledger_record_amount_positive CHECK (amount > 0),
  CONSTRAINT uq_ledger_record_event_type UNIQUE (event_exchange_id, record_type)
);

COMMENT ON TABLE ledger_record IS '礼账交易明细表（时间线与统计原始数据）';
COMMENT ON COLUMN ledger_record.id IS '主键ID（应用生成，非自增）';
COMMENT ON COLUMN ledger_record.user_id IS '所属用户ID（逻辑关联 app_user.id）';
COMMENT ON COLUMN ledger_record.event_exchange_id IS '事件往来ID（逻辑关联 event_exchange.id）';
COMMENT ON COLUMN ledger_record.record_type IS '记录类型：GIVE随礼，RECEIVE收礼';
COMMENT ON COLUMN ledger_record.occurred_on IS '礼金发生日期（业务统计口径）';
COMMENT ON COLUMN ledger_record.amount IS '金额，>0，保留2位小数';
COMMENT ON COLUMN ledger_record.remark IS '备注说明';
COMMENT ON COLUMN ledger_record.created_at IS '创建时间（审计）';
COMMENT ON COLUMN ledger_record.updated_at IS '修改时间（审计）';
COMMENT ON COLUMN ledger_record.created_by IS '创建人ID（审计）';
COMMENT ON COLUMN ledger_record.updated_by IS '修改人ID（审计）';

CREATE INDEX IF NOT EXISTS idx_ledger_record_user_occurred_on
  ON ledger_record (user_id, occurred_on DESC);

CREATE INDEX IF NOT EXISTS idx_ledger_record_user_type_occurred_on
  ON ledger_record (user_id, record_type, occurred_on DESC);

CREATE INDEX IF NOT EXISTS idx_ledger_record_user_event
  ON ledger_record (user_id, event_exchange_id, occurred_on DESC);

CREATE TABLE IF NOT EXISTS login_verification_code (
  id                  BIGINT PRIMARY KEY,
  phone               VARCHAR(20) NOT NULL,
  verification_code   VARCHAR(8) NOT NULL,
  expires_at          TIMESTAMPTZ NOT NULL,
  used                BOOLEAN NOT NULL DEFAULT FALSE,
  created_at          TIMESTAMPTZ NOT NULL DEFAULT NOW()
);

COMMENT ON TABLE login_verification_code IS '登录验证码表';
COMMENT ON COLUMN login_verification_code.id IS '主键ID（应用生成，非自增）';
COMMENT ON COLUMN login_verification_code.phone IS '手机号';
COMMENT ON COLUMN login_verification_code.verification_code IS '验证码';
COMMENT ON COLUMN login_verification_code.expires_at IS '过期时间';
COMMENT ON COLUMN login_verification_code.used IS '是否已使用';
COMMENT ON COLUMN login_verification_code.created_at IS '创建时间';

CREATE INDEX IF NOT EXISTS idx_login_verification_code_phone_created
  ON login_verification_code (phone, created_at DESC);

CREATE TABLE IF NOT EXISTS user_refresh_token (
  id                  BIGINT PRIMARY KEY,
  user_id             BIGINT NOT NULL,
  refresh_token       VARCHAR(128) NOT NULL,
  device_info         VARCHAR(200),
  expires_at          TIMESTAMPTZ NOT NULL,
  revoked             BOOLEAN NOT NULL DEFAULT FALSE,
  created_at          TIMESTAMPTZ NOT NULL DEFAULT NOW(),
  updated_at          TIMESTAMPTZ NOT NULL DEFAULT NOW(),
  CONSTRAINT uq_user_refresh_token_token UNIQUE (refresh_token)
);

COMMENT ON TABLE user_refresh_token IS '用户刷新令牌表';
COMMENT ON COLUMN user_refresh_token.id IS '主键ID（应用生成，非自增）';
COMMENT ON COLUMN user_refresh_token.user_id IS '用户ID';
COMMENT ON COLUMN user_refresh_token.refresh_token IS '刷新令牌';
COMMENT ON COLUMN user_refresh_token.device_info IS '设备信息';
COMMENT ON COLUMN user_refresh_token.expires_at IS '过期时间';
COMMENT ON COLUMN user_refresh_token.revoked IS '是否已撤销';
COMMENT ON COLUMN user_refresh_token.created_at IS '创建时间';
COMMENT ON COLUMN user_refresh_token.updated_at IS '更新时间';

CREATE INDEX IF NOT EXISTS idx_user_refresh_token_user_id
  ON user_refresh_token (user_id, revoked, expires_at DESC);

INSERT INTO event_type_dict(id, code, name, sort_order, enabled, built_in)
VALUES
  (1001, 'WEDDING', '婚礼', 10, TRUE, TRUE),
  (1002, 'FUNERAL', '白事', 20, TRUE, TRUE),
  (1003, 'BIRTHDAY', '生日', 30, TRUE, TRUE),
  (1004, 'FULL_MONTH', '满月', 40, TRUE, TRUE),
  (1005, 'HOUSEWARMING', '乔迁', 50, TRUE, TRUE),
  (1006, 'NEW_YEAR', '拜年', 60, TRUE, TRUE),
  (1999, 'OTHER', '其他', 999, TRUE, TRUE)
ON CONFLICT (code) DO UPDATE
SET name = EXCLUDED.name,
    sort_order = EXCLUDED.sort_order,
    enabled = EXCLUDED.enabled,
    built_in = EXCLUDED.built_in,
    updated_at = NOW();

COMMIT;

