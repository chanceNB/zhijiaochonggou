# Codex TASK 顺序

| TASK | 目标 | 主要验收 |
|---|---|---|
| T00 | Monorepo/Starter/Contract 基线 | Web/API/DB 可启动，OpenAPI 校验 |
| T01 | Demo Baseline + DemoRun | 2班80人，小明历史状态，可重放不污染 |
| T02 | Student Learning/TwinKT | LearningEvent → Snapshot；权威状态可测试 |
| T03 | Knowledge RAG + Real LLM + AI Coach | 文档索引、真实检索、真实LLM、citation、2题诊断 |
| T04 | Practice + Similar + WrongBook | 作答、相似题、显式入错题本、PracticeOutcome |
| T05 | Analytics Exchange | Outbox → 10 canonical datasets，baseline+live，CSV fallback |
| T06 | SmartBI ONLINE BUILD GATE | 人工在挑战杯平台验证数据网关/ETL/模型/2个主演示Dashboard/AIChat |
| T07 | Teacher Recommendation + Intervention | capture建议、EffectEstimator、approve/commit/assignment |
| T08 | Student Assignment + Transfer + Outcome + Growth | 教师任务回学生、迁移验证、A/B outcome、Growth |
| T09 | SmartBI second refresh + Smart Report | 人工 SmartBI 刷新、AIChat、报告证据 |
| T10 | Full E2E / Competition Hardening | Golden Demo 一条链从头到尾通过 |

T06/T09 属于 SmartBI 平台人工 Gate；Codex 只能生成 runbook/校验脚本，不能假装已在云平台完成。
