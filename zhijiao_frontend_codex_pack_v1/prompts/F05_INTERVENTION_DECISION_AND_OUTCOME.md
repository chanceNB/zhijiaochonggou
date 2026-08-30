# F05 — Teacher Intervention Decision + Outcome

目标：
实现 Recommendation → Teacher → EffectEstimator → Approve → Commit → Assignment → Outcome。

## Routes

- `/teacher/interventions`
- `/teacher/interventions/:interventionId`

同一页面可分 section：
- decision
- outcome

Sidebar “干预决策 / 干预结果”是上下文链接。
没有上下文时 EMPTY，不造 ID。

## Decision

GET recommendation snapshot。

展示 3 StrategyCard。
教师选择：
- strategyCode
- teacherRationale >= 10

POST `/teacher/interventions`
Idempotency-Key。

服务器返回后才显示：
- interventionId
- predictedLift
- predictionInterval
- status PROPOSED
- version

EffectEstimateCard 必须注明：
“本结果由本地 EffectEstimator 生成，用于辅助教师决策。”

## Approve

POST approve
- If-Match current version

412：
refetch，提示 server state updated。

## Commit

POST commit
- If-Match current version

展示真实 assignment：
- assignmentId
- practiceSetId
- status

等待学生时 outcome section：
PENDING，不显示假 Before/After。

## Outcome

GET `/teacher/interventions/{id}/outcome`

只有成功后显示：
- masteryBefore/After
- confidenceBefore/After
- forgettingRiskBefore/After
- evidenceCountBefore/After
- practiceAccuracyAfter
- predictedLift
- actualLift
- predictionDeviation
- transferValidation

不隐藏负向/下降指标。
不声称单次结果代表长期因果。

主 CTA：
→ `/teacher/analytics` 干预效果分析 context。

## Tests

- 3 candidates
- select + rationale validation
- create PROPOSED
- approve version
- commit version
- 409/412
- pending outcome
- completed outcome
- values all from server
- no client predicted/actual calculation

Acceptance:
test/typecheck/build PASS。
真实后端状态机走通。

Commit:
`feat(frontend): implement teacher intervention lifecycle`
