# TASK-ID: T07 — Recommendation + Intervention

目标：实现 AnalysisRecommendation capture/port、教师三方案 review、EffectEstimator、Intervention propose/approve/commit 和 Assignment。

边界：SmartBI AIChat 提供 rationale/action；predictedLift 由 B EffectEstimator 生成；教师必须确认。平台 API 未验证时使用 MANUAL_CAPTURE Adapter，不伪造自动回调。


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
