# 智教慧评 Golden Demo V2 · Codex 从零开发包

版本：V2.0  
冻结日期：2026-08-29  
适用方式：单人 + Codex，按 TASK-ID 顺序逐个完成，不允许一次性让 Codex 横跨多个任务。

## 1. 业务真源

本包以 **GOLDEN-DEMO-V2** 为最高业务真源：

1. 小明进入学生端；系统根据历史学习数据发现薄弱知识点“图遍历 BFS / DFS”。
2. 学生端原生 AI Coach 读取学生学习状态、调用真实大模型并通过课程 RAG 获取证据，生成 2 道结构化诊断题。
3. 小明答题，可继续与 AI Coach 互动、请求相似题，并显式把错题加入错题本。
4. 本轮新增学习/练习/错题/学习状态事实进入 Analytics Exchange，并同步给挑战杯官方 SmartBI Cloud。
5. SmartBI Cloud 在平台内完成数据接入、ETL、数据建模、Dashboard 与 AIChat 分析。
6. SmartBI AIChat 为张老师给出三个“分析建议候选”；它不是正式教学动作的事实源。
7. 张老师回到智教慧评教师端确认方案；B 域负责正式化、效果估计、审批、Commit 与 Assignment。
8. 小明在 Today 收到教师任务，完成专项练习和迁移验证。
9. A 更新学生学习状态，B 形成 InterventionOutcome；新事实再次进入 SmartBI。
10. SmartBI 刷新模型，展示干预前后变化；AIChat 继续分析，并生成 Smart Report。
11. 学生端 Growth 记录小明成长。

## 2. 三条绝对边界

- **学生 AI Coach ≠ SmartBI AIChat**。前者属于智教慧评学生业务，使用真实 LLM + RAG；后者属于 SmartBI Cloud，面向教师做数据分析。
- **SmartBI 不写业务事实**。它可以分析和给建议，但正式 Diagnosis / Intervention / Assignment / Outcome 只能由业务域写入。
- **SmartBI 不直接读业务核心表**。只读取 `smartbi_exchange` 数据源；SmartBI 平台内再完成比赛要求的 ETL、模型、指标、Dashboard、AIChat、归因和报告。

## 3. 推荐技术栈

- 前端：Vue 3 + TypeScript + Vite + Vue Router + Pinia + Axios + Zod + Vitest。
- 后端：Java 21 + Spring Boot 3.x + Spring Web + Bean Validation + Spring JDBC + Flyway + Actuator。
- 数据库：PostgreSQL 16；RAG 使用 pgvector（demo 数据量允许无 ANN 索引）。
- 文档解析：Apache Tika（PDF / DOCX / TXT / Markdown）。
- LLM：OpenAI-compatible HTTP Adapter，真实 provider 通过环境变量配置。
- SmartBI：`https://tiaozhanbei.cloud.smartbi.com.cn/`，优先验证数据库网关；CSV 为可复现保底。

## 4. 开发顺序

按 `10_CODEX_TASK_SEQUENCE.md` 执行。每个 TASK：预检 → 写失败测试 → 最小实现 → 全量回归 → git diff --check → 单独 commit → 输出报告。

## 5. 阅读顺序

1. `00_GOLDEN_DEMO_V2_FREEZE.md`
2. `01_TECHNICAL_ARCHITECTURE.md`
3. `02_DOMAIN_BOUNDARIES_AND_CONTRACTS.md`
4. `03_DATA_MODEL_AND_SMARTBI_EXCHANGE.md`
5. `04_API_INTERFACE_SPEC.md` / `04_API_INTERFACE_SPEC.docx`
6. `05_FRONTEND_CONTRACT.md`
7. `06_COMPONENT_LIBRARY.md`
8. `08_ERROR_AND_STATE_STANDARD.md`
9. `09_CODEX_MASTER_PROMPT.md`
10. 当前 `task-packs/TASK-*.md`

## 6. SmartBI 平台 Gate

SmartBI Cloud 的具体数据库网关、资源 URL、iframe、AIChat 结构化回传能力以挑战杯租户实际权限为准。代码必须预留 Adapter，不得伪造 resid、API、回调或 iframe 能力。
