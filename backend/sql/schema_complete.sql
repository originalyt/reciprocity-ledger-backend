-- ============================================
-- 人情往来账本系统 - 完整数据库初始化脚本
-- 数据库: PostgreSQL
-- 版本: v2.0 (含用户模块和多租户支持)
-- ============================================

-- ============================================
-- 1. 用户表
-- ============================================
CREATE TABLE IF NOT EXISTS rl_user (
    id VARCHAR(32) PRIMARY KEY,
    email VARCHAR(128) NOT NULL UNIQUE,
    password VARCHAR(128) NOT NULL,
    nickname VARCHAR(32),
    avatar_url VARCHAR(255),
    status VARCHAR(16) NOT NULL DEFAULT 'NORMAL',
    last_login_time TIMESTAMP,
    create_time TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    update_time TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

COMMENT ON TABLE rl_user IS '用户表';
COMMENT ON COLUMN rl_user.id IS '主键，用户唯一标识';
COMMENT ON COLUMN rl_user.email IS '邮箱，用于登录';
COMMENT ON COLUMN rl_user.password IS '密码，BCrypt加密存储';
COMMENT ON COLUMN rl_user.nickname IS '昵称';
COMMENT ON COLUMN rl_user.avatar_url IS '头像URL';
COMMENT ON COLUMN rl_user.status IS '状态，NORMAL表示正常，DISABLED表示停用';
COMMENT ON COLUMN rl_user.last_login_time IS '最后登录时间';
COMMENT ON COLUMN rl_user.create_time IS '创建时间';
COMMENT ON COLUMN rl_user.update_time IS '更新时间';

CREATE INDEX IF NOT EXISTS idx_rl_user_email ON rl_user(email);
CREATE INDEX IF NOT EXISTS idx_rl_user_status ON rl_user(status);

-- ============================================
-- 2. 联系人表
-- ============================================
CREATE TABLE IF NOT EXISTS rl_contact (
    id VARCHAR(32) PRIMARY KEY,
    contact_name VARCHAR(64) NOT NULL,
    alias_name VARCHAR(64),
    salutation VARCHAR(64),
    mobile VARCHAR(20),
    relation_type VARCHAR(32) NOT NULL,
    remark VARCHAR(500),
    status VARCHAR(16) NOT NULL DEFAULT 'NORMAL',
    user_id VARCHAR(32),
    create_time TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    update_time TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

COMMENT ON TABLE rl_contact IS '联系人表';
COMMENT ON COLUMN rl_contact.id IS '主键，联系人唯一标识';
COMMENT ON COLUMN rl_contact.contact_name IS '联系人姓名';
COMMENT ON COLUMN rl_contact.alias_name IS '联系人别名';
COMMENT ON COLUMN rl_contact.salutation IS '联系人称呼';
COMMENT ON COLUMN rl_contact.mobile IS '联系人手机号';
COMMENT ON COLUMN rl_contact.relation_type IS '关系类型';
COMMENT ON COLUMN rl_contact.remark IS '备注';
COMMENT ON COLUMN rl_contact.status IS '状态，NORMAL表示正常，DISABLED表示停用';
COMMENT ON COLUMN rl_contact.user_id IS '所属用户ID，多租户数据隔离';
COMMENT ON COLUMN rl_contact.create_time IS '创建时间';
COMMENT ON COLUMN rl_contact.update_time IS '更新时间';

CREATE INDEX IF NOT EXISTS idx_rl_contact_name ON rl_contact(contact_name);
CREATE INDEX IF NOT EXISTS idx_rl_contact_mobile ON rl_contact(mobile);
CREATE INDEX IF NOT EXISTS idx_rl_contact_relation_type ON rl_contact(relation_type);
CREATE INDEX IF NOT EXISTS idx_rl_contact_user_id ON rl_contact(user_id);

-- ============================================
-- 3. 事件类型字典表
-- ============================================
CREATE TABLE IF NOT EXISTS rl_event_type (
    id VARCHAR(32) PRIMARY KEY,
    type_code VARCHAR(32) NOT NULL,
    type_name VARCHAR(64) NOT NULL,
    sort_no INTEGER NOT NULL DEFAULT 0,
    enabled_flag BOOLEAN NOT NULL DEFAULT TRUE,
    built_in_flag BOOLEAN NOT NULL DEFAULT TRUE,
    remark VARCHAR(500),
    create_time TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    update_time TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT uk_rl_event_type_code UNIQUE (type_code)
);

COMMENT ON TABLE rl_event_type IS '事件类型字典表';
COMMENT ON COLUMN rl_event_type.id IS '主键，事件类型唯一标识';
COMMENT ON COLUMN rl_event_type.type_code IS '业务唯一键，事件类型编码';
COMMENT ON COLUMN rl_event_type.type_name IS '事件类型名称';
COMMENT ON COLUMN rl_event_type.sort_no IS '排序号';
COMMENT ON COLUMN rl_event_type.enabled_flag IS '是否启用，true表示启用';
COMMENT ON COLUMN rl_event_type.built_in_flag IS '是否系统内置，true表示内置类型';
COMMENT ON COLUMN rl_event_type.remark IS '备注';
COMMENT ON COLUMN rl_event_type.create_time IS '创建时间';
COMMENT ON COLUMN rl_event_type.update_time IS '更新时间';

-- ============================================
-- 4. 事件表
-- ============================================
CREATE TABLE IF NOT EXISTS rl_event (
    id VARCHAR(32) PRIMARY KEY,
    event_name VARCHAR(128) NOT NULL,
    event_type_id VARCHAR(32) NOT NULL,
    event_owner_type VARCHAR(16) NOT NULL,
    owner_contact_id VARCHAR(32),
    event_date DATE NOT NULL,
    remark VARCHAR(500),
    status VARCHAR(16) NOT NULL DEFAULT 'NORMAL',
    user_id VARCHAR(32),
    create_time TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    update_time TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_rl_event_type_id FOREIGN KEY (event_type_id) REFERENCES rl_event_type(id),
    CONSTRAINT fk_rl_event_owner_contact_id FOREIGN KEY (owner_contact_id) REFERENCES rl_contact(id),
    CONSTRAINT ck_rl_event_owner_type CHECK (event_owner_type IN ('SELF', 'CONTACT')),
    CONSTRAINT ck_rl_event_owner_contact CHECK (
        (event_owner_type = 'SELF' AND owner_contact_id IS NULL)
        OR (event_owner_type = 'CONTACT' AND owner_contact_id IS NOT NULL)
    )
);

COMMENT ON TABLE rl_event IS '公共事件表';
COMMENT ON COLUMN rl_event.id IS '主键，事件唯一标识';
COMMENT ON COLUMN rl_event.event_name IS '事件名称';
COMMENT ON COLUMN rl_event.event_type_id IS '事件类型主键';
COMMENT ON COLUMN rl_event.event_owner_type IS '事件归属类型，SELF表示本人事件，CONTACT表示联系人事件';
COMMENT ON COLUMN rl_event.owner_contact_id IS '联系人事件对应的联系人主键';
COMMENT ON COLUMN rl_event.event_date IS '事件日期';
COMMENT ON COLUMN rl_event.remark IS '备注';
COMMENT ON COLUMN rl_event.status IS '状态，NORMAL表示正常，DISABLED表示停用';
COMMENT ON COLUMN rl_event.user_id IS '所属用户ID，多租户数据隔离';
COMMENT ON COLUMN rl_event.create_time IS '创建时间';
COMMENT ON COLUMN rl_event.update_time IS '更新时间';

CREATE INDEX IF NOT EXISTS idx_rl_event_type_id ON rl_event(event_type_id);
CREATE INDEX IF NOT EXISTS idx_rl_event_date ON rl_event(event_date);
CREATE INDEX IF NOT EXISTS idx_rl_event_owner ON rl_event(event_owner_type, owner_contact_id);
CREATE INDEX IF NOT EXISTS idx_rl_event_user_id ON rl_event(user_id);

-- ============================================
-- 5. 人情记录表
-- ============================================
CREATE TABLE IF NOT EXISTS rl_gift_record (
    id VARCHAR(32) PRIMARY KEY,
    contact_id VARCHAR(32) NOT NULL,
    event_id VARCHAR(32) NOT NULL,
    direction VARCHAR(16) NOT NULL,
    amount NUMERIC(12, 2) NOT NULL,
    record_date DATE NOT NULL,
    remark VARCHAR(500),
    reciprocity_status VARCHAR(32) NOT NULL DEFAULT 'UNMATCHED',
    user_id VARCHAR(32),
    create_time TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    update_time TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_rl_gift_record_contact_id FOREIGN KEY (contact_id) REFERENCES rl_contact(id),
    CONSTRAINT fk_rl_gift_record_event_id FOREIGN KEY (event_id) REFERENCES rl_event(id),
    CONSTRAINT ck_rl_gift_record_direction CHECK (direction IN ('RECEIVE', 'SEND')),
    CONSTRAINT ck_rl_gift_record_amount CHECK (amount > 0),
    CONSTRAINT ck_rl_gift_record_reciprocity_status CHECK (reciprocity_status IN ('UNMATCHED', 'MATCHED', 'MANUAL_CANCELED', 'MANUAL_CONFIRMED'))
);

COMMENT ON TABLE rl_gift_record IS '人情记录表';
COMMENT ON COLUMN rl_gift_record.id IS '主键，人情记录唯一标识';
COMMENT ON COLUMN rl_gift_record.contact_id IS '联系人主键';
COMMENT ON COLUMN rl_gift_record.event_id IS '事件主键';
COMMENT ON COLUMN rl_gift_record.direction IS '收送方向，RECEIVE表示收礼，SEND表示送礼';
COMMENT ON COLUMN rl_gift_record.amount IS '金额';
COMMENT ON COLUMN rl_gift_record.record_date IS '记录日期';
COMMENT ON COLUMN rl_gift_record.remark IS '备注';
COMMENT ON COLUMN rl_gift_record.reciprocity_status IS '闭环状态，UNMATCHED表示未闭环，MATCHED表示自动匹配闭环，MANUAL_CANCELED表示手工取消闭环，MANUAL_CONFIRMED表示手工确认闭环';
COMMENT ON COLUMN rl_gift_record.user_id IS '所属用户ID，多租户数据隔离';
COMMENT ON COLUMN rl_gift_record.create_time IS '创建时间';
COMMENT ON COLUMN rl_gift_record.update_time IS '更新时间';

CREATE INDEX IF NOT EXISTS idx_rl_gift_record_contact_date ON rl_gift_record(contact_id, record_date);
CREATE INDEX IF NOT EXISTS idx_rl_gift_record_event_id ON rl_gift_record(event_id);
CREATE INDEX IF NOT EXISTS idx_rl_gift_record_direction_date ON rl_gift_record(direction, record_date);
CREATE INDEX IF NOT EXISTS idx_rl_gift_record_reciprocity_status ON rl_gift_record(reciprocity_status);
CREATE INDEX IF NOT EXISTS idx_rl_gift_record_user_id ON rl_gift_record(user_id);

-- ============================================
-- 6. 闭环匹配表
-- ============================================
CREATE TABLE IF NOT EXISTS rl_reciprocity_match (
    id VARCHAR(32) PRIMARY KEY,
    source_record_id VARCHAR(32) NOT NULL,
    target_record_id VARCHAR(32) NOT NULL,
    match_type VARCHAR(16) NOT NULL,
    match_status VARCHAR(16) NOT NULL DEFAULT 'ACTIVE',
    cancel_reason VARCHAR(500),
    remark VARCHAR(500),
    user_id VARCHAR(32),
    create_time TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    update_time TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_rl_reciprocity_match_source_record_id FOREIGN KEY (source_record_id) REFERENCES rl_gift_record(id),
    CONSTRAINT fk_rl_reciprocity_match_target_record_id FOREIGN KEY (target_record_id) REFERENCES rl_gift_record(id),
    CONSTRAINT ck_rl_reciprocity_match_not_same CHECK (source_record_id <> target_record_id),
    CONSTRAINT ck_rl_reciprocity_match_type CHECK (match_type IN ('AUTO', 'MANUAL')),
    CONSTRAINT ck_rl_reciprocity_match_status CHECK (match_status IN ('ACTIVE', 'CANCELED'))
);

COMMENT ON TABLE rl_reciprocity_match IS '闭环匹配表';
COMMENT ON COLUMN rl_reciprocity_match.id IS '主键，闭环匹配唯一标识';
COMMENT ON COLUMN rl_reciprocity_match.source_record_id IS '源记录主键';
COMMENT ON COLUMN rl_reciprocity_match.target_record_id IS '目标记录主键';
COMMENT ON COLUMN rl_reciprocity_match.match_type IS '匹配类型，AUTO表示自动识别，MANUAL表示人工确认';
COMMENT ON COLUMN rl_reciprocity_match.match_status IS '匹配状态，ACTIVE表示有效，CANCELED表示已取消';
COMMENT ON COLUMN rl_reciprocity_match.cancel_reason IS '取消原因';
COMMENT ON COLUMN rl_reciprocity_match.remark IS '备注';
COMMENT ON COLUMN rl_reciprocity_match.user_id IS '所属用户ID，多租户数据隔离';
COMMENT ON COLUMN rl_reciprocity_match.create_time IS '创建时间';
COMMENT ON COLUMN rl_reciprocity_match.update_time IS '更新时间';

CREATE UNIQUE INDEX IF NOT EXISTS uk_rl_reciprocity_match_source_active
    ON rl_reciprocity_match(source_record_id)
    WHERE match_status = 'ACTIVE';

CREATE UNIQUE INDEX IF NOT EXISTS uk_rl_reciprocity_match_target_active
    ON rl_reciprocity_match(target_record_id)
    WHERE match_status = 'ACTIVE';

CREATE INDEX IF NOT EXISTS idx_rl_reciprocity_match_user_id ON rl_reciprocity_match(user_id);

-- ============================================
-- 7. 事件关联表
-- ============================================
CREATE TABLE IF NOT EXISTS rl_event_relation (
    id VARCHAR(32) PRIMARY KEY,
    self_event_id VARCHAR(32) NOT NULL,
    contact_event_id VARCHAR(32) NOT NULL,
    user_id VARCHAR(32) NOT NULL,
    create_time TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    update_time TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_rl_event_relation_self_event_id FOREIGN KEY (self_event_id) REFERENCES rl_event(id),
    CONSTRAINT fk_rl_event_relation_contact_event_id FOREIGN KEY (contact_event_id) REFERENCES rl_event(id),
    CONSTRAINT ck_rl_event_relation_not_same CHECK (self_event_id <> contact_event_id)
);

COMMENT ON TABLE rl_event_relation IS '事件关联表';
COMMENT ON COLUMN rl_event_relation.id IS '主键，关联唯一标识';
COMMENT ON COLUMN rl_event_relation.self_event_id IS '本人事件ID（SELF类型）';
COMMENT ON COLUMN rl_event_relation.contact_event_id IS '联系人事件ID（CONTACT类型）';
COMMENT ON COLUMN rl_event_relation.user_id IS '所属用户ID';
COMMENT ON COLUMN rl_event_relation.create_time IS '创建时间';
COMMENT ON COLUMN rl_event_relation.update_time IS '更新时间';

CREATE INDEX IF NOT EXISTS idx_rl_event_relation_user_id ON rl_event_relation(user_id);
CREATE INDEX IF NOT EXISTS idx_rl_event_relation_self_event_id ON rl_event_relation(self_event_id);
CREATE INDEX IF NOT EXISTS idx_rl_event_relation_contact_event_id ON rl_event_relation(contact_event_id);

-- ============================================
-- 8. 初始化事件类型数据
-- ============================================
INSERT INTO rl_event_type (id, type_code, type_name, sort_no, enabled_flag, built_in_flag, remark)
VALUES
    ('1001', 'WEDDING', '结婚', 10, TRUE, TRUE, '系统预置事件类型'),
    ('1002', 'BIRTH', '生子', 20, TRUE, TRUE, '系统预置事件类型'),
    ('1003', 'FULL_MONTH', '满月', 30, TRUE, TRUE, '系统预置事件类型'),
    ('1004', 'FIRST_BIRTHDAY', '周岁', 40, TRUE, TRUE, '系统预置事件类型'),
    ('1005', 'HOUSEWARMING', '乔迁', 50, TRUE, TRUE, '系统预置事件类型'),
    ('1006', 'BIRTHDAY', '生日', 60, TRUE, TRUE, '系统预置事件类型'),
    ('1007', 'FUNERAL', '白事', 70, TRUE, TRUE, '系统预置事件类型'),
    ('1008', 'ENTRANCE', '升学', 80, TRUE, TRUE, '系统预置事件类型'),
    ('1009', 'FESTIVAL', '节日往来', 90, TRUE, TRUE, '系统预置事件类型'),
    ('1010', 'OTHER', '其他', 100, TRUE, TRUE, '系统预置事件类型')
ON CONFLICT (type_code) DO NOTHING;
