# Repository Guidelines

## Project Structure & Module Organization
The active codebase is the Spring Boot service in `backend/`. Java sources live in `backend/src/main/java/com/reciprocityledger/backend`, organized by feature packages such as `health`, `contact`, `ledger`, `reciprocity`, `stats`, and `timeline`. Shared API wrappers are under `backend/src/main/java/com/reciprocityledger/backend/common/api`. Runtime configuration is in `backend/src/main/resources/application.yml`, and tests mirror the main package structure in `backend/src/test/java`. Root-level Markdown files and `schema_v2_no_view.sql` are project references, not build inputs.

## Build, Test, and Development Commands
Run all build commands from `backend/`.

- `mvn spring-boot:run` starts the API on port `8080`.
- `mvn test` runs the JUnit 5 test suite.
- `mvn clean package` builds the application JAR and runs tests.

Set database configuration with `DB_URL`, `DB_USERNAME`, and `DB_PASSWORD` before starting locally. The default fallback points to `jdbc:postgresql://localhost:5432/reciprocity_ledger`.

## Coding Style & Naming Conventions
Use Java 11 and follow the existing Spring style: 4-space indentation, one top-level class per file, and packages rooted at `com.reciprocityledger.backend`. Name controllers `*Controller`, keep DTO-style wrappers like `ApiResponse`, and prefer descriptive camelCase method names such as `contextLoads` or `health`. Keep feature code inside its domain package rather than adding broad utility folders. No formatter or lint plugin is configured in Maven, so match the surrounding code closely and keep imports clean.

## Testing Guidelines
Tests use `spring-boot-starter-test` with JUnit 5. Place tests under `backend/src/test/java` in the same package as the code they cover, and name classes `*Tests`. Add at least one focused test for each new controller, mapper, or service path you introduce. Run `mvn test` before opening a PR; use `@SpringBootTest` only when wiring matters, and prefer narrower tests when possible.

## Commit & Pull Request Guidelines
This workspace snapshot does not include `.git`, so commit history could not be inspected directly. Use short, imperative commit subjects such as `Add health endpoint response wrapper` and keep each commit focused on one change. PRs should include a concise summary, any database or config changes, linked issue numbers when available, and example requests or response snippets for API changes.

## Configuration & Security Tips
Do not commit real database credentials. Keep secrets in environment variables, and document new configuration keys in `backend/README.md` and `application.yml` defaults when appropriate.

## 开发约定
1. 使用中文进行对话
2. 你是一个高级java开发工程师, 开发时会充分考虑各种情况
3. 不需要你执行maven的编译
4. 所有的请求全部使用post, 入参全部使用RequestBody, 如果只有一个字段也封装为一个对象. 同理出参也需要使用对象, 哪怕只有一个字段
5. 工具类使用hutool工具包
6. 日志,实体类简化使用lombok
7. 如果涉及到新创建表需要记录表sql