# Route / API / ViewModel Matrix

## Student

| Route | Page | 真实 API | 关键 ViewModel |
|---|---|---|---|
| `/student/today` | StudentTodayPage | GET `/student/today` | TodayVm |
| `/student/ai-coach` | StudentAiCoachPage | POST `/student/coach/sessions` | CoachSessionVm |
| `/student/ai-coach/:sessionId` | 同页 | 当前 session store；无 GET 历史接口时不伪恢复 | CoachSessionVm |
| `/student/practice` | StudentPracticeHubPage | GET `/student/today`, GET `/student/wrong-book` | PracticeHubVm |
| `/student/practice/:practiceSetId` | StudentPracticeRunnerPage | GET practice-set, POST attempts | PracticeSetVm / AttemptFeedbackVm |
| `/student/practice/:practiceSetId/result` | StudentPracticeResultPage | POST complete | PracticeResultVm |
| `/student/wrong-book` | StudentWrongBookPage | GET wrong-book, POST review | WrongBookVm |
| `/student/growth` | StudentGrowthPage | GET `/student/growth` | GrowthVm |
| `/student/resources` | StudentResourcesPage | 当前无 student-scoped CRUD | ResourceContractStateVm |

### Student commands

- Create session: Idempotency-Key
- Send Coach message: 按当前接口/实现执行
- Create diagnostic: Idempotency-Key
- Similar question: Idempotency-Key，必须 sourceAttemptId
- Attempt: Idempotency-Key，真实 attemptId
- Complete: Idempotency-Key
- Add WrongBook: Idempotency-Key，重复添加显示同一业务结果

## Teacher

| Route | Page | 真实 API | ViewModel |
|---|---|---|---|
| `/teacher/workbench` | TeacherWorkbenchPage | GET workbench + profile/diagnosis按需 | WorkbenchVm |
| `/teacher/students/:studentId` | TeacherStudentProfilePage | GET profile | StudentProfileVm |
| `/teacher/diagnosis-cases/:caseId` | TeacherDiagnosisCasePage | GET diagnosis | DiagnosisCaseVm |
| `/teacher/analytics` | TeacherSmartBiCenterPage | freshness + assets | SmartBiCenterVm |
| `/teacher/analytics/:assetKey` | TeacherSmartBiAssetPage | asset detail | SmartBiAssetVm |
| `/teacher/interventions` | TeacherInterventionPage | recommendationId query/teacherWorkStore | InterventionDecisionVm |
| `/teacher/interventions/:interventionId` | 同页 lifecycle | GET intervention + outcome | InterventionLifecycleVm |

### Teacher commands

1. POST `/teacher/analysis-recommendations`
   - MANUAL_CAPTURE
   - candidates 固定 3
   - 不允许声称 SmartBI callback

2. POST `/teacher/interventions`
   - recommendationId
   - strategyCode
   - teacherRationale >= 10
   - 后端返回 predictedLift / predictionInterval / PROPOSED

3. POST `/teacher/interventions/{id}/approve`
   - `If-Match` current version

4. POST `/teacher/interventions/{id}/commit`
   - `If-Match` current version
   - 返回 Assignment

5. GET `/teacher/interventions/{id}/outcome`
   - 只有真实 Outcome 后展示结果

## SmartBI

- GET `/analytics/smartbi/freshness`
- GET `/integrations/smartbi/assets`
- GET `/integrations/smartbi/assets/{assetKey}`

前端只根据：
- status
- launchMode
- resourceUrl

决定渲染。

IFRAME：
`<iframe :src="resourceUrl">`

NEW_TAB：
平台入口卡 + 新窗口打开

UNVERIFIED：
“分析平台能力待验证”

严禁本地拼 resid 后猜 URL；正式 resourceUrl 由平台验证后的 asset 配置提供。
