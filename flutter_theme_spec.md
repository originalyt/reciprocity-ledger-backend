# 礼账 App ThemeData 与设计主题草案

## 1. 说明

本文档用于把前面的 UI 风格草案进一步落到 Flutter `ThemeData` / `ColorScheme` / `TextTheme` 层。

目标：
- 为开发提供统一主题基线
- 避免页面各自定义颜色、字号、圆角
- 保证“简约、清晰、克制”的视觉方向可以稳定落地

---

## 2. 主题目标

主题关键词：
- 简约
- 清晰
- 克制
- 温和
- 稳定

主题落地原则：
- 默认浅色主题优先
- 中性色占主导，品牌色只做重点强调
- 页面背景柔和，卡片清晰但不过度浮起
- 字号层级少而稳定，不做过多变体

---

## 3. ColorScheme 草案

### 3.1 基础色
- `primary`: `#B35C44`
- `onPrimary`: `#FFFFFF`
- `primaryContainer`: `#F6EAE5`
- `onPrimaryContainer`: `#5E2C1D`

- `secondary`: `#7B6D66`
- `onSecondary`: `#FFFFFF`
- `secondaryContainer`: `#EFE8E4`
- `onSecondaryContainer`: `#3A312E`

- `tertiary`: `#8F7C5A`
- `onTertiary`: `#FFFFFF`
- `tertiaryContainer`: `#F4ECDD`
- `onTertiaryContainer`: `#4A3A1D`

### 3.2 背景与表面
- `surface`: `#FFFFFF`
- `onSurface`: `#222222`
- `surfaceVariant`: `#F4F1EE`
- `onSurfaceVariant`: `#666666`
- `background`: `#F7F7F5`
- `onBackground`: `#222222`

### 3.3 分割与轮廓
- `outline`: `#E8E6E3`
- `outlineVariant`: `#F0EEEB`

### 3.4 错误色
- `error`: `#C84D3A`
- `onError`: `#FFFFFF`
- `errorContainer`: `#FBE9E6`
- `onErrorContainer`: `#6A2318`

---

## 4. 业务状态色建议

以下状态色不一定全部直接放进 `ColorScheme`，建议由业务主题扩展统一管理：

- `waitMe`: `#E59A2F`
- `waitOther`: `#4C84D3`
- `mutual`: `#5E9B6B`
- `noNeed`: `#9A9A9A`
- `voided`: `#C2C2C2`

配套浅背景建议：
- `waitMeSoft`: `#FFF3DF`
- `waitOtherSoft`: `#EAF2FF`
- `mutualSoft`: `#EAF5EC`
- `noNeedSoft`: `#F1F1F1`
- `voidedSoft`: `#F4F4F4`

---

## 5. TextTheme 草案

建议保持少量且稳定的文字层级：

- `displaySmall`
  - 用途：首页大金额
  - 建议：`fontSize 32`, `fontWeight w600`

- `headlineSmall`
  - 用途：页面标题、模块主标题
  - 建议：`fontSize 24`, `fontWeight w600`

- `titleLarge`
  - 用途：卡片标题、联系人名、事件名
  - 建议：`fontSize 20`, `fontWeight w600`

- `titleMedium`
  - 用途：列表主信息、按钮文字
  - 建议：`fontSize 16`, `fontWeight w500`

- `bodyLarge`
  - 用途：正文、表单输入、普通信息
  - 建议：`fontSize 16`, `fontWeight w400`

- `bodyMedium`
  - 用途：次级说明、辅助正文
  - 建议：`fontSize 14`, `fontWeight w400`

- `labelMedium`
  - 用途：标签、状态、小按钮
  - 建议：`fontSize 12`, `fontWeight w500`

数字展示建议：
- 金额可统一使用更稳的半粗字重
- 列表中的金额、日期尽量保持视觉对齐

---

## 6. Shape 与圆角建议

建议统一使用圆角系统，不要每类组件各自定义：

- `small`: `8`
- `medium`: `12`
- `large`: `16`
- `xLarge`: `20`
- `pill`: `999`

组件映射：
- 输入框：`12`
- 按钮：`12`
- 卡片：`16`
- 底部弹层：`20`
- 状态标签：`pill`

---

## 7. 组件主题建议

### 7.1 Scaffold
- 页面背景建议统一用 `background`
- 不建议每页单独指定背景色

### 7.2 AppBar
- 背景色建议与页面背景统一或近似
- 不建议强色背景 AppBar
- 标题颜色使用主文字色
- `elevation` 尽量低或为 0

### 7.3 Card
- 白底
- 低阴影或无阴影
- 圆角 `16`
- 内边距统一

### 7.4 FilledButton
- 作为主按钮使用
- 背景用 `primary`
- 高度建议在 `48~52`
- 文案尽量短

### 7.5 OutlinedButton
- 作为次级操作
- 边框使用 `outline`
- 避免边框颜色过重

### 7.6 TextButton
- 作为轻操作、次级链接操作
- 避免在一个区域内出现过多 TextButton 导致层级混乱

### 7.7 InputDecorationTheme
建议：
- 使用填充式输入框
- 背景偏白或浅灰
- 默认边框弱化
- 聚焦时使用主色边框
- 错误时使用错误色边框与辅助文案

### 7.8 ChipTheme
- 用于筛选项与轻量标签
- 筛选项与状态标签可共用基础样式，但状态标签应再封装为业务组件

### 7.9 BottomSheet / Dialog
- 圆角 `20`
- 内容边距充足
- 操作按钮层级明确

---

## 8. 建议的 Theme Extension

建议通过 `ThemeExtension` 扩展业务主题字段，而不是把业务色散落在各页面中。

建议扩展：
- `LedgerStatusColors`
  - `waitMe`
  - `waitMeSoft`
  - `waitOther`
  - `waitOtherSoft`
  - `mutual`
  - `mutualSoft`
  - `noNeed`
  - `noNeedSoft`
  - `voided`
  - `voidedSoft`

- `LedgerSpacing`
  - `xs`, `sm`, `md`, `lg`, `xl`

- `LedgerRadius`
  - `small`, `medium`, `large`, `xLarge`, `pill`

这样能让设计 token 更容易在全局复用。

---

## 9. 页面级主题使用建议

### 首页
- 指标卡背景统一
- 重点靠数字和结构体现，不靠夸张配色
- 待办模块使用状态色点缀

### 新增记录页
- 输入区简洁、背景稳定
- 提交按钮固定样式
- 校验提示使用错误色，但不宜过重

### 回礼清单页
- 状态颜色是页面重点
- Tab、筛选条、状态标签风格需统一
- `无需回礼` 状态颜色应明显弱于待办状态

### 详情页
- 详情页仍保持同一主题，不单独做视觉跳脱
- 卡片结构和按钮层级应延续全局系统

---

## 10. 暗色模式建议

当前阶段建议：
- 先不作为第一优先级
- 若后续支持暗色模式：
  - 不使用纯黑
  - 保持品牌色低饱和
  - 状态色整体降低亮度与饱和度
  - 保证金额与状态可读性优先

---

## 11. 总结

这套主题草案的核心目标是：
- 用统一 ThemeData 保证页面风格稳定
- 用 ThemeExtension 管理业务状态色与设计 token
- 让 Flutter 开发阶段直接拥有可落地的视觉基线

后续可以继续补充：
- `ThemeData` 示例代码
- `ColorScheme.fromSeed` 与手动配色比较建议
- `TextTheme` 代码草案
- `ThemeExtension` Dart 结构示例

