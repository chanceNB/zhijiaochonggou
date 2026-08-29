# TASK-ID: T04 — Practice + Similar + WrongBook

目标：PracticeSet/Attempt/complete、相似题生成、错题显式加入、WrongBook review、PracticeOutcome。

验收：重复 submit 幂等；相似题是结构化 Question；错误 attempt 可一键入错题本且重复添加不重复；完成练习触发学习状态更新。


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
