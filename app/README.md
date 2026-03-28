# 礼账 App

## 当前状态

当前目录已经提供一版 Flutter 客户端，并已接入后端真实接口。

已接入的接口：
- `/app/home/overview`
- `/app/contact/page`
- `/app/contact/detail`
- `/app/record/self-timeline`
- `/app/record/contact-timeline`
- `/app/reciprocity/page`
- `/app/reciprocity/detail`
- `/app/dict/event-type/list`
- `/app/event/page`
- `/app/event/save`
- `/app/record/save`

## 当前页面

- `/home` 首页
- `/timeline` 时间线
- `/reciprocity` 闭环列表
- `/record/editor` 新增记录
- `/contacts/:contactId` 联系人详情
- `/reciprocity/:eventId` 闭环详情

## 运行方式

默认使用真实后端：

```bash
cd app
flutter pub get
flutter run
```

如果本地后端地址不是默认值，可以传：

```bash
flutter run --dart-define=API_BASE_URL=http://127.0.0.1:10086
```

Chrome 本地调试默认会回源到当前页面主机，并访问 `10086` 端口。
例如页面运行在 `http://localhost:53421` 时，默认接口地址会是 `http://localhost:10086`。

如果只是看 UI，不连后端：

```bash
flutter run --dart-define=USE_MOCK_DATA=true
```

## 当前实现说明

- App 默认优先连接真实后端。
- 测试环境和纯 UI 预览环境可以切到 mock。
- 新增记录页已经能直接调用真实接口保存。
- Android 模拟器默认访问 `http://10.0.2.2:10086`，桌面端默认访问 `http://127.0.0.1:10086`。
- 当选择“新建事件后保存记录”时，事件类型 ID 目前按 `backend/sql/baseline.sql` 中的内置事件类型映射处理。

## 当前边界

目前“事件类型列表”接口只返回 `code/name`，不返回 `eventTypeId`。
因此前端为了走通 `/app/event/save`，临时使用了基线脚本里的内置类型 ID 映射。
更稳妥的长期方案，是后端后续直接在事件类型列表接口中补回 `id` 字段。

## 建议下一步

1. 增加联系人列表页与联系人选择页
2. 为新增记录页补“新建联系人”能力
3. 增加闭环手工确认 / 手工取消操作
4. 如要完全消除前端硬编码，补后端事件类型 `id` 返回字段
