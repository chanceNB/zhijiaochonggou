# SmartBI 数据交换与数据模型输入规范

## 1. Canonical Source Datasets

SmartBI 只读取 `smartbi_exchange` schema，S0 冻结以下 10 个数据集：

### Dimensions
1. `sb_dim_course`
2. `sb_dim_class`
3. `sb_dim_student`
4. `sb_dim_knowledge_point`

### Facts
5. `sb_fact_learning_state`
6. `sb_fact_practice_attempt`
7. `sb_fact_wrong_book`
8. `sb_fact_diagnosis`
9. `sb_fact_intervention`
10. `sb_fact_intervention_outcome`

辅助：`sb_data_freshness`、`sb_demo_run_state`。

## 2. 共同审计字段

事实表至少包含：

- `event_id`
- `event_time`
- `student_id`
- `course_id`
- `class_id`
- `knowledge_point_id`
- `data_origin` = `BASELINE_SIMULATED | LIVE_DEMO | PRODUCTION`
- `demo_run_id`
- `demo_case_id`
- `correlation_id`
- `source_version`
- `ingested_at`

## 3. 事实字段

### sb_fact_learning_state
`mastery,confidence,ability,forgetting_risk,misconception_code`

### sb_fact_practice_attempt
`practice_set_id,question_id,question_source,difficulty,correct,duration_seconds,coach_session_id,assignment_id,intervention_id`

### sb_fact_wrong_book
`wrong_item_id,question_id,reason,status,review_count,added_at,repaired_at`

### sb_fact_diagnosis
`case_id,status,severity,confidence,hypothesis_code`

### sb_fact_intervention
`intervention_id,recommendation_id,strategy_code,status,predicted_lift,prediction_low,prediction_high,assignment_id`

### sb_fact_intervention_outcome
`intervention_id,transfer_validation,actual_lift,prediction_deviation,practice_accuracy_after,mastery_after`

## 4. Baseline + Live

- Baseline：2 班 × 40 人，30 天历史。
- 小明在 baseline 中已存在，不额外复制学生。
- Live Demo 写入同一事实表，`data_origin=LIVE_DEMO`，带当前 `demo_run_id`。
- `sb_demo_run_state` 指向当前 active run；CSV export 只导出 Baseline + active run。

## 5. SmartBI 内部建模建议

SmartBI 平台内：

- ETL：类型清洗、时间维度、事实关联、异常值校验、active demo run 选择。
- Data Model：Student / Class / Course / KnowledgePoint 维度关联各事实。
- Metric：AVG(mastery)、practice accuracy、wrong item count、risk student count、intervention effective rate 等。
- 禁止重新定义业务事实 mastery、forgettingRisk、predictedLift、actualLift。

## 6. 四个比赛 Dashboard

- `governance`：课程/班级整体治理。
- `course-diagnosis`：知识点与班级诊断。
- `student-risk`：Golden Demo 第一次 SmartBI 主页面，个人 + cohort。
- `intervention-outcome`：Golden Demo 第二次 SmartBI 主页面，干预前后与方案 cohort。

## 7. 数据接入策略

优先验证：SmartBI Cloud 数据库网关连接本地 PostgreSQL read-only user。

保底：通过导出任务生成 10 个 canonical CSV + manifest.json 上传 SmartBI。

数据库网关是否在挑战杯租户开放属于 ONLINE BUILD GATE；不得用通用产品能力假定租户权限。
