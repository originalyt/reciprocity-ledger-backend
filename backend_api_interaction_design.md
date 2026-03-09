# 礼账 App 后端 API 设计（按交互逻辑整理）

## 1. 文档目标

本文档按“页面交互逻辑 + 用户动作”来整理后端接口，而不是按数据库表做 CRUD 拆分。

适用目标：
- 为前后端协作提供统一接口视图
- 让接口设计直接服务于页面交互
- 把业务规则、实现细节、数据落点提前说清楚

设计原则：
- 一个接口尽量对应一个明确的交互目的
- 页面首屏优先使用聚合接口，避免前端自行拼装多个小接口
- 数据库表结构留在 service / repository 层处理，不直接暴露表级心智
- 所有业务默认带 `userId` 做用户隔离

---

## 2. 统一接口规范建议

### 2.1 统一返回结构
建议统一返回结构：

```json
{
  "code": 0,
  "message": "success",
  "data": {}
}
```

说明：
- `code = 0` 表示成功
- 非 0 表示业务异常或系统异常
- `message` 用于用户提示或调试
- `data` 为具体业务数据

### 2.2 分页结构
列表接口统一返回：

```json
{
  "list": [],
  "pageNo": 1,
  "pageSize": 20,
  "total": 100,
  "hasMore": true
}
```

### 2.3 常见业务错误码建议
- `CONTACT_NOT_FOUND`
- `RECORD_NOT_FOUND`
- `EVENT_EXCHANGE_NOT_FOUND`
- `DUPLICATE_RECORD`
- `INVALID_AMOUNT`
- `INVALID_DATE`
- `INVALID_ARGUMENT`
- `TOKEN_EXPIRED`
- `UNAUTHORIZED`
- `FORBIDDEN`

### 2.4 幂等建议
以下接口建议支持 `clientRequestId` 或请求头幂等键：
- `POST /records`
- `POST /auth/login`
- `POST /reciprocity/events/{id}/exempt`
- `POST /reciprocity/events/{id}/cancel-exempt`

---

## 3. 交互视角的接口分组

按客户端交互逻辑，后端接口建议分为：
- 认证与会话
- 首页总览
- 联系人管理
- 记录录入与编辑
- 时间线查询
- 回礼识别与无需回礼
- 统计分析
- 字典与配置
- 导入导出（后续）

---

## 4. 认证与会话

### 4.1 手机号验证码登录

- **接口**：`POST /auth/login`
- **页面/动作**：登录页提交手机号 + 验证码
- **交互目的**：建立登录态，返回用户身份和 token

**请求参数**

```json
{
  "phone": "13800000000",
  "smsCode": "123456",
  "clientRequestId": "login-20260306-001"
}
```

**返回数据**

```json
{
  "accessToken": "...",
  "refreshToken": "...",
  "expiresIn": 7200,
  "userInfo": {
    "userId": 1,
    "phone": "13800000000",
    "nickname": "小礼账"
  }
}
```

**业务规则**
- 手机号格式合法
- 验证码校验通过且未过期
- 验证码错误次数和发送频率受限
- 用户禁用状态下不可登录

**实现细节**
- 查询 `app_user`
- 若用户不存在，可按产品策略自动注册或返回“未注册”
- 登录成功后更新 `last_login_at`
- 生成 `accessToken` 与 `refreshToken`
- `refreshToken` 建议落库便于失效控制

### 4.2 刷新 token

- **接口**：`POST /auth/refresh`
- **页面/动作**：客户端 access token 过期后自动刷新
- **交互目的**：换取新的 access token

**请求参数**

```json
{
  "refreshToken": "..."
}
```

**返回数据**

```json
{
  "accessToken": "...",
  "expiresIn": 7200
}
```

**实现细节**
- 校验 `refreshToken` 是否存在、未过期、未撤销
- 通过后返回新的 `accessToken`
- 如需要，也可轮换新的 `refreshToken`

### 4.3 退出登录

- **接口**：`POST /auth/logout`
- **页面/动作**：设置页点击退出登录
- **交互目的**：让当前 refresh token 失效

**请求参数**

```json
{
  "refreshToken": "..."
}
```

**实现细节**
- 将 refresh token 标记失效
- 客户端清理本地 token

---

## 5. 首页总览

### 5.1 首页聚合数据

- **接口**：`GET /home/overview`
- **页面/动作**：首页首屏加载
- **交互目的**：一次性返回首页展示所需核心数据

**请求参数**
- `dateRangeType`：`THIS_YEAR / THIS_MONTH / CUSTOM`
- `startDate`：可选
- `endDate`：可选

**返回数据**

```json
{
  "summary": {
    "totalGiveAmount": 3200.00,
    "totalReceiveAmount": 5600.00,
    "netAmount": 2400.00
  },
  "pending": {
    "waitMeCount": 3,
    "waitOtherCount": 5,
    "noNeedCount": 2
  },
  "recentRecords": [],
  "topContacts": []
}
```

**业务规则**
- 统计口径默认按当前自然年
- `waitMeCount` 默认不包含“无需回礼”
- 最近记录建议按 `occurredOn DESC`

**实现细节**
- `summary` 来自 `ledger_record`
- `pending` 来自 `event_exchange`
- `recentRecords` 建议直接从时间线明细聚合返回
- `topContacts` 可按总金额降序取前 N 名

### 5.2 首页待办摘要（可选拆分）

- **接口**：`GET /home/pending-summary`
- **页面/动作**：首页待办区刷新
- **交互目的**：轻量拉取待办摘要，适合首页局部刷新

---

## 6. 联系人管理

### 6.1 联系人列表

- **接口**：`GET /contacts`
- **页面/动作**：联系人列表页 / 新增记录页选择联系人
- **交互目的**：支持查看、搜索、筛选、选择联系人

**请求参数**
- `keyword`
- `relation`
- `status`
- `pageNo`
- `pageSize`
- `sort`：如 `lastInteractionDesc`

**返回数据**

```json
{
  "list": [
    {
      "id": 1,
      "name": "张三",
      "relation": "表弟",
      "phone": "138****0000",
      "lastInteractionOn": "2026-03-01"
    }
  ],
  "pageNo": 1,
  "pageSize": 20,
  "total": 1,
  "hasMore": false
}
```

**业务规则**
- 默认按最近往来时间倒序
- 支持姓名、手机号模糊搜索
- 归档联系人默认可不展示

**实现细节**
- 查询 `contact`
- 排序优先 `last_interaction_on DESC`
- 若有 `search_key`，优先使用标准化搜索字段

### 6.2 新增联系人

- **接口**：`POST /contacts`
- **页面/动作**：联系人页新增 / 记录页现场新增联系人
- **交互目的**：创建联系人主数据

**请求参数**

```json
{
  "name": "张三",
  "relation": "表弟",
  "phone": "13800000000",
  "note": "老家亲属"
}
```

**业务规则**
- 姓名必填
- 手机号如填写需校验格式
- 可做弱去重提醒，但不一定强拦截

**实现细节**
- 写入 `contact`
- 姓名建议做 `trim`
- 手机号建议做规范化存储

### 6.3 编辑联系人

- **接口**：`PUT /contacts/{id}`
- **页面/动作**：联系人详情页编辑
- **交互目的**：更新联系人信息

**实现细节**
- 更新 `contact`
- 不影响历史记录归属
- 建议写审计日志

### 6.4 联系人详情

- **接口**：`GET /contacts/{id}`
- **页面/动作**：联系人详情页首屏加载
- **交互目的**：返回联系人基础信息和汇总统计

**返回数据**

```json
{
  "contact": {
    "id": 1,
    "name": "张三",
    "relation": "表弟",
    "phone": "138****0000",
    "note": "老家亲属"
  },
  "summary": {
    "totalGiveAmount": 1200.00,
    "totalReceiveAmount": 800.00,
    "netAmount": -400.00,
    "lastInteractionOn": "2026-03-01"
  },
  "recentEvents": []
}
```

**实现细节**
- `contact` 查询主表
- `summary` 由 `ledger_record + event_exchange` 聚合
- `recentEvents` 可返回最近几条往来摘要

### 6.5 联系人归档

- **接口**：`POST /contacts/{id}/archive`
- **页面/动作**：联系人详情页归档/恢复
- **交互目的**：隐藏不常用联系人，不影响历史数据

**请求参数**

```json
{
  "archived": true
}
```

### 6.6 联系人合并（后续）

- **接口**：`POST /contacts/merge`
- **页面/动作**：重复联系人治理
- **交互目的**：将重复联系人合并

**实现细节**
- 需要迁移 `ledger_record` 与 `event_exchange` 归属
- 迁移后需要重算聚合
- 建议保留合并日志

---

## 7. 记录录入与编辑

### 7.1 新增记录

- **接口**：`POST /records`
- **页面/动作**：新增记录页点击保存
- **交互目的**：新增一笔随礼或收礼，并自动更新事件级聚合

**请求参数**

```json
{
  "contactId": 1,
  "recordType": "GIVE",
  "eventTypeCode": "WEDDING",
  "eventNote": "表弟结婚",
  "occurredOn": "2026-03-01",
  "amount": 500.00,
  "remark": "现场随礼",
  "clientRequestId": "record-20260306-001"
}
```

**返回数据**

```json
{
  "recordId": 1001,
  "eventExchangeId": 2001,
  "reciprocityStatus": "WAIT_OTHER",
  "latestOccurredOn": "2026-03-01",
  "snapshot": {
    "giveAmount": 500.00,
    "receiveAmount": 0.00,
    "netAmount": -500.00
  }
}
```

**业务规则**
- 联系人、记录类型、事件类型、发生日期、金额必填
- 金额必须大于 0，最多两位小数
- 事件说明为空时按空串处理
- 按 `联系人 + 事件类型 + 事件说明 + 记录类型` 去重

**实现细节**
- 在 service 层统一做输入标准化
- 先定位或创建 `event_exchange`
- 插入 `ledger_record`
- 回写 `event_exchange.give/receive_amount`、`latest_occurred_on`、`reciprocity_status`
- 更新 `contact.last_interaction_on`
- 全流程建议放在一个事务中完成

### 7.2 编辑记录

- **接口**：`PUT /records/{id}`
- **页面/动作**：编辑记录页点击保存
- **交互目的**：修改记录，并自动重算受影响聚合

**请求参数**
- 与新增记录基本一致

**业务规则**
- 修改后需要重新校验去重
- 若修改了联系人、事件类型、事件说明，可能会切换到新的聚合

**实现细节**
- 先读取旧记录及旧聚合
- 判断是否跨 `event_exchange`
- 若跨聚合：旧聚合重算 + 新聚合重算
- 若未跨聚合：当前聚合重算
- 建议记录操作日志

### 7.3 查询记录详情

- **接口**：`GET /records/{id}`
- **页面/动作**：编辑页回填
- **交互目的**：返回表单初始化数据

### 7.4 作废记录（建议）

- **接口**：`POST /records/{id}/void`
- **页面/动作**：记录纠错
- **交互目的**：在不物理删除的前提下让记录退出业务统计

**请求参数**

```json
{
  "reason": "录错金额"
}
```

**实现细节**
- 建议将记录标记为 `VOID`
- 作废后重算关联聚合
- 默认不参与统计与时间线
- 建议保留恢复能力

---

## 8. 时间线查询

### 8.1 时间线列表

- **接口**：`GET /timeline`
- **页面/动作**：时间线页加载 / 切换筛选条件
- **交互目的**：返回符合当前筛选口径的记录列表

**请求参数**
- `recordType`：`ALL / GIVE / RECEIVE`
- `contactId`
- `relation`
- `eventTypeCode`
- `keyword`
- `startDate`
- `endDate`
- `pageNo`
- `pageSize`
- `sort`

**返回数据**

```json
{
  "summary": {
    "totalGiveAmount": 1200.00,
    "totalReceiveAmount": 800.00,
    "netAmount": -400.00,
    "recordCount": 6
  },
  "page": {
    "list": [],
    "pageNo": 1,
    "pageSize": 20,
    "total": 6,
    "hasMore": false
  }
}
```

**业务规则**
- 统计口径基于当前筛选结果
- 混合时间线需要返回净额
- 默认按 `occurredOn DESC, id DESC`

**实现细节**
- 主查询从 `ledger_record` 出发
- 联合 `event_exchange`、`contact`、`event_type_dict`
- `summary` 与 `page.list` 使用同一筛选条件

### 8.2 时间线摘要（可选）

- **接口**：`GET /timeline/summary`
- **页面/动作**：筛选条件变化后局部刷新摘要
- **交互目的**：减少列表与摘要重复查询时的耦合

---

## 9. 回礼识别与无需回礼

### 9.1 回礼清单

- **接口**：`GET /reciprocity/events`
- **页面/动作**：回礼清单页加载 / 切换 tab / 切换筛选
- **交互目的**：返回“我待回礼 / 待对方回礼 / 已互回 / 无需回礼”事件列表

**请求参数**
- `status`：`WAIT_ME / WAIT_OTHER / MUTUAL / NO_NEED / ALL`
- `relation`
- `keyword`
- `startDate`
- `endDate`
- `sort`
- `pageNo`
- `pageSize`

**返回数据**

```json
{
  "counts": {
    "waitMe": 3,
    "waitOther": 5,
    "mutual": 8,
    "noNeed": 2
  },
  "page": {
    "list": [],
    "pageNo": 1,
    "pageSize": 20,
    "total": 3,
    "hasMore": false
  }
}
```

**业务规则**
- 基础配对状态来自 `event_exchange.reciprocity_status`
- “无需回礼”是业务展示状态，不改变原始收/随礼事实
- `waitMe` 默认不包含“无需回礼”

**实现细节**
- 主查询来自 `event_exchange`
- 展示状态需结合 `reciprocity_exempt` 字段判断
- `counts` 与列表最好在同一口径下计算

### 9.2 回礼事件详情

- **接口**：`GET /reciprocity/events/{id}`
- **页面/动作**：点击回礼清单某一项
- **交互目的**：返回“联系人 + 事件”维度的完整详情

**返回数据**

```json
{
  "contact": {
    "id": 1,
    "name": "张三",
    "relation": "表弟"
  },
  "event": {
    "id": 2001,
    "eventTypeCode": "WEDDING",
    "eventTypeName": "婚礼",
    "eventNote": "表弟结婚"
  },
  "status": {
    "baseStatus": "WAIT_ME",
    "displayStatus": "NO_NEED",
    "reciprocityExempt": true,
    "exemptReason": "礼节性往来"
  },
  "records": {
    "giveRecord": null,
    "receiveRecord": {
      "id": 1002,
      "amount": 500.00,
      "occurredOn": "2026-03-01",
      "remark": "表弟红包"
    }
  },
  "summary": {
    "giveAmount": 0.00,
    "receiveAmount": 500.00,
    "netAmount": 500.00,
    "latestOccurredOn": "2026-03-01"
  }
}
```

**实现细节**
- 主体数据来自 `event_exchange`
- 明细记录来自 `ledger_record`
- 可顺带返回联系人摘要与事件类型中文名

### 9.3 设为无需回礼

- **接口**：`POST /reciprocity/events/{id}/exempt`
- **页面/动作**：事件详情页点击“设为无需回礼”
- **交互目的**：让该事件不再计入待办

**请求参数**

```json
{
  "exemptSide": "ME",
  "reason": "礼节性往来",
  "clientRequestId": "exempt-20260306-001"
}
```

**业务规则**
- 设置粒度是“联系人 + 事件”级别
- 只改变展示状态与待办统计口径，不修改原始记录事实
- 建议记录设置时间、设置人、原因

**实现细节**
- 更新 `event_exchange` 的豁免标记字段
- 建议写操作日志
- 返回更新后的展示状态

### 9.4 取消无需回礼

- **接口**：`POST /reciprocity/events/{id}/cancel-exempt`
- **页面/动作**：事件详情页点击“取消无需回礼”
- **交互目的**：恢复按基础状态参与待办统计

**实现细节**
- 清空豁免字段
- 恢复展示状态为 `WAIT_ME / WAIT_OTHER / MUTUAL`

---

## 10. 统计分析

### 10.1 统计总览

- **接口**：`GET /stats/overview`
- **页面/动作**：统计页首屏加载
- **交互目的**：返回统计总览卡片数据

**请求参数**
- `startDate`
- `endDate`

**返回数据**
- `totalGiveAmount`
- `totalReceiveAmount`
- `netAmount`
- `giveCount`
- `receiveCount`

### 10.2 按联系人统计

- **接口**：`GET /stats/by-contact`
- **页面/动作**：按人统计列表
- **交互目的**：展示某时间范围内各联系人的往来统计

**请求参数**
- `startDate`
- `endDate`
- `relation`
- `keyword`
- `sort`
- `pageNo`
- `pageSize`

**实现细节**
- 基于 `ledger_record` 分组聚合
- 补充 `lastInteractionOn`
- 返回净额字段

### 10.3 按事件类型统计

- **接口**：`GET /stats/by-event-type`
- **页面/动作**：事件类型分析
- **交互目的**：看哪类事件往来最多

### 10.4 Top 联系人

- **接口**：`GET /stats/top-contacts`
- **页面/动作**：榜单区查看
- **交互目的**：返回收礼 Top / 随礼 Top / 净流出 Top 等榜单

**请求参数**
- `metric`：`GIVE / RECEIVE / NET_OUT / NET_IN`
- `limit`
- `startDate`
- `endDate`

### 10.5 趋势统计（后续）

- **接口**：`GET /stats/trend`
- **页面/动作**：趋势图表
- **交互目的**：按日/月看往来变化趋势

---

## 11. 字典与基础配置

### 11.1 事件类型字典

- **接口**：`GET /dict/event-types`
- **页面/动作**：新增记录页加载事件类型下拉
- **交互目的**：返回统一事件类型字典

**实现细节**
- 查询 `event_type_dict`
- 只返回启用项
- 按 `sort_order` 排序

### 11.2 关系字典（可选）

- **接口**：`GET /dict/relations`
- **页面/动作**：联系人页、筛选页加载关系选项
- **交互目的**：减少关系口径不一致

---

## 12. 导入导出（后续）

### 12.1 导入联系人
- **接口**：`POST /import/contacts`
- **交互目的**：导入联系人数据

### 12.2 导入记录
- **接口**：`POST /import/records`
- **交互目的**：导入历史礼账记录

### 12.3 导出记录
- **接口**：`POST /export/records`
- **交互目的**：导出当前筛选口径下的数据

说明：
- 建议导入导出采用任务化接口
- 返回任务 ID，前端轮询任务状态

---

## 13. 建议优先实现的 v1 接口集

建议个人开发优先实现：

### P0：核心闭环
- `POST /auth/login`
- `POST /auth/refresh`
- `GET /home/overview`
- `GET /contacts`
- `POST /contacts`
- `PUT /contacts/{id}`
- `GET /contacts/{id}`
- `POST /records`
- `PUT /records/{id}`
- `GET /records/{id}`
- `GET /timeline`
- `GET /reciprocity/events`
- `GET /reciprocity/events/{id}`
- `POST /reciprocity/events/{id}/exempt`
- `POST /reciprocity/events/{id}/cancel-exempt`

### P1：体验增强
- `POST /records/{id}/void`
- `POST /contacts/{id}/archive`
- `GET /stats/overview`
- `GET /stats/by-contact`
- `GET /stats/top-contacts`

### P2：后续增强
- `POST /contacts/merge`
- `GET /stats/by-event-type`
- `GET /stats/trend`
- 导入导出任务接口

---

## 14. 实现层建议

### 14.1 Controller 层
- 按交互分组建立 controller：
  - `AuthController`
  - `HomeController`
  - `ContactController`
  - `RecordController`
  - `TimelineController`
  - `ReciprocityController`
  - `StatsController`

### 14.2 Service 层
- service 层处理业务闭环，不把复杂逻辑塞进 controller
- 特别是以下操作必须在 service 内封装：
  - 新增记录
  - 编辑记录
  - 回礼聚合重算
  - 设置/取消无需回礼
  - 联系人合并

### 14.3 Repository / Mapper 层
- repository 层只负责数据读写，不负责页面交互逻辑
- 复杂聚合查询可单独放 query mapper

### 14.4 事务建议
以下操作建议开启事务：
- `POST /records`
- `PUT /records/{id}`
- `POST /records/{id}/void`
- `POST /reciprocity/events/{id}/exempt`
- `POST /reciprocity/events/{id}/cancel-exempt`
- `POST /contacts/merge`

---

## 15. 总结

这套接口设计的核心思想是：
- 接口围绕用户交互与页面动作组织
- 聚合逻辑留在后端，前端只消费业务结果
- 先保证核心闭环可用，再逐步增加统计、导入导出、联系人治理等增强能力

对于个人开发，这种设计的优势是：
- 页面和接口对应关系清楚
- 后端实现顺序容易拆分
- 前端后续就算样式慢慢打磨，也不会反复改动底层业务接口

后续可以继续补充：
- 请求 DTO / 响应 VO 设计稿
- Controller / Service / Mapper 分层示例
- 数据库字段与 API 字段映射表
- 接口异常码与前端提示文案清单

