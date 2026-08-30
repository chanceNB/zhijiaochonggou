# F01 — Student Today + AI Coach

目标：
完成学生主链前半段入口：
Today → AI Coach → 创建真实 2 题 diagnostic PracticeSet。

## Read first

- API Student 2.5.2.1 ~ 2.5.2.6
- frontend contract V3
- legacy StudentToday / StudentAiCoach visual refs

## Architecture

创建：
- api/student/today
- api/student/coach
- types/contracts/student
- adapters/student/today
- adapters/student/coach
- stores/studentContextStore
- stores/coachStore
- StudentTodayPage
- StudentAiCoachPage
- 纯展示业务组件

所有响应 Zod parse。

## Today

GET `/student/today`

展示：
- actual learningState
- actual nextAction
- teacherAssignment if exists
- learning path
- compact right rail

规则：
- no assignment → AI Coach CTA
- assignment → TeacherAssignmentCard CTA
- 不硬编码学生名/百分比

## AI Coach

POST create session。
POST messages。

布局必须贴近旧项目：
- session rail
- main conversation
- right learning context
- citations
- diagnostic action

若 API 没有历史 GET：
- 当前浏览器 session 可保留
- fresh restore 显示 DEGRADED
- 不伪造历史会话

RAG：
- citations 有 → RagCitationCard
- 无 → 标准 no-citation 文案
- RAG unavailable → 保留会话，显式 degraded

LLM：
- 失败不清空输入/会话
- retry

## Diagnostic

START_DIAGNOSTIC → POST diagnostic-sets:
- questionCount 固定 2
- knowledgePointId 来自实际 context
- Idempotency-Key
- 成功后 router push `/student/practice/{practiceSetId}`

这里不做题。

## Do not

- 不实现 Practice Runner
- 不写假 AI 回复
- 不用 old mock 当 HTTP fallback
- 不写“1对1错”

## Tests

RED → GREEN：
- Today no assignment
- Today assignment
- citation yes/no
- LLM degraded
- RAG degraded
- diagnostic returns practiceSetId and navigates
- diagnostic questionCount exactly 2
- no hard-coded outcome text

Acceptance：
test/typecheck/build PASS。
浏览器真实 API 验证 Today + Coach + diagnostic navigation。

Commit:
`feat(frontend): connect student today and ai coach`
