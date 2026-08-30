# 智教慧评组件库 V2（建议合并）

## Base

- AppShell
- SideNav
- TopBar
- PageHeader
- UiButton
- UiCard
- StatusBadge
- MetricCard
- EmptyState
- ErrorState
- LoadingSkeleton
- AppDrawer
- AppDialog
- ToastHost
- TraceDrawer

## Student

- LearningActionCard
- TeacherAssignmentCard
- LearningPathStepper
- CoachSessionRail
- CoachConversation
- CoachMessageBubble
- CoachComposer
- LearningContextPanel
- RagCitationCard
- DiagnosticStartCard
- PracticeHubCard
- QuestionCard
- AnswerOptionGroup
- AnswerFeedbackPanel
- DiscussWithCoachAction
- SimilarQuestionAction
- WrongBookAction
- PracticeProgress
- PracticeResultSummary
- TransferValidationCard
- WrongQuestionList
- GrowthMetricCard
- GrowthTimeline
- ResourceContractState

## Teacher

- CurrentStudentContext
- TeacherProcessStepper
- StudentRiskSummary
- EvidenceCard
- HypothesisCard
- RecommendationSummary
- StrategyCard
- EffectEstimateCard
- TeacherDecisionPanel
- InterventionLifecycleStepper
- AssignmentStatusCard
- OutcomeComparisonCard
- LearningStateDeltaTable
- SmartBiAssetCard
- SmartBiEmbedPanel
- SmartBiAiChatPanel
- RecommendationCaptureDrawer
- DataFreshnessBadge
- CourseResourceContractState

## Boundary

- Base：无业务 API
- Business Component：只接 ViewModel
- Page：store/composable 编排
- API：HTTP only
- Adapter：DTO → ViewModel
- SmartBiEmbedPanel：只渲染真实资源，不生成分析图

## Visual

必须参考旧项目：
- 轻边框
- 8~10px radius
- restrained shadow
- 高信息密度
- 蓝/绿/橙/红语义
