# TASK-ID: T05 — Analytics Exchange

目标：transactional outbox + 幂等 projection，创建 smartbi_exchange schema 的 10 个 canonical datasets、freshness、demo_run_state、CSV export。

禁止：在 Java 中完成 SmartBI 最终业务模型；SmartBI 不得读核心表。

验收：baseline+active live 同时可见；live新增两道错题后事实计数变化；reset后不重复计入；read-only user只能SELECT。


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
