# 前端契约 V3（建议合并到当前 05_FRONTEND_CONTRACT.md）

## 1. 原则

- 页面只编排业务，不计算 `mastery / predictedLift / actualLift / predictionDeviation`。
- `api/*` 只负责 HTTP；响应 Zod 校验后由 `adapters/*` 转 ViewModel。
- UI 组件纯 props/events，不直接 Axios。
- UUID/correlationId/demoRunId 默认隐藏到 Trace Drawer。
- SmartBI 聚合分析不在 Vue 重画。
- 诊断固定 2 题，但正确/错误/accuracy 完全按运行时真实数据。
- 旧项目只作为视觉真源，禁止迁旧业务。

## 2. Student Routes

- `/student/today` → StudentTodayPage
- `/student/ai-coach` → StudentAiCoachPage
- `/student/ai-coach/:sessionId` → 同页；无 server restore 时允许 DEGRADED
- `/student/practice` → StudentPracticeHubPage
- `/student/practice/:practiceSetId` → StudentPracticeRunnerPage
- `/student/practice/:practiceSetId/result` → StudentPracticeResultPage
- `/student/wrong-book` → StudentWrongBookPage
- `/student/growth` → StudentGrowthPage
- `/student/resources` → StudentResourcesPage

Student 一级导航：
今日学习、AI学习教练、定向练习、错题本、我的成长、学习资料。
消息进入右上通知中心。

## 3. Teacher Routes

- `/teacher/workbench` → TeacherWorkbenchPage
- `/teacher/students/:studentId` → TeacherStudentProfilePage（二级）
- `/teacher/diagnosis-cases/:caseId` → TeacherDiagnosisCasePage（二级）
- `/teacher/analytics` → TeacherSmartBiCenterPage
- `/teacher/analytics/:assetKey` → TeacherSmartBiAssetPage
- `/teacher/interventions` → TeacherInterventionPage（decision entry）
- `/teacher/interventions/:interventionId` → TeacherInterventionPage（lifecycle）

Teacher 一级导航：
工作台、数据洞察、干预决策、干预结果、课程资源。

“干预决策/干预结果”允许复用同一 lifecycle route 的不同 section。
没有 active recommendation/intervention 时显示 EMPTY，不制造数据。

## 4. Student Page Boundaries

AI Coach：
- 会话
- current learning context
- real RAG citation
- trigger diagnostic
- jump Practice
- 不在聊天气泡伪造答题

Practice Hub：
- today action
- teacher assignment
- current coach PracticeSet
- wrongbook reinforcement
- 无 backend list 时不伪造练习历史

Runner：
- question
- submit attempt
- immediate feedback
- AI discussion context
- similar question
- wrongbook
- attemptId 必须真实

Result：
- complete outcome
- accuracy runtime-driven
- teacher assignment 才展示 transfer/outcome

## 5. Teacher Page Boundaries

Workbench：
- 当前 demo case / 当前学生业务上下文
- 非班级 BI

Data Insights：
- SmartBI IFRAME-first
- NEW_TAB/UNVERIFIED fallback
- AIChat MANUAL_CAPTURE
- 不做决策

Intervention：
- Recommendation
- Teacher selection/rationale
- EffectEstimator server result
- approve
- commit/assignment
- outcome section

Profile/Diagnosis：
- 作为 Workbench 二级证据页

## 6. SmartBI Policy

`launchMode = IFRAME | NEW_TAB | UNVERIFIED`

实现偏好：
- verified Dashboard → IFRAME
- verified AIChat → IFRAME
- iframe 不可用 → NEW_TAB
- 未验证 → UNVERIFIED

前端只使用 Asset API `resourceUrl`，不硬编码 resid/凭据。

AIChat iframe 不得跨域抓 DOM。
Recommendation 继续 MANUAL_CAPTURE。

## 7. Stores

- authStore
- studentContextStore
- coachStore
- practiceStore
- wrongBookStore
- growthStore
- teacherWorkStore
- interventionStore
- smartbiStore

Store 仅保存 UI state/server cache。
允许把“当前 active route context ID”放 sessionStorage，但不能把业务事实当本地权威。

## 8. UI State

INITIAL / LOADING / READY / EMPTY / SUBMITTING / SUCCESS / STALE / DEGRADED / FORBIDDEN / ERROR

409/412：
重新获取 server state，不覆盖。

SmartBI 失败：
不回滚学生/教师已成功业务。

## 9. Knowledge Resource Gap

当前正式 CRUD 为 `/admin/knowledge/*`。
Student/Teacher 资源上传在新增权限契约前不得直接调用 Admin API。
