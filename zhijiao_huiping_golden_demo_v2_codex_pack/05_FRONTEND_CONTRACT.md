# 前端契约 V2

## 1. 原则

- 页面只编排业务，不计算 mastery / predictedLift / actualLift。
- `api/*` 只负责 HTTP；`adapters/*` 把 Transport DTO 转为 ViewModel；页面不展示 raw DTO。
- UI 组件默认纯 props/events，不直接调用 Axios。
- Mock 与 HTTP 必须进入同一 ViewModel，不能出现“Mock 漂亮、HTTP 变调试页”。
- UUID、correlationId 等默认隐藏在 Trace Drawer。
- SmartBI 聚合分析不在 Vue 重画。

## 2. 路由边界

### Student
- `/student/today` → `StudentTodayPage`
- `/student/ai-coach` → `StudentAiCoachPage`
- `/student/ai-coach/:sessionId` → 同页恢复会话
- `/student/practice/:practiceSetId` → `StudentPracticeRunnerPage`
- `/student/practice/:practiceSetId/result` → `StudentPracticeResultPage`
- `/student/wrong-book` → `StudentWrongBookPage`
- `/student/growth` → `StudentGrowthPage`
- `/student/resources` → `StudentResourcesPage`

### Teacher
- `/teacher/workbench` → `TeacherWorkbenchPage`
- `/teacher/students/:studentId` → `TeacherStudentProfilePage`
- `/teacher/diagnosis-cases/:caseId` → `TeacherDiagnosisCasePage`
- `/teacher/interventions/:interventionId` → `TeacherInterventionPage`
- `/teacher/analytics` → `TeacherSmartBiCenterPage`
- `/teacher/analytics/:assetKey` → `TeacherSmartBiAssetPage`

### Admin
- `/admin/knowledge` → `AdminKnowledgePage`
- `/admin/platform` → `AdminPlatformPage`

## 3. Golden Demo 页面顺序

`AI Coach → Practice(2题) → WrongBook/Similar → SmartBI student-risk → Teacher Intervention → Student Today Assignment → Practice/Transfer → SmartBI intervention-outcome → Growth`

## 4. 页面组件边界

### StudentAiCoachPage
只负责：会话、学习上下文、触发诊断、展示引用、跳转 practice。诊断题真实作答在 Practice Runner，不在聊天文本里伪造。

### StudentPracticeRunnerPage
负责：题目、结构化提交、即时反馈、相似题入口、加入错题本。每次 attempt 必须有 attemptId。

### TeacherInterventionPage
负责：展示 SmartBI Recommendation、教师选择、B EffectEstimator 结果、approve/commit/assignment/outcome 状态。

### TeacherSmartBiAssetPage
只负责平台入口、加载/权限/Unavailable 状态；真实图表属于 SmartBI Cloud。launchMode 支持 `NEW_TAB | IFRAME | UNVERIFIED`，默认不假定 iframe。

## 5. Store

- `authStore`
- `studentContextStore`
- `coachStore`
- `practiceStore`
- `wrongBookStore`
- `growthStore`
- `teacherWorkStore`
- `interventionStore`
- `smartbiStore`

Store 只保存前端状态和 server cache，不复制业务算法。

## 6. API Adapter

目录示例：

```text
src/api/http.ts
src/api/student/*
src/api/teacher/*
src/api/admin/*
src/adapters/student/*
src/adapters/teacher/*
src/types/contracts/*
```

所有响应先用 Zod 校验，再进入 Adapter。

## 7. UI 状态

每个异步页面必须支持：

- `INITIAL`
- `LOADING`
- `READY`
- `EMPTY`
- `SUBMITTING`
- `SUCCESS`
- `STALE`
- `DEGRADED`
- `FORBIDDEN`
- `ERROR`

禁止把后端异常字符串直接显示给用户。

## 8. 页面级异常

- AI/LLM 失败：保留会话和输入，显示“AI 服务暂不可用”，允许重试；不得丢 attempt。
- RAG 无结果：AI 可以回答，但必须显示“未检索到课程资料”，不得伪造 citation。
- Practice 提交冲突：禁止重复提交，重新获取 attempt 状态。
- SmartBI 未配置：显示“分析平台尚未配置/权限未开放”，不 fallback 到 Vue 假图。
- SmartBI 新窗口模式：保留返回业务系统按钮和当前 student/course context 提示。
- 网络中断：已成功的 server state 不回滚成失败。

## 9. 导航

Student：今日、AI学习教练、定向练习、错题本、我的成长、学习资料；消息进入右上角通知中心。

Teacher：工作台、数据洞察、干预决策、干预结果、课程资源；学生画像与诊断案例作为二级路由。

## 10. V3 F00 alignment

- `/student/practice` is the Practice Hub entry. It composes current Today, Coach, and Wrong Book state and never fabricates practice history.
- Teacher一级导航固定为：工作台、数据洞察、干预决策、干预结果、课程资源。学生画像与诊断案例保留为二级路由。
- Diagnostic `questionCount` is fixed at 2, while correct count, incorrect count, and accuracy are runtime-driven from real attempts and completion outcomes.
- SmartBI uses verified IFRAME-first assets. `NEW_TAB` and `UNVERIFIED` are explicit fallbacks, and AIChat recommendations remain `MANUAL_CAPTURE`; Vue never redraws SmartBI analytics or reads cross-origin iframe DOM.
- Student/Teacher resource upload is blocked by the current knowledge upload permission gap. Neither role may call `/admin/knowledge/*` until a role-scoped contract exists.
