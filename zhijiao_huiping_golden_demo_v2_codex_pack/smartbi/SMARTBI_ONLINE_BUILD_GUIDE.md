# SmartBI Cloud 在线施工指南

## 1. 官方平台
`https://tiaozhanbei.cloud.smartbi.com.cn/`

## 2. 连接优先级
1. 比赛租户数据库网关（若实际开放）。
2. 受控数据库连接（仅当官方流程支持且安全）。
3. Canonical CSV 导入保底。

不得把 PostgreSQL 5432 裸露公网。

## 3. 数据源
只允许 `smartbi_exchange` 10 个 canonical datasets + freshness/demo_run_state。

## 4. 平台内必须真正完成
- Data Import
- ETL 清洗/转换/校验
- Data Model / Metric Model
- 4 Dashboard
- AIChat 15 问（其中 Golden Demo 至少覆盖小明状态与干预建议）
- Attribution
- Smart Report

## 5. Golden Demo 第一次平台检查
筛选小明 + BFS/DFS，核验刚做的 2 道题已经进入数据源，并能与班级历史 cohort 对比。

## 6. Recommendation 回传
挑战杯租户未验证结构化 API 前，使用 MANUAL_CAPTURE：老师在 SmartBI AIChat 获取三建议 → 回教师端记录/选择。验证 API/MCP 后再切 PLATFORM_API Adapter。

## 7. 第二次平台检查
小明完成 Assignment 后刷新，核验 mastery/practice/transfer/intervention outcome；再让 AIChat 总结并生成报告。
