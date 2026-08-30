# CODEX MASTER PROMPT — FRONTEND REBUILD

你正在 `chanceNB/zhijiaochonggou` 中实现 Golden Demo V2 的学生端和教师端前端。

先做预检，不修改文件：

1. 输出当前 worktree / branch / HEAD。
2. 运行：
   `git merge-base --is-ancestor 6079ad9991f558ad0aa34953143da2750260252a HEAD`
3. 输出 `git status --short`。
4. 阅读：
   - `zhijiao_huiping_golden_demo_v2_codex_pack/00_GOLDEN_DEMO_V2_FREEZE.md`
   - `04_API_INTERFACE_SPEC.md`
   - `05_FRONTEND_CONTRACT.md`
   - `06_COMPONENT_LIBRARY.md`
   - `08_ERROR_AND_STATE_STANDARD.md`
   - 本提示包 `01_AUTHORITY_AND_FREEZE.md`
   - `02_CONTRACT_AUDIT_AND_FIXES.md`
   - `03_UI_DESIGN_FREEZE_STUDENT_TEACHER.md`
   - `04_ROUTE_API_VIEWMODEL_MATRIX.md`
   - `05_LEGACY_VISUAL_MIGRATION_RULES.md`
   - `06_SMARTBI_EMBED_SPEC.md`
   - `07_TEST_AND_ACCEPTANCE.md`
5. 浏览 `legacy_visual_reference/`，只把它当视觉真源。

硬规则：

- FRONTEND DESIGN GATE 已由用户批准，不重新设计。
- 旧代码只复用视觉，不复用 Store/API/Mock/业务。
- 所有业务数据使用真实 API → Zod → Adapter → ViewModel。
- 不硬编码小明的作答结果。
- 诊断题固定 2 道，但结果可以 0/2、1/2、2/2。
- 不硬编码 mastery/confidence/forgettingRisk/predictedLift/actualLift。
- 不在 Vue 重画 SmartBI。
- SmartBI verified IFRAME first；NEW_TAB/UNVERIFIED fallback。
- AIChat iframe 不抓 DOM；Recommendation 继续 MANUAL_CAPTURE。
- Idempotency-Key / If-Match / 409 / 412 必须遵守 API 规范。
- 不调用 `/admin/knowledge/*` 给 student/teacher 绕权限。
- 不更改已通过的后端业务算法来迁就 UI。
- 不升级 Vue Router 到 5，不升级 Zod 到 4。
- 不把旧页面业务文案、旧班级统计带回新系统。

后续我会逐个给你 F00-F06。
每个 Fxx：
- 只做一次完整预检
- 预检通过后连续做到验收完成
- 普通 test/build/typecheck 问题自行修复
- 每个阶段提交清晰 commit
- 不自行 merge main
- 最终给出：修改文件、契约映射、测试、运行截图/URL、已知 blocker

现在只完成预检和阅读摘要，不开始 F00，等我发 F00。
