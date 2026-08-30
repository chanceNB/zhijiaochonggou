# UI Design Freeze V1

## 1. 统一设计语言

视觉必须延续旧项目真实页面，而不是重新设计一套 AI SaaS。

### Token

- page bg: `#F5F7FB` / `#F7FAFF`
- student bg: `#F5F9FF`
- card: `#FFFFFF`
- primary: `#2563EB`
- student primary: `#4F8EF7`
- active/soft: `#EDF4FF` / `#EEF2FF`
- AI/success: `#0FA779`
- warning: `#F59E0B`
- risk: `#EF4444`
- text: `#172238`
- secondary: `#6E7C92`
- border: `#E2E8F1`
- border strong: `#D3DCE9`
- card radius: `8-10px`
- shadow: `0 5px 18px rgb(37 61 99 / 7%)`

字体：
`Noto Sans SC, PingFang SC, Microsoft YaHei, sans-serif`

禁止：
- 大面积渐变
- 玻璃拟态
- 霓虹/黑色大屏
- 20~30px 大圆角
- 重阴影
- 巨大 Hero
- 过度卡通 AI

## 2. Student Shell

视觉参考：
- `legacy_visual_reference/src/layouts/StudentLayout.visual.vue`
- `legacy_visual_reference/src/components/student/layout/StudentSidebar.visual.vue`

尺寸：
- sidebar: 186px
- collapsed: 76px
- nav row: 48px
- radius: 8px
- active: `#EEF2FF + #2F63DC`

Student nav：
1. 今日学习
2. AI学习教练
3. 定向练习
4. 错题本
5. 我的成长
6. 学习资料

Today：
- 复用旧 Today 高信息密度双栏
- Today 可隐藏常规 topbar

AI Coach：
- 72px topbar
- 会话列 + 主对话 + 学习上下文右栏

Practice：
- 紧凑 58px topbar
- 主内容优先做题，不做大型 Dashboard

## 3. StudentTodayPage

职责：
“当前是什么状态 + 下一步干什么”。

主结构：
- LearningActionCard / TeacherAssignmentCard 二选一
- 当前 Learning State
- 今日学习路径
- 右栏：目标/进度/错题提醒/最近动态

初始：
- API `nextAction` 驱动 CTA → AI Coach

Assignment 到达：
- `teacherAssignment != null` 驱动 CTA → PracticeSet
- 不再显示 AI diagnostic 为首要行动

所有数字来自 API。

## 4. StudentAiCoachPage

布局：
- 左：当前/最近会话（只展示真实可恢复内容）
- 中：CoachConversation + Composer
- 右：LearningContextPanel / RAG引用 / 错题提醒

回答：
- assistantMessage
- citations
- actions

RAG 无命中：
显示标准文案，不伪造 citation。

START_DIAGNOSTIC：
出现浅蓝 `DiagnosticStartCard`。
点击创建 2 题 PracticeSet 并跳 Practice Runner。

## 5. Practice Hub / Runner / Result

Hub：
- 当前 AI 诊断
- Teacher Assignment
- 错题巩固
- 不伪造练习历史

Runner：
- QuestionCard
- AnswerOptionGroup
- PracticeProgress
- 提交后 `AnswerFeedbackPanel`

错误/正确后允许：
- 和 AI 教练讨论当前题
- 若 `canGenerateSimilar=true` → 生成类似题
- 若 `canAddWrongBook=true` → 加入错题本

所有动作依赖真实 `attemptId`。

Result：
- 使用 `complete` 返回 accuracy/outcome
- 诊断结果不写死 1/2
- Teacher Assignment 才显示 transferValidation / intervention outcome

## 6. WrongBook

视觉复用旧错题本：
- 状态 tab
- 知识点筛选
- 错题列表
- 右侧/Drawer 错误分析
- review / AI讨论 / 相似题动作

## 7. Growth

学生语气：
“我学会了什么”。

展示：
- mastery
- trend
- completedTasks
- repairedMisconceptions
- latestIntervention（如存在）
- before/after + transferValidation

不把 predictionDeviation 做学生主视觉。

## 8. Student Resources

视觉可复用旧 KnowledgeBase：
- 资料摘要
- 资料列表
- 索引状态
- 上传入口预留

但当前 student upload API 不存在：
- 不调用 admin API
- 未扩约前显示契约受限状态
- 该功能不阻塞 Golden Demo

---

# Teacher Design

## 9. Teacher Shell

视觉参考：
- `TeacherLayout.visual.vue`
- `TeacherSidebar.visual.vue`
- `TeacherTopHeader.visual.vue`
- `teacher-overhaul.scss`

尺寸：
- sidebar 230px
- collapsed 78px
- header 56px
- content max 1440px
- content padding `22px 24px 34px`

Teacher nav：
1. 工作台
2. 数据洞察
3. 干预决策
4. 干预结果
5. 课程资源

Student Profile / Diagnosis 是二级详情，不占一级主链。

## 10. Teacher Workbench

定位：
当前教学 Case 控制台，Demo 聚焦小明，但组件不硬编码“小明”。

回答：
- 当前学生是谁
- 目前学习状态
- 为什么需要关注
- 流程走到哪一步
- 下一步去哪

结构：
- 当前教学对象 Context
- 小型 KPI（实际 profile 数据）
- 当前学习问题
- 当前教学流程 Stepper
- 最近业务事实（从真实 profile/attempt/diagnosis 组合，不造日志）
- 诊断摘要
- 唯一主 CTA → 数据洞察

不做：
- 班级排行榜
- 班级高风险总览
- Vue cohort BI

## 11. Teacher Data Insights

定位：
本地教师系统中的 SmartBI 内嵌工作区。

顶部：
- current student/course/KP context
- DataFreshnessBadge
- tabs:
  - 学习风险分析
  - AI数据分析
  - 干预效果分析

### 学习风险分析
主体 60%~70% 空间给真实 SmartBI Dashboard iframe。

### AI数据分析
SmartBI AIChat 验证可 iframe 时内嵌。
外层本地工具条：
- 记录分析建议
- MANUAL_CAPTURE Drawer

### 干预效果分析
学生完成 Assignment 后打开真实 `intervention-outcome` Dashboard。

## 12. Intervention Decision

生命周期：
SmartBI分析
→ 方案选择
→ EffectEstimator
→ 教师批准
→ 下发任务

先展示 Recommendation 3 个 StrategyCard。
教师选择 + teacherRationale 后调用 create Intervention。
只有后端返回后才出现：
- predictedLift
- predictionInterval
- PROPOSED

然后：
- approve（If-Match）
- commit（If-Match）
- assignment

AIChat 绝不能直接产生 predictedLift。

## 13. Intervention Result

Pending：
- Assignment 已下发
- 等待学生
- 不显示假 Outcome

Completed：
- before/after mastery
- actualLift
- predictedLift
- predictionDeviation
- practiceAccuracyAfter
- transferValidation
- confidence before/after
- forgettingRisk before/after
- evidenceCount before/after
- 真实执行状态

唯一主 CTA：
→ 数据洞察 / 干预效果分析

## 14. Course Resources

视觉延续旧教师知识库页。
当前 API 权限未收口前不绕过 `/admin/knowledge/*`。
功能性上传属于后续契约扩展。
