# TASK-ID: T10 — Golden Demo Full E2E

目标：从 Student AI Coach 开始完整执行 DEMO-GRAPH-001，直到 Growth 和 SmartBI Smart Report。

必须验证：2道诊断题、AI互动、相似题、错题本、第一次SmartBI分析、三个建议、教师选择、Assignment、迁移验证、Outcome、第二次SmartBI分析、报告、Growth。任何一步使用 Mock/硬编码时 FAIL。


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
