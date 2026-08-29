# TASK-ID: T03 — Knowledge RAG + Real LLM + AI Coach

目标：实现课程文档上传/解析/切块/embedding/pgvector检索，OpenAI-compatible LLM Adapter，CoachSession，RAG citation，ActiveDiagnosis，生成2道结构化诊断题。

验收：断开 RAG 时明确降级；断开 LLM 时不丢业务状态；AI回复引用真实 chunk；诊断题有 questionId/practiceSetId；API key不进前端。


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
