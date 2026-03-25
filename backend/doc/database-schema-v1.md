# 人情往来记账系统数据库表结构设计 V1.0

## 1. 设计原则
1. 主键统一由应用层生成，数据库不使用自增主键。
2. 当前版本按单用户模型设计，表结构中不额外引入 `user_id`。
3. 事件采用公共事件库，人情记录必须关联事件。
4. 闭环关系单独建表保存，避免把双向匹配关系硬编码到记录表中导致后续维护困难。
5. 当前版本不设计物理删除能力，事件和人情记录均保留历史数据。

## 2. 表清单
1. `rl_contact`：联系人表
2. `rl_event_type`：事件类型字典表
3. `rl_event`：公共事件表
4. `rl_gift_record`：人情记录表
5. `rl_reciprocity_match`：闭环匹配表

## 3. 表结构说明

### 3.1 rl_contact
用途：保存人情往来联系人基础信息。

核心字段：
1. `id`：主键
2. `contact_name`：联系人姓名
3. `alias_name`：别名
4. `salutation`：称呼
5. `mobile`：手机号
6. `relation_type`：关系类型
7. `remark`：备注
8. `status`：状态，`NORMAL` / `DISABLED`
9. `create_time`
10. `update_time`

索引建议：
1. 姓名索引
2. 手机号索引
3. 关系类型索引

### 3.2 rl_event_type
用途：维护事件类型预置字典。

核心字段：
1. `id`：主键
2. `type_code`：业务唯一编码，如 `WEDDING`
3. `type_name`：类型名称，如 `结婚`
4. `sort_no`：排序号
5. `enabled_flag`：是否启用
6. `built_in_flag`：是否系统内置
7. `remark`：备注
8. `create_time`
9. `update_time`

说明：
1. `type_code` 建议做唯一约束。
2. 常见预置值包括：`WEDDING`、`BIRTH`、`FULL_MONTH`、`FIRST_BIRTHDAY`、`HOUSEWARMING`、`BIRTHDAY`、`FUNERAL`、`ENTRANCE`、`FESTIVAL`、`OTHER`。

### 3.3 rl_event
用途：保存公共事件库信息。

核心字段：
1. `id`：主键
2. `event_name`：事件名称
3. `event_type_id`：事件类型主键
4. `event_owner_type`：事件归属类型，`SELF` / `CONTACT`
5. `owner_contact_id`：当归属为联系人事件时，记录对应联系人主键
6. `event_date`：事件日期
7. `remark`：备注
8. `status`：状态，`NORMAL` / `DISABLED`
9. `create_time`
10. `update_time`

设计原因：
1. `event_owner_type` 用于区分“我的事件”和“联系人的事件”，这是闭环识别的关键依据。
2. 该字段属于事件本身属性，不要求在新增人情记录时重复选择。

索引建议：
1. 事件日期索引
2. 事件类型索引
3. 事件归属类型 + 联系人索引

### 3.4 rl_gift_record
用途：保存每一笔收礼或送礼的人情记录。

核心字段：
1. `id`：主键
2. `contact_id`：联系人主键
3. `event_id`：事件主键
4. `direction`：收送方向，`RECEIVE` / `SEND`
5. `amount`：金额
6. `record_date`：记录日期
7. `remark`：备注
8. `reciprocity_status`：闭环状态，`UNMATCHED` / `MATCHED` / `MANUAL_CANCELED` / `MANUAL_CONFIRMED`
9. `create_time`
10. `update_time`

设计原因：
1. `direction` 统一从本人视角定义，避免查询和统计时出现口径不一致。
2. 闭环状态落在记录表上，方便列表和时间线直接查询。
3. 实际闭环关系由 `rl_reciprocity_match` 维护，记录表中的状态用于快速展示。

索引建议：
1. 联系人 + 记录日期索引
2. 事件索引
3. 收送方向 + 记录日期索引
4. 闭环状态索引

### 3.5 rl_reciprocity_match
用途：保存两条人情记录之间的闭环匹配关系。

核心字段：
1. `id`：主键
2. `source_record_id`：源记录主键
3. `target_record_id`：目标记录主键
4. `match_type`：匹配类型，`AUTO` / `MANUAL`
5. `match_status`：匹配状态，`ACTIVE` / `CANCELED`
6. `cancel_reason`：取消原因
7. `remark`：备注
8. `create_time`
9. `update_time`

设计原因：
1. 单独建表可以保留自动匹配、人工确认、人工取消的完整轨迹。
2. 后续如果闭环算法升级，不需要改动历史记录表主结构。

约束建议：
1. `source_record_id` 与 `target_record_id` 不能相同。
2. 一个激活中的闭环匹配只允许绑定一对记录。
3. 取消匹配时，需要同步回写两条记录的 `reciprocity_status`。

## 4. 关系说明
1. 一个联系人可以关联多个事件。
2. 一个事件可以关联多条人情记录。
3. 一条人情记录只能属于一个联系人、一个事件。
4. 一条人情记录最多参与一个有效闭环匹配。

## 5. 关键约束建议
1. `rl_event.event_owner_type = CONTACT` 时，`owner_contact_id` 必填。
2. `rl_event.event_owner_type = SELF` 时，`owner_contact_id` 为空。
3. `rl_gift_record.amount` 必须大于 0。
4. `rl_reciprocity_match` 中源记录和目标记录必须满足“同一联系人、同一事件类型、方向相反”。
5. 事件和记录不提供删除能力，只允许编辑。

## 6. 查询与统计映射
1. 联系人明细、联系人时间线：主查 `rl_gift_record`，关联 `rl_contact`、`rl_event`、`rl_event_type`。
2. 事件明细：主查 `rl_event`，关联 `rl_gift_record`。
3. 闭环列表：主查 `rl_gift_record`，必要时关联 `rl_reciprocity_match`。
4. 回礼历史参考：按 `contact_id + event_type_id` 维度聚合 `rl_gift_record`。
5. 统计总览：对 `rl_gift_record` 按时间范围聚合收礼和送礼金额。

## 7. 推荐建表顺序
1. `rl_contact`
2. `rl_event_type`
3. `rl_event`
4. `rl_gift_record`
5. `rl_reciprocity_match`
