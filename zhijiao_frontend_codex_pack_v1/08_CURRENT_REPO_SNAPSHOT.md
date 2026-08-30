# Current Repo Snapshot Used For This Pack

Repository:
`chanceNB/zhijiaochonggou`

Observed main HEAD:
`6079ad9991f558ad0aa34953143da2750260252a`
`fix(t09): harden analytics exchange validation`

Current frontend state at pack creation:
- `apps/web/src/router/index.ts` 只有 `/` → FoundationView
- `apps/web/src/components` 只有基础 AppShell / BaseButton 等少量空壳
- dependencies：
  - Vue 3.5
  - Vue Router 4.5
  - Pinia 3
  - Axios
  - Zod 3
- 尚未正式实现冻结的学生/教师页面

因此本包采用“从视觉 Foundation 正式开始”，不是在现有完整 UI 上打补丁。

若 Codex 执行时 HEAD 已前进：
- 先重新审计 `apps/web`
- 保留已完成且符合冻结设计的代码
- 不机械覆盖
