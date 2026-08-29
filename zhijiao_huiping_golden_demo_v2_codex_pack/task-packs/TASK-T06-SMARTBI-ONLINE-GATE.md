# TASK-ID: T06 — SmartBI ONLINE BUILD GATE（人工为主）

目标：登录 `https://tiaozhanbei.cloud.smartbi.com.cn/` 验证比赛租户实际能力。

必须记录：数据库网关是否开放；若开放则连接 smartbi_exchange read-only source；否则导入 canonical CSV。使用 SmartBI 原生 ETL 和 Data Model，先完成 `student-risk` 与 `intervention-outcome` 两个主演示 Dashboard，再验证 AIChat。

Codex 只能生成 SQL/runbook/checklist，不得声称平台操作 PASS，除非用户提供实际截图/结果。


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
