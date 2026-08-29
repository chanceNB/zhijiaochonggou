# TASK-ID: T08 — Assignment + Transfer + Outcome + Growth

目标：Assignment 出现在 Student Today；学生执行 practice；TransferValidation；更新 StudentLearningSnapshot；B 生成 InterventionOutcome；Growth 展示干预前后。

验收：predictedLift/actualLift/deviation 可追踪；同一 demoRunId/correlationId 贯穿；Growth 不读取 SmartBI 结果作为学习状态真源。


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
