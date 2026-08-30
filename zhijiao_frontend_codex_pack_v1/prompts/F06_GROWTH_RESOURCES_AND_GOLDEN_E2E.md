# F06 — Growth + Resource Contract States + Golden Demo E2E

目标：
收学生 Growth，处理 resources 契约缺口，完成整条浏览器验收。

## Student Growth

GET `/student/growth`

展示：
- mastery
- trend
- completedTasks
- repairedMisconceptions
- latestIntervention

学生语气：
成长/完成/迁移结果。

不要把 predictionDeviation 作为学生主 KPI。

若需要本地 trend 图，允许此阶段增加 ECharts；
SmartBI 图仍禁止 Vue 重画。

## Student Resources

完成视觉页面，但严格按当前契约：

如果没有 student-scoped resource API：
- ResourceContractState
- 可以展示说明和当前 RAG 引用上下文
- upload 按钮不可调用 Admin API
- 明确“当前版本未开放学生资料上传契约”

不要为了“看起来完成”写 fake list。

## Teacher Course Resources

同样：
- 如果 teacher 没有正式 knowledge CRUD 权限契约，不绕 `/admin/knowledge/*`
- 展示受限/待开放状态
- 主 Demo 不依赖该页面

如用户之后批准 backend extension，再执行 optional prompt。

## Golden Demo E2E

使用真实 API。

顺序：

Student Today
→ AI Coach
→ 2题 diagnostic
→ actual attempts（结果任意合法）
→ discuss/similar/wrongbook 至少验证可用
→ complete
→ Teacher Workbench
→ SmartBI risk
→ AIChat
→ Manual Capture 3 candidates
→ Intervention create
→ approve
→ commit
→ Student Today Assignment
→ Practice transfer
→ complete
→ Teacher Outcome
→ SmartBI intervention outcome
→ Student Growth

重要：
演示 rehearsal 可以人为选择 1 对 1 错，
但代码/自动化 assertion 不允许要求 1 对 1 错。

## Final verification

- npm test
- typecheck
- build
- 1440×900 screenshots
- 1672×941 screenshots
- no horizontal overflow
- grep/search 确认无 hard-coded demo result
- grep/search 确认没有 frontend mastery/predictedLift/actualLift calculation
- grep/search 确认无 `/admin/knowledge` from student/teacher pages
- grep/search 确认无 SmartBI iframe DOM scraping

Commit:
`feat(frontend): complete golden demo web flow`

最终输出完整 FRONTEND GOLDEN DEMO ACCEPTANCE REPORT。
