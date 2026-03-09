# 礼账 App Flutter 组件映射建议

## 1. 说明

本文档用于把当前 UI 风格草案和页面样式草图，进一步映射到 Flutter + Material 3 的具体组件实现层。

目标：
- 降低设计与开发沟通成本
- 提前统一组件选型
- 减少页面实现时的样式分裂

---

## 2. 全局设计 Token 建议

### 2.1 颜色 Token
建议在 Flutter 中抽象为：
- `brandPrimary`
- `brandPrimarySoft`
- `pageBackground`
- `cardBackground`
- `textPrimary`
- `textSecondary`
- `textTertiary`
- `dividerColor`
- `statusWaitMe`
- `statusWaitOther`
- `statusMutual`
- `statusNoNeed`
- `statusVoid`

### 2.2 圆角 Token
建议统一定义：
- `radiusSmall = 8`
- `radiusMedium = 12`
- `radiusLarge = 16`
- `radiusXLarge = 20`
- `radiusPill = 999`

### 2.3 间距 Token
建议统一定义：
- `space4`
- `space8`
- `space12`
- `space16`
- `space20`
- `space24`

### 2.4 字体 Token
建议统一定义：
- `displayAmount`
- `titleLarge`
- `titleMedium`
- `bodyPrimary`
- `bodySecondary`
- `labelSmall`

---

## 3. Material 3 组件映射

### 3.1 页面 Scaffold
推荐：
- `Scaffold`
- 页面背景使用统一浅色
- 底部操作区可用 `bottomNavigationBar` 或 `bottomSheet`

适用页面：
- 首页
- 时间线页
- 联系人页
- 回礼页
- 详情页

### 3.2 顶部导航
推荐：
- `AppBar`
- 简化标题
- 少用复杂渐变和图片背景

适用页面：
- 全部主页面与详情页

### 3.3 卡片类容器
推荐：
- `Card`
- 或 `Container + BoxDecoration`

建议：
- 低 `elevation`
- 统一圆角
- 内边距统一

适用页面：
- 首页指标卡
- 待办卡
- 联系人信息卡
- 事件概览卡

### 3.4 按钮
推荐：
- 主操作：`FilledButton`
- 次操作：`OutlinedButton`
- 轻操作：`TextButton`
- 特殊轻强调操作：`FilledButton.tonal`

适用规则：
- 提交保存类：`FilledButton`
- 筛选、归档、查看详情类：`OutlinedButton` 或 `TextButton`
- “无需回礼”可优先考虑 `FilledButton.tonal`

### 3.5 标签与状态
推荐：
- `Container + Text`
- 或基于 `Chip` 自定义轻量状态标签

建议：
- 不直接复用默认过滤 Chip 样式做状态标签
- 状态标签应有统一高度、统一内边距、统一圆角

适用状态：
- 随礼 / 收礼
- 我待回礼 / 待对方回礼 / 已互回 / 无需回礼
- 作废

### 3.6 筛选条
推荐：
- `ChoiceChip`
- `FilterChip`
- 配合底部弹层筛选面板

适用页面：
- 时间线页
- 回礼清单页
- 统计页

建议：
- 少量高频筛选直接平铺
- 复杂筛选放入 `showModalBottomSheet`

### 3.7 表单控件
推荐：
- `TextFormField`
- `DropdownMenu` 或 `DropdownButtonFormField`
- `SegmentedButton`
- `DatePicker`

映射建议：
- 联系人输入：搜索选择器 + 页面跳转选择
- 记录类型：`SegmentedButton`
- 事件类型：下拉选择器
- 日期：日期选择器
- 金额：数字输入框
- 备注：多行输入框

### 3.8 列表
推荐：
- `ListView.separated`
- 或 `CustomScrollView + SliverList`

适用页面：
- 时间线页
- 联系人列表页
- 回礼清单页
- 最近记录区

建议：
- 若页面顶部有多个模块，优先 `CustomScrollView`
- 简单长列表优先 `ListView.separated`

### 3.9 Tab 与分段切换
推荐：
- `TabBar`
- 或自定义分段选择器

适用页面：
- 回礼清单页
- 时间线页（混合/随礼/收礼）

建议：
- 若状态数量固定且重要，使用更明显的分段切换样式
- `无需回礼` Tab 建议视觉上比待办 Tab 更克制

### 3.10 弹层与确认操作
推荐：
- `showModalBottomSheet`
- `AlertDialog`
- `SnackBar`

映射建议：
- 筛选面板：`showModalBottomSheet`
- 设置无需回礼确认：`AlertDialog` 或底部弹层
- 提交成功反馈：`SnackBar`

---

## 4. 页面到组件的映射建议

### 4.1 首页
建议组件组合：
- `Scaffold`
- `CustomScrollView`
- 顶部标题区：`SliverToBoxAdapter`
- 指标区：自定义 `SummaryCard`
- 待办区：自定义 `TaskCard`
- 快捷入口：自定义 `QuickActionGrid`
- 最近记录：自定义 `RecentRecordList`

### 4.2 新增 / 编辑记录页
建议组件组合：
- `Scaffold`
- `Form`
- 快捷选择区：横向 `Wrap` / `SingleChildScrollView`
- 字段区：多个 `TextFormField` / `Dropdown` / `SegmentedButton`
- 底部提交区：固定 `FilledButton`

### 4.3 回礼清单页
建议组件组合：
- `Scaffold`
- 顶部状态切换：`TabBar` 或自定义分段控件
- 筛选条：`ChoiceChip` / `FilterChip`
- 列表：`ListView.separated`
- 列表项：自定义 `ReciprocityEventTile`

### 4.4 联系人详情页
建议组件组合：
- `Scaffold`
- 联系人卡：自定义 `ContactHeaderCard`
- 汇总卡：自定义 `ContactSummaryCard`
- 快捷操作区：按钮组
- 时间线：复用 `TimelineRecordTile`

### 4.5 联系人 + 事件详情页
建议组件组合：
- `Scaffold`
- 事件概览卡：自定义 `EventHeaderCard`
- 状态卡：自定义 `ReciprocityStatusCard`
- 双向记录区：两个 `RecordInfoCard`
- 操作区：按钮组 + 更多菜单

---

## 5. 建议抽象的自定义组件

建议尽早抽象以下组件，避免页面各自重复实现：
- `SummaryCard`
- `StatusTag`
- `QuickActionTile`
- `SectionHeader`
- `TimelineRecordTile`
- `ReciprocityEventTile`
- `ContactHeaderCard`
- `ContactSummaryCard`
- `EventHeaderCard`
- `RecordInfoCard`
- `EmptyStateView`
- `FilterChipGroup`

这些组件会构成礼账 App 的第一版基础设计系统。

---

## 6. 状态管理与 UI 结合建议

### 6.1 页面状态
每个页面建议统一处理：
- loading
- loaded
- empty
- error

### 6.2 业务状态
需要在 UI 层明确处理：
- 回礼状态
- 无需回礼标记
- 记录是否作废
- 草稿是否恢复
- 表单是否可提交

### 6.3 样式状态
组件层建议明确区分：
- default
- selected
- disabled
- error
- success

---

## 7. 下一步建议

在本组件映射建议基础上，后续可以继续补充：
- Flutter 目录结构建议
- 组件命名规范
- 页面级 Widget 树草图
- Riverpod provider 拆分建议
- ThemeData / ColorScheme / TextTheme 示例

