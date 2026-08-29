# 领域边界与跨模块契约

## 1. 事实所有权

### A / Student
拥有：LearningEvent、StudentLearningSnapshot、PracticeAttempt、PracticeOutcome、WrongBookItem、TransferValidation、Growth。

### B / Teacher
拥有：DiagnosisCase、Evidence、Intervention、InterventionAssignment、InterventionOutcome、predictedLift、actualLift、predictionDeviation。

### C / Platform
拥有：Knowledge/RAG、Analytics Exchange、SmartBI 平台资产元数据、数据 Freshness。

### SmartBI Cloud
拥有：平台内 ETL、Data Model、Metric Model、Dashboard、AIChat 会话、Attribution、Smart Report 资产；不拥有业务写事实。

## 2. Published Facts

- `StudentLearningFact`
- `PracticeAttemptFact`
- `WrongBookFact`
- `DiagnosisFact`
- `InterventionFact`
- `InterventionOutcomeFact`

每个事实必须包含：`eventId,eventTime,sourceVersion,correlationId,demoCaseId,demoRunId,dataOrigin`。

## 3. AnalysisRecommendation

SmartBI AIChat 或人工 capture 进入项目时统一为：

```json
{
  "recommendationId": "rec-001",
  "studentId": "stu-xiaoming",
  "courseId": "course-data-structures",
  "classId": "class-cs-2024-01",
  "knowledgePointId": "kp-graph-bfs-dfs",
  "analysisSummary": "BFS/DFS访问与回溯概念混淆",
  "evidenceRefs": ["attempt-101", "attempt-102"],
  "candidates": [
    {"strategyCode":"CONCEPT_REMEDIATION","title":"方案A","rationale":"先纠正概念边界","actionDescription":"概念辨析+低难度专项"},
    {"strategyCode":"VISUAL_TRANSFER_PRACTICE","title":"方案B","rationale":"可视化过程+迁移题","actionDescription":"BFS/DFS过程演示+变式练习"},
    {"strategyCode":"AI_GUIDED_VARIATION","title":"方案C","rationale":"强化个性化反馈","actionDescription":"AI Coach引导+分层变式"}
  ],
  "source": "SMARTBI_AICHAT",
  "generatedAt": "2026-08-29T08:00:00Z"
}
```

禁止 AnalysisRecommendation 自带 approved、assignmentId、actualLift。

## 4. B 正式化

老师选择候选后：

`RecommendationReview -> EffectEstimator -> Intervention(PROPOSED) -> approve -> commit -> Assignment`

`predictedLift` 由 B 的 EffectEstimator 基于历史相似 cohort 估计，不接受 AIChat 随口生成值作为权威事实。

## 5. Trace

Golden Demo 全链必须保留：

`demoCaseId = DEMO-GRAPH-001`
`demoRunId = 每轮唯一`
`correlationId = 单业务链一致`

前端默认不显示 UUID，只在 Trace Drawer / 管理视图中展示。
