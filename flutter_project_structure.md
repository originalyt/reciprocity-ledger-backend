# 礼账 App Flutter 目录结构与 Riverpod 拆分建议

## 1. 说明

本文档用于给礼账 App 提供一版可落地的 Flutter 工程结构建议，并结合 Riverpod 说明页面状态、业务状态与数据层的拆分方式。

目标：
- 让项目结构一开始就有扩展性
- 降低页面增多后代码混乱的风险
- 让 UI、业务、数据职责分层清楚

---

## 2. 总体结构建议

建议采用按领域 + 分层结合的方式组织项目。

推荐目录：

```text
lib/
  app/
    app.dart
    router/
    theme/
    bootstrap/
  core/
    constants/
    extensions/
    utils/
    errors/
    widgets/
  features/
    auth/
    home/
    ledger/
    contact/
    timeline/
    reciprocity/
    stats/
    settings/
  shared/
    models/
    dto/
    providers/
```

说明：
- `app`：应用级配置，如路由、主题、启动初始化
- `core`：跨功能通用能力
- `features`：按业务模块拆分
- `shared`：跨 feature 共用但不适合放进 core 的模型或 provider

---

## 3. 单个 feature 内部结构建议

以 `reciprocity` 为例：

```text
features/
  reciprocity/
    domain/
      entities/
      enums/
      repositories/
      usecases/
    application/
      providers/
      controllers/
      states/
    infrastructure/
      datasources/
      models/
      repositories/
    presentation/
      pages/
      widgets/
```

### 各层职责

- `domain`
  - 核心业务概念
  - 业务枚举、实体、仓储接口
  - 领域用例定义

- `application`
  - Riverpod provider
  - 页面控制器
  - 页面状态对象
  - 调用 domain / infrastructure 完成业务组合

- `infrastructure`
  - Dio API 调用
  - DTO / Model 转换
  - repository 实现

- `presentation`
  - 页面、组件、样式、交互

---

## 4. 页面与 Provider 拆分建议

### 4.1 Provider 分类
建议至少分成这几类：

- 页面数据 Provider
- 筛选条件 Provider
- 表单草稿 Provider
- 操作型 Controller Provider
- 全局会话 Provider

### 4.2 示例拆分

#### 首页
- `homeOverviewProvider`
- `homePendingProvider`
- `homeRecentRecordsProvider`

#### 新增记录页
- `recordDraftProvider`
- `recordFormControllerProvider`
- `recordSubmitProvider`

#### 时间线页
- `timelineFilterProvider`
- `timelineListProvider`
- `timelineGroupingProvider`

#### 回礼清单页
- `reciprocityTabProvider`
- `reciprocityFilterProvider`
- `reciprocityListProvider`
- `reciprocityActionControllerProvider`

#### 联系人详情页
- `contactDetailProvider`
- `contactTimelineProvider`
- `contactActionControllerProvider`

---

## 5. 推荐的状态对象设计

### 5.1 页面状态
建议统一封装页面状态，例如：
- `loading`
- `data`
- `empty`
- `error`

可以使用：
- `AsyncValue<T>`
- 或项目内统一定义页面状态类

### 5.2 筛选状态
筛选条件建议独立成对象，不直接散落在 Widget 本地状态中。

例如：
- `TimelineFilterState`
- `ReciprocityFilterState`
- `StatsFilterState`

这些对象中可包含：
- 时间范围
- 联系人
- 关系
- 事件类型
- 排序方式
- 关键字

### 5.3 表单状态
表单状态建议独立封装，如：
- `RecordDraftState`
- `ContactDraftState`

字段包括：
- 当前值
- 校验状态
- 是否已修改
- 是否从草稿恢复
- 是否可提交

---

## 6. 页面组件拆分建议

以“新增记录页”为例，建议拆成：
- `RecordPageScaffold`
- `RecentContactsSection`
- `CommonEventTypesSection`
- `RecordTypeSelector`
- `RecordAmountField`
- `RecordDateField`
- `RecordRemarkField`
- `RecordSubmitBar`

以“回礼清单页”为例，建议拆成：
- `ReciprocityTabBar`
- `ReciprocityFilterBar`
- `ReciprocityEventList`
- `ReciprocityEventTile`
- `ReciprocityEmptyView`

原则：
- 页面由结构组件拼装
- 业务展示块尽量提炼成复用 Widget
- 页面的网络逻辑不要写进 Widget 中

---

## 7. 路由建议

建议在 `app/router/` 中统一定义路由。

推荐页面路径：
- `/login`
- `/home`
- `/timeline`
- `/contacts`
- `/contacts/:id`
- `/records/new`
- `/records/:id/edit`
- `/reciprocity`
- `/reciprocity/:id`
- `/stats`
- `/settings`

建议：
- 详情页通过参数传 `id`
- 页面筛选条件尽量通过 provider 管理，不全部塞进路由参数
- 对深链接或分享能力预留扩展空间

---

## 8. Dio 与 Repository 建议

### 8.1 Dio
建议统一管理：
- Base URL
- Access Token 注入
- 刷新 token 流程
- 错误拦截
- 日志输出

### 8.2 Repository
每个 feature 建议拥有自己清晰的 repository 接口，例如：
- `AuthRepository`
- `ContactRepository`
- `LedgerRepository`
- `TimelineRepository`
- `ReciprocityRepository`
- `StatsRepository`

页面层只依赖 provider / controller，不直接调用 Dio。

---

## 9. 本地存储建议

### 9.1 shared_preferences
适合存：
- 筛选条件
- 草稿
- UI 偏好
- 最近联系人简表

### 9.2 安全存储
适合存：
- Access Token
- Refresh Token
- 登录态相关敏感信息

### 9.3 缓存策略
建议明确：
- 首页概览可短期缓存
- 联系人列表可本地缓存提升选择体验
- 详情页与统计页以接口实时数据为准

---

## 10. 推荐的 feature 划分顺序

建议优先建立以下 feature：
- `auth`
- `home`
- `ledger`
- `contact`
- `reciprocity`
- `timeline`

之后再补：
- `stats`
- `settings`
- 导入导出相关 feature

这样能先跑通高频主路径。

---

## 11. 建议的开发规范

### 11.1 命名
- Provider 以 `Provider` 结尾
- Controller 以 `Controller` 结尾
- 页面以 `Page` 结尾
- 组件以 `Card` / `Tile` / `Section` / `Bar` 结尾

### 11.2 分层纪律
- `presentation` 不直接依赖 Dio
- `infrastructure` 不处理页面样式逻辑
- `application` 负责把页面行为组合起来
- `domain` 尽量保持纯粹

### 11.3 样式纪律
- 所有颜色、字号、圆角尽量来自主题系统
- 禁止在页面随意硬编码大量颜色值
- 业务状态色统一走主题扩展

---

## 12. 总结

推荐的 Flutter 实现方向是：
- 用 `app + core + features + shared` 建立清晰工程骨架
- 用 feature 内部四层拆分降低耦合
- 用 Riverpod 管理页面状态、筛选状态、草稿状态和操作行为
- 用统一主题和自定义组件保证视觉一致性

后续可以继续补充：
- 目录脚手架清单
- provider 命名示例
- 首页与回礼页的 provider 关系图
- go_router 配置草案

