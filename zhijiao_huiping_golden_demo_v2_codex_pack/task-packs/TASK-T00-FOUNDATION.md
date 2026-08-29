# TASK-ID: T00 — Foundation

目标：从零建立 monorepo：`apps/web`、`apps/api`、`database`、`contracts`。Vue3/TS/Vite；Spring Boot/Java21；PostgreSQL/Flyway；统一 Envelope/JWT/Idempotency 基础。

必须：OpenAPI 可解析；Web build/typecheck；API test；docker compose PostgreSQL health；Flyway 空基线通过。

禁止：业务页面、AI、SmartBI、Demo 数据。


## 全局要求
- 阅读 `09_CODEX_MASTER_PROMPT.md` 与 `11_RUN_ONE_TASK.md`。
- TDD；一条任务一个 commit。
- 任何 Golden Demo 核心链不得用静态 Mock 作为最终验收。
- 修改接口时同步 `04_API_INTERFACE_SPEC.md` 与 `openapi/openapi.yaml`。

## 停止条件
- 需要突破 A/B/C 事实所有权。
- 需要伪造 SmartBI/LLM/RAG 外部能力。
- 发现当前契约无法承载需求且需要破坏性改动。
- 工作树有重叠未提交修改。

## 报告
按 `11_RUN_ONE_TASK.md` 的最终报告格式输出并停止。
