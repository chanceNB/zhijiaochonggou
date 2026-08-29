# TASK-ID: T01 — Demo Baseline

目标：版本化生成 `baseline-ds-v1`：数据结构课程、2班×40学生、30天历史；小明属于1班并有 mastery=0.62/confidence=0.71/forgettingRisk=0.57。实现 DemoRun create/reset/trace。

验收：reset 不删除 baseline；每轮新 demoRunId；班级人数永远80；旧 live run 不参与当前聚合。


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
