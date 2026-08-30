# Legacy Visual Migration Rules

## 为什么提供 `legacy_visual_reference`

旧项目已经跑通，视觉语言比图片描述更可靠。
但旧项目业务模型和新项目不一致，所以这里只保留视觉摘录。

每个 `.visual.vue`：
- 已移除 `<script>`
- 保留 `<template>` 与 `<style>`
- 仅用于看布局和 CSS
- 不能直接编译
- 不能把模板里的旧业务文本/数据当新事实

## 高优先级视觉参考

Student：
1. `src/layouts/StudentLayout.visual.vue`
2. `src/components/student/layout/StudentSidebar.visual.vue`
3. `src/pages/student/StudentTodayPage.visual.vue`
4. `src/pages/student/StudentAiCoachPage.visual.vue`
5. `src/pages/student/StudentPracticePage.visual.vue`
6. `src/pages/student/StudentWrongBookPage.visual.vue`
7. `src/pages/student/StudentGrowthPage.visual.vue`
8. `src/pages/student/StudentKnowledgeBasePage.visual.vue`

Teacher：
1. `src/layouts/TeacherLayout.visual.vue`
2. `src/components/teacher/TeacherSidebar.visual.vue`
3. `src/components/teacher/TeacherTopHeader.visual.vue`
4. `src/pages/teacher/TeacherDashboardPage.visual.vue`
5. `src/pages/teacher/TeacherStudentProfilePage.visual.vue`
6. `src/pages/teacher/TeacherInterventionLabPage.visual.vue`
7. `src/pages/teacher/TeacherCourseKnowledgeBasePage.visual.vue`
8. `src/pages/teacher/teacher-overhaul.scss`

## 迁移策略

A 类：可直接借结构
- Shell
- Sidebar
- Header
- 卡片布局
- list/table/timeline
- drawer/dialog
- 空态/错误态外观

B 类：只借 Template/Style，Script 全重写
- Today
- AI Coach
- Practice
- WrongBook
- Growth
- Teacher Workbench
- Intervention
- Knowledge pages

C 类：禁止迁
- old store
- old api
- old mock
- old types
- old permission map
- old data-source switch
- old SmartBI adapter
- old calculations

## 依赖策略

当前新项目保持 Vue 3.5 / Vue Router 4.5 / Pinia 3 / Axios / Zod 3。

为了复刻旧视觉，可以引入：
- `element-plus`
- `@element-plus/icons-vue`
- `sass`
- `@vue/test-utils`

AI Coach 若需要原 Markdown 样式：
- `markdown-it`
- `highlight.js`

Growth 若确实需要本地图表：
- `echarts`

禁止为了复制旧项目而升级：
- Vue Router 到 5
- Zod 到 4
- 整套 Vite/TypeScript 大版本
