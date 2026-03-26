# 礼账 App

## 当前状态

当前目录已经提供一版 Flutter 客户端骨架，重点完成了：
- 主题系统与基础颜色、卡片、按钮、标签风格
- `go_router` 路由壳与底部导航
- 首页、时间线、回礼清单、联系人详情、事件详情、新增记录页
- 统一 mock 数据与后端 API 客户端占位

## 当前页面

- `/home` 首页
- `/timeline` 时间线
- `/reciprocity` 回礼清单
- `/record/editor` 新增记录
- `/contacts/:contactId` 联系人详情
- `/reciprocity/:eventId` 回礼事件详情

## 运行方式

```bash
cd app
flutter pub get
flutter run
```

## 当前接入边界

目前为了先把 UI 和页面流转跑通，页面使用的是本地 mock 数据。
后续接后端时，优先替换这些模块：
- `lib/shared/providers/mock_providers.dart`
- `lib/shared/mock/mock_data.dart`
- `lib/core/network/api_client.dart`

## 建议下一步

1. 把首页、回礼清单、联系人详情改为真实接口
2. 为新增记录页接入 `/app/record/save`
3. 增加联系人列表页与完整联系人选择页
4. 为筛选条件、草稿恢复和错误态补完整状态管理
