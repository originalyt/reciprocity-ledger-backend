# 礼账程序实现计划

## 1. 实施原则

- 以 `event_exchange` 作为“同联系人 + 同事件”的业务聚合根，承接回礼状态、净额、最近日期。
- 以 `ledger_record` 作为交易明细事实表，时间线与统计统一从明细出发。
- 所有联系人、记录、统计查询都必须强制带 `user_id` 做多用户隔离。
- 先遵守当前业务边界：同一事件下每种记录类型最多 1 条记录；后续再扩展“补礼/补收”等更复杂场景。
- 先做稳核心闭环，再补体验增强与数据治理能力。

## 2. 分阶段实施计划

### Phase 1：最小可用闭环

目标：先完成“能录、能改、能查、能看总览”的基础版本。

范围：
- 登录与用户隔离
- 联系人新增、编辑、查询
- 随礼/收礼记录新增与编辑
- 记录去重与字段校验
- 基础时间线
- 首页三大指标：总随礼、总收礼、净额

对应需求：
- 账号与鉴权模块
- 联系人管理模块
- 礼账记录模块
- 记录去重与数据校验模块
- 时间线与筛选模块（基础版）
- 首页总览模块（基础指标）

### Phase 2：核心业务强化

目标：把“同事件回礼识别”做成真正有价值的核心功能。

范围：
- 同事件回礼识别清单
- 待我回礼 / 待对方回礼 / 已互回 / 无需回礼分组
- 联系人详情页
- 搜索能力
- Top 榜单
- 筛选联动
- 回礼清单与联系人、事件详情联动

### Phase 3：体验与治理增强

目标：提升长期可用性、纠错能力和数据迁移能力。

范围：
- 联系人合并、归档
- 记录作废/隐藏
- 编辑历史追溯
- 导入导出
- 备份恢复
- 趋势统计
- 首页待办提醒区

## 3. 后端实现方案

### 3.1 模块划分

建议按领域拆分后端模块：

- `auth`：短信登录、refresh token、会话管理
- `contact`：联系人 CRUD、搜索、合并、归档
- `ledger`：新增/编辑记录、去重、作废、聚合重算
- `timeline`：时间线分页、筛选、分组
- `stats`：首页指标、按人统计、按事件类型统计、趋势统计
- `reciprocity`：回礼清单、待办、联系人+事件详情
- `import_export`：CSV/Excel 导入导出

### 3.2 保留现有核心表

保留以下核心表作为主业务模型：
- `app_user`
- `event_type_dict`
- `contact`
- `event_exchange`
- `ledger_record`

理由：当前 schema 与需求总体一致，尤其是“事件级聚合 + 明细事实表”的设计适合礼账场景。

### 3.3 建议新增的数据表

为了支撑完整业务能力，建议新增以下表：

- `login_verification_code`
  - 存储手机号验证码、过期时间、错误次数、发送频率控制信息。

- `user_refresh_token`
  - 存储 refresh token、设备信息、失效状态、过期时间。

- `operation_log`
  - 存储业务对象修改前后快照、操作人、操作时间、操作类型。

- `contact_merge_log`
  - 存储联系人合并记录，包括源联系人、目标联系人、时间、操作人。

- `import_job`
  - 存储导入任务状态、结果摘要、错误信息。

- `export_job`
  - 存储导出任务状态、文件地址、导出范围。

## 4. 核心业务流程设计

### 4.1 新增记录

前端提交字段：
- `contactId`
- `recordType`
- `eventTypeCode`
- `eventNote`
- `occurredOn`
- `amount`
- `remark`

后端处理步骤：
1. 参数校验：联系人、记录类型、事件类型、日期、金额必填。
2. 输入标准化：对 `eventNote` 执行 `trim`、空串归一化。
3. 去重校验：按 `contact + eventType + eventNote + recordType + userId` 检查唯一性。
4. 开启事务。
5. 查询或创建对应的 `event_exchange`。
6. 插入 `ledger_record`。
7. 回写 `event_exchange` 的金额、记录 ID、最近发生日期、回礼状态。
8. 更新 `contact.last_interaction_on`。
9. 提交事务并返回聚合结果。

### 4.2 编辑记录

编辑时要特别处理“联系人、事件类型、事件说明被修改”的场景。

建议实现方式：
1. 读取旧记录与旧聚合。
2. 判断修改后是否仍属于同一个 `event_exchange`。
3. 如果变更了聚合归属：
   - 旧聚合执行一次重算。
   - 新聚合执行一次重算。
4. 如果未变更聚合归属：
   - 更新记录。
   - 当前聚合执行一次重算。
5. 所有步骤在同一事务内完成。

### 4.3 作废记录

为满足“禁止删除但允许纠错”的业务要求，建议：
- 在 `ledger_record` 中增加 `record_status` 字段，取值如 `ACTIVE / VOID`。
- 增加 `void_reason` 字段记录作废原因。
- 所有统计、时间线默认仅统计 `ACTIVE` 记录。
- 作废操作写入 `operation_log`。
- 作废后重算对应的 `event_exchange`。

### 4.4 联系人合并

联系人合并建议按事务实现：
1. 用户选择主联系人和被合并联系人。
2. 将被合并联系人的 `ledger_record` / `event_exchange` 归属迁移到主联系人。
3. 对迁移后涉及到的所有聚合执行重算。
4. 记录 `contact_merge_log`。
5. 被合并联系人标记为 `MERGED` 或 `ARCHIVED`。
### 4.5 无需回礼 / 回礼豁免

建议将“无需回礼”设计为事件级业务标记，而不是直接改写原始配对事实。

推荐规则：
1. 基础配对状态仍然按 `WAIT_ME / WAIT_OTHER / MUTUAL` 计算。
2. 当某个“联系人 + 事件”被用户标记为无需回礼时，业务展示层改为显示“无需回礼”。
3. “无需回礼”默认不进入首页待办数量与默认待办清单。
4. 支持取消豁免，取消后恢复基础状态展示。
5. 建议记录豁免原因、设置人、设置时间，便于审计与回溯。

## 5. 查询与统计实现思路

### 5.1 时间线

查询来源：以 `ledger_record` 为主表，关联：
- `event_exchange`
- `contact`
- `event_type_dict`

支持筛选：
- 记录类型
- 联系人
- 关系
- 事件类型
- 时间范围
- 关键字（联系人名、事件说明、备注）

默认排序：
- `occurred_on DESC`
- `id DESC`

### 5.2 联系人详情页

需要输出：
- 联系人基础信息
- 总随礼金额
- 总收礼金额
- 净额
- 最近往来日期
- 分页时间线

### 5.3 回礼清单

直接查询 `event_exchange`：
- 基础状态：`WAIT_ME / WAIT_OTHER / MUTUAL`
- 业务展示状态：在基础状态之上叠加“无需回礼/回礼豁免”标记
- 默认优先展示 `WAIT_ME`
- 支持按最近发生日期、净额、联系人名称排序
- 支持查看“无需回礼”分组，且默认不计入待办数量
- 设置粒度为单个“联系人 + 事件”

### 5.4 统计中心

- 首页指标：对 `ledger_record` 直接聚合
- Top 联系人：按 `contact_id + record_type` 分组统计
- 按事件类型统计：按 `event_type_code + record_type` 分组统计
- 趋势统计：按日或月聚合，建议数据库层使用 `date_trunc`

## 6. 前端实现方案（Flutter）

### 6.1 页面结构

建议页面：
- 登录页
- 首页
- 新增/编辑记录页
- 联系人列表页
- 联系人详情页
- 时间线页
- 回礼清单页
- 统计中心页
- 设置页

### 6.2 前端分层建议

- `presentation`：页面、组件、状态展示
- `application`：用例层，如 `CreateRecordUseCase`
- `domain`：实体、值对象、业务规则
- `infrastructure`：Dio、DTO、缓存、本地存储

### 6.3 状态管理与本地存储

- 每个主页面使用独立 provider 管理状态。
- 筛选条件单独维护 provider。
- 录入草稿使用 provider + `shared_preferences` 持久化。
- 草稿恢复时按页面和记录类型区分。

## 7. 接口设计建议

### 7.1 认证
- `POST /auth/sms/send`
- `POST /auth/login`
- `POST /auth/refresh`
- `POST /auth/logout`

### 7.2 联系人
- `GET /contacts`
- `POST /contacts`
- `PUT /contacts/{id}`
- `POST /contacts/{id}/archive`
- `POST /contacts/merge`
- `GET /contacts/{id}`

### 7.3 礼账记录
- `GET /records`
- `POST /records`
- `PUT /records/{id}`
- `POST /records/{id}/void`

### 7.4 时间线与统计
- `GET /timeline`
- `GET /stats/overview`
- `GET /stats/by-contact`
- `GET /stats/by-event-type`
- `GET /stats/trend`

### 7.5 回礼识别
- `GET /reciprocity/events`
- `GET /reciprocity/events/{id}`
- `POST /reciprocity/events/{id}/exempt`
- `POST /reciprocity/events/{id}/cancel-exempt`

### 7.6 导入导出
- `POST /import/contacts`
- `POST /import/records`
- `GET /export/records`

## 8. 必须重点做稳的技术点

### 8.1 幂等控制
- 新增记录接口建议支持 `idempotencyKey`。
- 防止弱网重试、按钮连点导致重复创建。

### 8.2 输入标准化
- `eventNote` 统一 `trim` 与空串归一。
- 手机号统一规范化。
- 关系字段尽量标准化，避免统计口径分裂。

### 8.3 审计能力
- 仅有 `updated_at` 不够。
- 对联系人编辑、记录编辑、作废、合并等操作需记录完整日志。

### 8.4 聚合一致性
- `event_exchange` 是聚合快照，必须提供：
  - 单聚合重算能力
  - 指定联系人范围重算能力
  - 全量重算能力
- 避免因异常流程导致快照和明细不一致。

### 8.5 业务边界说明
- 当前版本默认不支持“同一事件多次随礼/多次收礼”的复杂场景。
- 该边界需要在产品、开发、测试三方间明确。

## 9. 建议的 schema 增强方向

### 9.1 `contact`
建议增加：
- `status`
- `search_key`
- 可选的 `name_pinyin` / `name_initials`

### 9.2 `ledger_record`
建议增加：
- `record_status`
- `void_reason`
- 可选的 `client_request_id`

### 9.3 `event_exchange`
建议增加：
- `reciprocity_exempt`
- `reciprocity_exempt_side`
- `reciprocity_exempt_reason`
- `reciprocity_exempt_at`
- `reciprocity_exempt_by`

说明：
- 基础配对状态继续保留在 `reciprocity_status` 中。
- “无需回礼”作为业务豁免标记叠加在展示层与待办口径上。

### 9.4 新增辅助表
建议补充：
- `operation_log`
- `user_refresh_token`
- `login_verification_code`
- `contact_merge_log`
- `import_job`
- `export_job`

## 10. 推荐开发顺序

- 第 1 周：认证、用户隔离、联系人基础 CRUD
- 第 2 周：新增/编辑记录、去重、聚合同步
- 第 3 周：时间线、首页指标、联系人详情
- 第 4 周：回礼清单、待我回礼入口、无需回礼设置、Top 榜
- 第 5 周：搜索、联系人合并/归档、作废记录
- 第 6 周：导入导出、趋势统计、审计日志

## 11. 总结

整体方案建议：
- 数据层使用 `event_exchange + ledger_record` 保证业务表达力。
- 服务层通过“事务 + 聚合重算”保证一致性。
- 前端通过“快捷录入 + 联系人详情 + 回礼待办”提升产品体验。
- 项目推进上先做稳核心闭环，再补导入导出、趋势与治理功能。

