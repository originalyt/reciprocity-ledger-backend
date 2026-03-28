# Reciprocity Ledger Backend

后端实现以 `doc` 目录中的需求、接口和开发指导文档为准。

## 运行说明
1. 启动前配置 `DB_URL`、`DB_USERNAME`、`DB_PASSWORD`
2. 先执行 `sql/baseline.sql`
3. 在 `backend/` 目录下运行 `mvn spring-boot:run`
4. 服务默认监听 `10086` 端口
5. 如需允许非本机调试来源访问，可通过 `APP_CORS_ALLOWED_ORIGIN_PATTERNS` 覆盖跨域白名单，默认值为 `http://localhost:*` 和 `http://127.0.0.1:*`

## 文档位置
1. `doc/renqing-requirements-v1.md`
2. `doc/interface-list-v1.md`
3. `doc/database-schema-v1.md`
4. `doc/development-guide-v1.md`
