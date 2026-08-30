# Frontend Contract Audit & Required Fixes

以下是当前契约与已冻结设计之间需要收口的问题。

## FIX-01：诊断结果必须 outcome-driven

`questionCount=2` 是 API 契约。
“1 对 1 错”不是契约，只能是一次演示脚本行为。

要求：
- UI 从实际 attempt.correct / complete.accuracy 展示结果。
- 测试至少覆盖 0/2、1/2、2/2 三种合法结果。
- 不在 fixture 名称、ViewModel、组件条件里写死 `oneCorrectOneWrong`。

## FIX-02：补 `/student/practice` Practice Hub

当前契约有 `/student/practice/:practiceSetId`，但左侧存在“定向练习”一级入口。

增加前端路由：
- `/student/practice` → `StudentPracticeHubPage`

不新增后端“练习列表”接口。
Hub 只组合真实来源：
- `/student/today` 的 nextAction / teacherAssignment
- 当前 coach 产生的 practiceSet
- `/student/wrong-book`
- 前端当前会话保存的最近 PracticeSet 引用

禁止伪造历史练习列表。

## FIX-03：Teacher 一级导航改为演示主链

一级导航冻结：
- 工作台
- 数据洞察
- 干预决策
- 干预结果
- 课程资源

保留为二级路由：
- `/teacher/students/:studentId`
- `/teacher/diagnosis-cases/:caseId`

干预决策和干预结果允许复用同一个 `TeacherInterventionPage`：
- 决策 section
- outcome section

若当前没有 recommendation/intervention 上下文，显示 EMPTY，而不是制造 demo 数据。

## FIX-04：教师工作台不以班级为主视角

工作台聚焦当前 demo case 的当前学生。
不能展示“班级高风险人数排行榜”等作为首屏主内容。

班级/cohort：
- 允许在 SmartBI Dashboard 中作为参照
- 不允许本地 Vue 再造班级 BI

## FIX-05：SmartBI 改为 verified-IFRAME-first

目标：
- Dashboard 验证成功 → `IFRAME`
- AIChat 验证成功 → `IFRAME`
- iframe/认证不可用 → `NEW_TAB`
- 未验证 → `UNVERIFIED`

Dashboard 目标 URL 形式：
`https://tiaozhanbei.cloud.smartbi.com.cn/smartbi/vision/openresource.jsp?resid={RESID}`

AIChat 目标入口：
`https://tiaozhanbei.cloud.smartbi.com.cn/smartbi/vision/aichat/proxy/#/`

但前端组件不得硬编码这两个地址。
前端只消费后端 `SmartBI asset resourceUrl + launchMode`。
resid/真实 URL 必须来自已验证平台配置。

## FIX-06：AIChat iframe 仍然不能自动抓取 Recommendation

跨域 iframe 默认受 Same-Origin Policy 约束。

因此：
- 不读 `iframe.contentDocument`
- 不抓 SmartBI DOM
- 不声称 SmartBI 自动回调
- 继续 `MANUAL_CAPTURE`

UX：
AIChat iframe 外层提供“记录分析建议”按钮。
点击打开本地 Drawer：
- 手工输入/粘贴结构化 JSON
- Zod 校验
- 教师确认
- POST `/teacher/analysis-recommendations`

## FIX-07：AI Coach session restore 契约不完整

现有前端契约允许 `/student/ai-coach/:sessionId`，但 API 文档未提供会话列表/完整历史 GET。

主链实现规则：
- 当前浏览器会话内可由 coachStore 恢复当前 session。
- fresh reload 若无法从服务端恢复历史，不允许显示伪历史。
- 显示 DEGRADED/EMPTY：“当前版本未提供历史会话恢复接口，可开始新会话。”
- 这是非 Golden Demo blocker。

## FIX-08：学习资料 / 课程资源存在权限契约缺口

当前知识库 CRUD 是 `/admin/knowledge/*`。

因此：
- StudentResourcesPage 不得直接调用 admin API。
- TeacherCourseResourcesPage 不得因为 UI 需要就绕过 admin namespace。
- 主 Golden Demo 先使用已存在/已索引知识库完成 RAG。
- 资料上传能力进入 `optional/RESOURCE_UPLOAD_CONTRACT_GAP.md`，单独扩展后端契约后再启用。

## FIX-09：所有命令接口保留幂等/并发语义

- 创建/命令 POST：`Idempotency-Key`
- approve / commit 等状态迁移：`If-Match: <version>`
- 409/412：重新拉取最新 server state，不覆盖
- requestId 仅进 Trace/诊断信息

## FIX-10：SmartBI unavailable 不得阻塞业务事实

SmartBI 503：
- 数据洞察显示 unavailable/degraded
- 不回滚学生 Attempt
- 不回滚 Intervention
- 不生成 Vue 假图
