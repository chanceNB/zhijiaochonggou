# CODEX MASTER PROMPT — 智教慧评 Golden Demo V2

你正在从零实现“智教慧评 Golden Demo V2”。

## 强制阅读

按顺序读取：
1. README_总包说明.md
2. 00_GOLDEN_DEMO_V2_FREEZE.md
3. 01_TECHNICAL_ARCHITECTURE.md
4. 02_DOMAIN_BOUNDARIES_AND_CONTRACTS.md
5. 03_DATA_MODEL_AND_SMARTBI_EXCHANGE.md
6. 04_API_INTERFACE_SPEC.md
7. openapi/openapi.yaml
8. 05_FRONTEND_CONTRACT.md
9. 06_COMPONENT_LIBRARY.md
10. 08_ERROR_AND_STATE_STANDARD.md
11. 当前 task-packs/TASK-*.md

## 最高约束

- 一次只执行一个 TASK-ID。
- Golden Demo V2 是业务真源；若其他文档冲突，以它为准并停止报告冲突。
- 使用模块化单体，不擅自改微服务。
- 真实 LLM + RAG 必须走 Port/Adapter；不在 Vue 暴露 API Key。
- AI Coach 题目必须结构化持久化，不把聊天文本当正式题。
- SmartBI 只读 smartbi_exchange；Java 不替 SmartBI 完成最终 ETL/模型。
- SmartBI AIChat 只产生分析建议；B 才能创建正式 Intervention/Assignment。
- 不伪造 SmartBI resid、API、iframe、回调或挑战杯租户权限。
- 前端不得计算 mastery/predictedLift/actualLift。
- 所有写命令幂等；状态迁移做 version/If-Match。
- TDD：先失败测试，再最小实现，再回归。

## 每个 TASK 开始前

```bash
git status --short
git branch --show-current
git rev-parse HEAD
```

工作树不干净且与 TASK 重叠时立即停止。

## 每个 TASK 结束

必须执行目标测试、全量测试（在成本可接受时）、`git diff --check`，然后只输出：修改文件、测试证据、边界检查、未解决 blocker、是否 PASS。

禁止自动进入下一个 TASK。
