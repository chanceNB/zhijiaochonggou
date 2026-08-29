# TASK-ID: T09 — SmartBI Second Refresh + Smart Report（人工为主）

目标：验证小明干预后新事实进入 exchange；SmartBI 刷新 ETL/Model；`intervention-outcome` 显示前后变化；AIChat 基于同一模型分析；生成 Smart Report。

必须保存：截图、数据刷新时间、AIChat问题/回答、Smart Report资源证据。Codex 不得伪造平台产物。


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
