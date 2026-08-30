# F03 — Teacher Workbench + Secondary Profile/Diagnosis

目标：
教师端进入系统后立即围绕当前学生 Case 工作，不回到班级 Dashboard。

## Workbench

GET `/teacher/workbench`

从真实 priority/context 解析 current student。
按需 GET：
- `/teacher/students/{studentId}/profile`
- `/teacher/diagnosis-cases/{caseId}`

页面结构：
- CurrentStudentContext
- actual KPI
- 当前学习问题
- 当前教学流程
- recentAttempts / diagnosis / intervention 组成的真实业务事实
- diagnosis summary
- 主 CTA → `/teacher/analytics`

不要：
- 班级高风险排行榜
- 班级人数/KPI 首屏
- 本地 cohort BI
- 伪时间线

若没有 current student：
EMPTY。

## Profile

完成二级 `/teacher/students/:studentId`：
- 单学生业务事实
- recentAttempts
- diagnosis link
- intervention summary

## Diagnosis

完成二级 `/teacher/diagnosis-cases/:caseId`：
- primaryHypothesis
- evidence
- counterEvidence
- severity
- confidence

可用 Drawer/详情页，视觉贴旧 TeacherStudentProfile。

## Tests

- workbench priority resolves current student
- no class dashboard metrics
- profile route
- diagnosis evidence
- EMPTY states
- 403/404 states

Acceptance:
test/typecheck/build PASS。
1440/1672 visual PASS。

Commit:
`feat(frontend): build focused teacher workbench`
