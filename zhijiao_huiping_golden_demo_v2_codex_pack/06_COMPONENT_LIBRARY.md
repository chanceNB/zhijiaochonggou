# 智教慧评组件库 V1

目标风格：清爽教育科技风；浅色、蓝/蓝紫主色，青绿/橙黄/错误红语义色，轻圆角、轻边框、适度留白。

## 1. 基础组件

| 组件 | 责任 | 核心 Props / Events |
|---|---|---|
| `AppShell` | 应用主框架 | `role`, `navItems`; slot |
| `SideNav` | 角色导航 | `items`, `activeKey`; `navigate` |
| `TopBar` | 搜索、通知、身份 | `user`, `notificationCount` |
| `PageHeader` | 标题/说明/动作 | `title`, `subtitle`, actions slot |
| `UiButton` | 按钮 | `variant,size,loading,disabled`; `click` |
| `UiCard` | 卡片 | `title,padding`; slots |
| `StatusBadge` | 状态语义 | `status,label` |
| `MetricCard` | 单指标展示 | `label,value,unit,delta,trend` |
| `EmptyState` | 空态 | `title,description,actionLabel` |
| `ErrorState` | 业务错误 | `title,description,retryable`; `retry` |
| `LoadingSkeleton` | 骨架屏 | `variant,rows` |
| `AppDrawer` | 追踪/证据侧栏 | `open,title`; `close` |
| `AppDialog` | 确认 | `open,title,confirmLoading` |
| `ToastHost` | 全局反馈 | message bus |

## 2. 学生业务组件

| 组件 | 责任 |
|---|---|
| `LearningActionCard` | Today 下一最佳行动 |
| `TeacherAssignmentCard` | 教师下发的 InterventionAssignment |
| `LearningPathStepper` | 今日路径 |
| `CoachConversationList` | AI Coach 会话列表 |
| `CoachMessageBubble` | 消息 |
| `LearningContextPanel` | mastery/薄弱点/RAG上下文 |
| `RagCitationCard` | 课程资料引用 |
| `DiagnosticStartCard` | AI Coach 发起诊断 |
| `QuestionCard` | 结构化题目 |
| `AnswerOptionGroup` | 选择/判断等答案 |
| `AnswerFeedbackPanel` | 即时反馈 |
| `SimilarQuestionAction` | 请求相似题 |
| `WrongBookAction` | 显式加入错题本 |
| `PracticeProgress` | 练习进度 |
| `TransferValidationCard` | 迁移验证 |
| `GrowthMetricCard` | 成长指标 |
| `GrowthTimeline` | 学习/干预变化时间线 |

## 3. 教师业务组件

| 组件 | 责任 |
|---|---|
| `TeacherTaskCard` | 工作台待办 |
| `StudentRiskSummary` | 单学生业务摘要，非 BI 聚合 |
| `EvidenceCard` | 支持/反向证据 |
| `HypothesisCard` | 诊断假设 |
| `RecommendationSummary` | SmartBI 分析摘要 |
| `StrategyCard` | A/B/C候选方案 |
| `EffectEstimateCard` | B估计 predictedLift/区间 |
| `TeacherDecisionPanel` | 教师理由 + 选择 |
| `InterventionLifecycleStepper` | proposed→approved→committed→assigned→completed |
| `OutcomeComparisonCard` | predicted vs actual |
| `SmartBiAssetCard` | SmartBI 资产入口 |
| `DataFreshnessBadge` | 数据新鲜度 |

## 4. 组件边界

- 基础组件：无业务 API。
- 业务组件：只接收 ViewModel；不得直接 Axios。
- Page：调用 composable/store 并拼装组件。
- `MetricCard` 只能展示后端/SmartBI 给定指标，禁止在组件中推导业务核心指标。

## 4.1 F00 V2 additions

- Base surface additions: `TraceDrawer`, `CoachSessionRail`, `CoachConversation`, `CoachComposer`, `PracticeHubCard`, `PracticeResultSummary`, `WrongQuestionList`, and `ResourceContractState`.
- Teacher additions: `CurrentStudentContext`, `TeacherProcessStepper`, `AssignmentStatusCard`, `LearningStateDeltaTable`, `SmartBiEmbedPanel`, `SmartBiAiChatPanel`, `RecommendationCaptureDrawer`, `DataFreshnessBadge`, and `CourseResourceContractState`.
- `SmartBiEmbedPanel` only renders an API-provided `resourceUrl` according to `launchMode = IFRAME | NEW_TAB | UNVERIFIED`; it never generates local charts or credentials.
- Business components receive ViewModels only. API calls remain in pages/stores, and resource upload components must surface the knowledge permission gap instead of calling admin endpoints.

## 5. 必须统一的业务文案

- SmartBI 未验证：`分析平台能力待验证`
- SmartBI 未配置：`分析平台尚未配置`
- AI/RAG 无资料：`未检索到课程资料，本次回答未引用课程知识库`
- 无数据：`暂时没有可展示的数据`
- 权限不足：`当前账号没有访问该资源的权限`

## 6. 可访问性

- 所有交互元素键盘可达。
- Icon button 必须 aria-label。
- 错误不能仅依赖颜色。
- 对话、题目、结果区有明确 heading 层级。
