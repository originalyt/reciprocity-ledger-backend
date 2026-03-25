# 人情往来记账系统接口清单 V1.0

## 1. 设计约束
1. 所有接口统一使用 `POST`。
2. 所有入参统一使用 `RequestBody`，即使只有一个字段也封装为请求对象。
3. 所有出参统一使用对象包装，外层统一为 `ApiResponse<T>`。
4. 当前版本只覆盖 APP 端核心业务接口，不包含登录注册。
5. 当前版本不提供删除接口，联系人、事件、人情记录均通过新增、编辑、详情、分页查询完成业务闭环。

## 2. 统一返回结构
```json
{
  "code": "0",
  "message": "success",
  "data": {}
}
```

分页数据建议统一结构：
```json
{
  "list": [],
  "pageNo": 1,
  "pageSize": 20,
  "total": 0
}
```

## 3. 接口分组
1. 首页接口
2. 联系人接口
3. 事件接口
4. 人情记录接口
5. 闭环与回礼参考接口
6. 统计接口
7. 字典接口

## 4. 首页接口

### 4.1 首页概览
- 接口：`POST /app/home/overview`
- 用途：APP 首页首屏加载
- 请求对象：`HomeOverviewRequest`
- 请求字段：
  - `startDate`：开始日期，非必填
  - `endDate`：结束日期，非必填
- 返回对象：`HomeOverviewResponse`
- 返回字段：
  - `receiveTotalAmount`：收礼总额
  - `sendTotalAmount`：送礼总额
  - `pendingReciprocityCount`：待处理闭环数量
  - `recentRecordList`：最近记录列表
  - `recentEventList`：最近事件列表

## 5. 联系人接口

### 5.1 联系人分页查询
- 接口：`POST /app/contact/page`
- 用途：联系人列表页、选择联系人弹窗
- 请求对象：`ContactPageRequest`
- 请求字段：
  - `keyword`：姓名、别名、手机号模糊查询
  - `relationType`：关系类型
  - `pageNo`
  - `pageSize`
- 返回对象：`PageResponse<ContactPageItemResponse>`

### 5.2 联系人详情
- 接口：`POST /app/contact/detail`
- 用途：联系人详情页加载
- 请求对象：`ContactDetailRequest`
- 请求字段：
  - `contactId`
- 返回对象：`ContactDetailResponse`
- 返回字段：
  - `contactInfo`：联系人基础信息
  - `summaryInfo`：收送汇总
  - `unclosedReciprocityCount`：未闭环数量

### 5.3 新增联系人
- 接口：`POST /app/contact/save`
- 用途：新增联系人
- 请求对象：`ContactSaveRequest`
- 请求字段：
  - `contactName`
  - `aliasName`
  - `salutation`
  - `mobile`
  - `relationType`
  - `remark`
- 返回对象：`IdResponse`

### 5.4 编辑联系人
- 接口：`POST /app/contact/update`
- 用途：编辑联系人
- 请求对象：`ContactUpdateRequest`
- 请求字段：
  - `contactId`
  - `contactName`
  - `aliasName`
  - `salutation`
  - `mobile`
  - `relationType`
  - `remark`
- 返回对象：`IdResponse`

## 6. 事件接口

### 6.1 事件分页查询
- 接口：`POST /app/event/page`
- 用途：事件列表页、录入记录时选择事件
- 请求对象：`EventPageRequest`
- 请求字段：
  - `keyword`：事件名称模糊查询
  - `eventTypeCode`：事件类型编码
  - `eventOwnerType`：事件归属类型，`SELF` / `CONTACT`
  - `ownerContactId`：联系人事件时可筛选联系人
  - `startDate`
  - `endDate`
  - `pageNo`
  - `pageSize`
- 返回对象：`PageResponse<EventPageItemResponse>`

### 6.2 事件详情
- 接口：`POST /app/event/detail`
- 用途：事件详情页加载
- 请求对象：`EventDetailRequest`
- 请求字段：
  - `eventId`
- 返回对象：`EventDetailResponse`
- 返回字段：
  - `eventInfo`
  - `recordSummary`
  - `contactCount`

### 6.3 新增事件
- 接口：`POST /app/event/save`
- 用途：新增公共事件
- 请求对象：`EventSaveRequest`
- 请求字段：
  - `eventName`
  - `eventTypeCode`
  - `eventOwnerType`：`SELF` / `CONTACT`
  - `ownerContactId`：当事件归属为联系人事件时必填
  - `eventDate`
  - `remark`
- 返回对象：`IdResponse`

### 6.4 编辑事件
- 接口：`POST /app/event/update`
- 用途：编辑事件
- 请求对象：`EventUpdateRequest`
- 请求字段：
  - `eventId`
  - `eventName`
  - `eventTypeCode`
  - `eventOwnerType`
  - `ownerContactId`
  - `eventDate`
  - `remark`
- 返回对象：`IdResponse`

## 7. 人情记录接口

### 7.1 人情记录分页查询
- 接口：`POST /app/record/page`
- 用途：记录列表页、筛选查询
- 请求对象：`RecordPageRequest`
- 请求字段：
  - `contactId`
  - `eventId`
  - `direction`：`RECEIVE` / `SEND`
  - `eventTypeCode`
  - `startDate`
  - `endDate`
  - `pageNo`
  - `pageSize`
- 返回对象：`PageResponse<RecordPageItemResponse>`

### 7.2 人情记录详情
- 接口：`POST /app/record/detail`
- 用途：编辑页回显、记录详情页展示
- 请求对象：`RecordDetailRequest`
- 请求字段：
  - `recordId`
- 返回对象：`RecordDetailResponse`

### 7.3 新增人情记录
- 接口：`POST /app/record/save`
- 用途：新增收礼或送礼记录
- 请求对象：`RecordSaveRequest`
- 请求字段：
  - `contactId`
  - `eventId`
  - `direction`：`RECEIVE` / `SEND`
  - `amount`
  - `recordDate`
  - `remark`
- 返回对象：`IdResponse`
- 业务规则：
  - 联系人、事件、收礼送礼、金额、记录日期必填
  - 金额必须大于 0，保留两位小数
  - 一条记录只对应一个联系人和一个事件

### 7.4 编辑人情记录
- 接口：`POST /app/record/update`
- 用途：编辑记录
- 请求对象：`RecordUpdateRequest`
- 请求字段：
  - `recordId`
  - `contactId`
  - `eventId`
  - `direction`
  - `amount`
  - `recordDate`
  - `remark`
- 返回对象：`IdResponse`

### 7.5 本人时间线查询
- 接口：`POST /app/record/self-timeline`
- 用途：查询本人某时间段内的收礼送礼时间线
- 请求对象：`SelfTimelineRequest`
- 请求字段：
  - `startDate`
  - `endDate`
  - `direction`
  - `eventTypeCode`
  - `pageNo`
  - `pageSize`
- 返回对象：`SelfTimelineResponse`
- 返回字段：
  - `summaryInfo`
  - `pageResult`

### 7.6 联系人时间线查询
- 接口：`POST /app/record/contact-timeline`
- 用途：查看与某个联系人的往来时间线
- 请求对象：`ContactTimelineRequest`
- 请求字段：
  - `contactId`
  - `startDate`
  - `endDate`
  - `direction`
  - `eventTypeCode`
  - `pageNo`
  - `pageSize`
- 返回对象：`ContactTimelineResponse`

## 8. 闭环与回礼参考接口

### 8.1 闭环分页查询
- 接口：`POST /app/reciprocity/page`
- 用途：查看未闭环、已闭环、手工取消等记录
- 请求对象：`ReciprocityPageRequest`
- 请求字段：
  - `contactId`
  - `eventTypeCode`
  - `reciprocityStatus`
  - `startDate`
  - `endDate`
  - `pageNo`
  - `pageSize`
- 返回对象：`PageResponse<ReciprocityPageItemResponse>`

### 8.2 闭环详情
- 接口：`POST /app/reciprocity/detail`
- 用途：查看某条记录的闭环匹配详情和历史参考
- 请求对象：`ReciprocityDetailRequest`
- 请求字段：
  - `recordId`
- 返回对象：`ReciprocityDetailResponse`
- 返回字段：
  - `recordInfo`
  - `matchedRecordInfo`
  - `historyReference`
  - `manualFlagInfo`

### 8.3 手工确认闭环
- 接口：`POST /app/reciprocity/manual-confirm`
- 用途：把两条记录手工确认为闭环
- 请求对象：`ReciprocityManualConfirmRequest`
- 请求字段：
  - `sourceRecordId`
  - `targetRecordId`
  - `remark`
- 返回对象：`IdResponse`
- 业务规则：
  - 两条记录必须属于同一联系人
  - 两条记录事件类型必须一致
  - 两条记录方向必须相反

### 8.4 手工取消闭环
- 接口：`POST /app/reciprocity/manual-cancel`
- 用途：取消自动识别或手工确认的闭环关系
- 请求对象：`ReciprocityManualCancelRequest`
- 请求字段：
  - `reciprocityMatchId`
  - `cancelReason`
- 返回对象：`IdResponse`

### 8.5 回礼历史参考
- 接口：`POST /app/reciprocity/history-reference`
- 用途：新增记录或查看详情时展示回礼历史参考
- 请求对象：`ReciprocityHistoryReferenceRequest`
- 请求字段：
  - `contactId`
  - `eventTypeCode`
- 返回对象：`ReciprocityHistoryReferenceResponse`
- 返回字段：
  - `sameTypeReceiveAmount`
  - `sameTypeSendAmount`
  - `lastSameTypeRecord`
  - `unclosedRecordCount`

## 9. 统计接口

### 9.1 统计总览
- 接口：`POST /app/stats/overview`
- 用途：统计页总览
- 请求对象：`StatsOverviewRequest`
- 请求字段：
  - `startDate`
  - `endDate`
- 返回对象：`StatsOverviewResponse`
- 返回字段：
  - `receiveTotalAmount`
  - `sendTotalAmount`
  - `netAmount`
  - `receiveCount`
  - `sendCount`

### 9.2 按联系人统计
- 接口：`POST /app/stats/by-contact`
- 用途：查看某时间范围内按联系人维度的统计结果
- 请求对象：`StatsByContactRequest`
- 请求字段：
  - `startDate`
  - `endDate`
  - `relationType`
  - `keyword`
  - `pageNo`
  - `pageSize`
- 返回对象：`PageResponse<StatsByContactItemResponse>`

### 9.3 按事件类型统计
- 接口：`POST /app/stats/by-event-type`
- 用途：查看某时间范围内按事件类型的统计结果
- 请求对象：`StatsByEventTypeRequest`
- 请求字段：
  - `startDate`
  - `endDate`
- 返回对象：`ListResponse<StatsByEventTypeItemResponse>`

## 10. 字典接口

### 10.1 事件类型字典
- 接口：`POST /app/dict/event-type/list`
- 用途：事件类型选择器、筛选器初始化
- 请求对象：`EventTypeListRequest`
- 请求字段：
  - `enabledFlag`
- 返回对象：`ListResponse<EventTypeItemResponse>`

### 10.2 关系类型字典
- 接口：`POST /app/dict/relation-type/list`
- 用途：关系类型选择器、筛选器初始化
- 请求对象：`RelationTypeListRequest`
- 请求字段：
  - `keyword`
- 返回对象：`ListResponse<RelationTypeItemResponse>`
- 说明：当前版本关系类型也可以由后端固定枚举返回，不强制建表。

## 11. 建议优先级

### 11.1 P0
1. `/app/home/overview`
2. `/app/contact/page`
3. `/app/contact/save`
4. `/app/contact/update`
5. `/app/contact/detail`
6. `/app/event/page`
7. `/app/event/save`
8. `/app/event/update`
9. `/app/event/detail`
10. `/app/record/page`
11. `/app/record/save`
12. `/app/record/update`
13. `/app/record/detail`
14. `/app/record/self-timeline`
15. `/app/record/contact-timeline`
16. `/app/reciprocity/page`
17. `/app/reciprocity/detail`
18. `/app/reciprocity/manual-confirm`
19. `/app/reciprocity/manual-cancel`
20. `/app/reciprocity/history-reference`
21. `/app/stats/overview`
22. `/app/stats/by-contact`
23. `/app/stats/by-event-type`
24. `/app/dict/event-type/list`

## 12. 实现说明
1. Controller 建议按 `HomeController`、`ContactController`、`EventController`、`RecordController`、`ReciprocityController`、`StatsController`、`DictController` 拆分。
2. 记录新增、记录编辑、手工确认闭环、手工取消闭环这几类写操作应放在事务中完成。
3. 闭环识别建议在 Service 层封装，避免 Controller 中出现复杂业务判断。
4. 所有 ID 建议由应用层使用雪花算法生成字符串，数据库不使用自增主键。
