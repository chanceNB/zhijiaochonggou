# F00 — Contract Alignment + Visual Foundation

目标：
先修正前端契约和 Design System，再建立 Student/Teacher Shell。不要实现业务深页。

## 1. Contract alignment

将当前：
- `05_FRONTEND_CONTRACT.md`
- `06_COMPONENT_LIBRARY.md`

与：
- `proposed_contracts/05_FRONTEND_CONTRACT_V3_PROPOSED.md`
- `proposed_contracts/06_COMPONENT_LIBRARY_V2_PROPOSED.md`

做合并式更新。

要求：
- 保留当前仓库比 proposed 更“新”的真实事实。
- 补 `/student/practice`。
- Teacher 一级导航改为：工作台、数据洞察、干预决策、干预结果、课程资源。
- profile/diagnosis 保留二级路由。
- 写明 diagnostic result runtime-driven。
- 写明 SmartBI verified IFRAME-first + fallback + MANUAL_CAPTURE。
- 写明 knowledge upload 权限 gap。

新增：
`docs/frontend-design-freeze-v3.md`
内容以本包 `03_UI_DESIGN_FREEZE_STUDENT_TEACHER.md` 为准。

## 2. Dependencies

在保持当前 Vue/Vue Router/Pinia/Axios/Zod 版本的前提下，仅加入 F00 必要依赖：

production:
- element-plus
- @element-plus/icons-vue

dev:
- sass
- @vue/test-utils

不要升级 Router/Zod/Vite 大版本。

## 3. Design tokens

把新项目 tokens 对齐旧视觉：

page/card/primary/student-primary/ai/warning/risk/text/secondary/border/radius/shadow。

优先使用 CSS variables；SCSS 仅用于组件样式组织。

## 4. App shell

创建/重构：

- StudentLayout
- StudentSidebar
- TeacherLayout
- TeacherSidebar
- TeacherTopHeader

Student：
186px / 76px
active #EEF2FF
6个 nav label 按冻结

Teacher：
230px / 78px
Header 56px
content max 1440px
5个一级 nav

当前已有 `AppShell.vue` 可以拆/复用，但不要为了保留空壳而扭曲已冻结布局。

## 5. Routes

至少注册全部冻结 route 占位页，避免死链。

新增 `/student/practice`。

Teacher profile/diagnosis 二级。

Teacher intervention base route + `/:interventionId`。

## 6. Visual source

严格参考：
`legacy_visual_reference/src/layouts/*`
`legacy_visual_reference/src/components/*Sidebar*`
`TeacherTopHeader.visual.vue`
tokens/style。

不要复制旧 script。

## 7. Tests

先写 RED：
- student routes resolve
- teacher routes resolve
- StudentSidebar 6项
- TeacherSidebar 5项
- active nav
- collapse
- no horizontal overflow class/layout assumptions

再实现 GREEN。

## Acceptance

- `npm run test:run` PASS
- `npm run typecheck` PASS
- `npm run build` PASS
- 1440×900 / 1672×941 shell 无 horizontal overflow
- 视觉明显属于旧项目同一套系统
- 没有业务 Mock 填满页面

完成后提交：
`feat(frontend): establish frozen visual foundation`

然后停止，给 F00 报告。
