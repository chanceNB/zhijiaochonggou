# GOLDEN-DEMO-V2 业务链冻结

## 1. 演示角色

- 学生：小明，`stu-xiaoming`
- 教师：张老师，`teacher-zhang`
- 课程：《数据结构》，`course-data-structures`
- 班级：2024级计算机1班，`class-cs-2024-01`
- 主知识点：图遍历 BFS / DFS，`kp-graph-bfs-dfs`
- 演示案例：`DEMO-GRAPH-001`

## 2. 演示前基线

- 2 个班，每班 40 人，共 80 名学生。
- 30 天历史学习事实；不是 80 人都具有完整干预闭环。
- 小明属于 1 班，并有可复现历史状态：mastery 0.62、confidence 0.71、forgettingRisk 0.57。
- 数据来源必须标记：`BASELINE_SIMULATED`。

## 3. 现场增量链

`历史状态 → AI Coach → 2道诊断题 → AI互动/相似题 → 错题本 → 学习状态更新 → SmartBI第一次分析 → AIChat三个建议 → 教师确认 → Intervention → Assignment → 学生专项练习 → TransferValidation → A/B Outcome → SmartBI第二次分析 → Smart Report → Growth`

## 4. 第一次 SmartBI 分析应能看到

- 小明当前 mastery、confidence、forgettingRisk。
- 诊断题正确率、错题数量和主要误概念。
- 班级平均 mastery 与同类风险学生，形成个人与 cohort 的参照。

## 5. 教师三个方案的事实边界

SmartBI AIChat 只生成 `AnalysisRecommendation`：标题、理由、行动描述、证据摘要。

B 域正式化后才可产生：
- `predictedLift`
- `predictionInterval`
- `Intervention`
- `InterventionAssignment`

教师必须人工确认。

## 6. 第二次 SmartBI 分析应能看到

- 干预前后 mastery / practice accuracy / forgettingRisk。
- TransferValidation 结果。
- predictedLift、actualLift、predictionDeviation。
- 方案在班级历史 cohort 中的效果对比。

## 7. 重放规则

- Baseline 永不因 Demo Reset 被删除。
- 每轮 Demo 生成新的 `demoRunId`。
- SmartBI 当前演示视图 = Baseline + 当前 active demo run。
- 历史 demo run 不得重复计入当前班级/方案聚合。
