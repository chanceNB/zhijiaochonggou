import { createRouter, createWebHistory, type RouteRecordRaw } from 'vue-router'
import StudentLayout from '@/components/StudentLayout.vue'
import TeacherLayout from '@/components/TeacherLayout.vue'
import FoundationView from '@/views/FoundationView.vue'
import RoutePlaceholderView from '@/views/RoutePlaceholderView.vue'

const studentPage = (title: string, subtitle = '学生学习空间') => ({
  component: RoutePlaceholderView,
  meta: { role: 'student', title, subtitle },
})

const teacherPage = (title: string, subtitle = '教师工作台') => ({
  component: RoutePlaceholderView,
  meta: { role: 'teacher', title, subtitle },
})

const routes: RouteRecordRaw[] = [
  {
    path: '/',
    name: 'foundation',
    component: FoundationView,
    meta: { title: '智教慧评' },
  },
  {
    path: '/student',
    component: StudentLayout,
    meta: { role: 'student' },
    children: [
      { path: '', redirect: '/student/today' },
      { path: 'today', name: 'student-today', ...studentPage('今日学习', '查看当前学习状态与下一步行动') },
      { path: 'ai-coach', name: 'student-ai-coach', ...studentPage('AI学习教练', '与你一起梳理学习问题') },
      { path: 'ai-coach/:sessionId', name: 'student-ai-coach-session', ...studentPage('AI学习教练', '恢复当前学习会话') },
      { path: 'practice', name: 'student-practice-hub', ...studentPage('定向练习', '从当前学习上下文进入练习') },
      { path: 'practice/:practiceSetId/result', name: 'student-practice-result', ...studentPage('练习结果', '查看本次练习的真实结果') },
      { path: 'practice/:practiceSetId', name: 'student-practice-runner', ...studentPage('定向练习', '专注完成当前练习') },
      { path: 'wrong-book', name: 'student-wrong-book', ...studentPage('错题本', '复习需要再次理解的题目') },
      { path: 'growth', name: 'student-growth', ...studentPage('我的成长', '回顾已经掌握的知识') },
      { path: 'resources', name: 'student-resources', ...studentPage('学习资料', '浏览当前可用的课程资料') },
    ],
  },
  {
    path: '/teacher',
    component: TeacherLayout,
    meta: { role: 'teacher' },
    children: [
      { path: '', redirect: '/teacher/workbench' },
      { path: 'workbench', name: 'teacher-workbench', ...teacherPage('工作台', '当前教学对象与流程状态') },
      { path: 'analytics', name: 'teacher-analytics', ...teacherPage('数据洞察', '进入 SmartBI 分析工作区') },
      { path: 'analytics/:assetKey', name: 'teacher-analytics-asset', ...teacherPage('数据洞察', '查看 SmartBI 分析资产') },
      { path: 'interventions', name: 'teacher-interventions', ...teacherPage('干预决策', '基于分析证据选择教学方案') },
      { path: 'interventions/:interventionId', name: 'teacher-intervention', ...teacherPage('干预结果', '查看干预生命周期与结果') },
      { path: 'students/:studentId', name: 'teacher-student-profile', ...teacherPage('学生画像', '查看学生学习证据') },
      { path: 'diagnosis-cases/:caseId', name: 'teacher-diagnosis-case', ...teacherPage('诊断案例', '查看诊断过程与证据') },
      { path: 'resources', name: 'teacher-resources', ...teacherPage('课程资源', '查看课程资料与索引状态') },
    ],
  },
  { path: '/admin/knowledge', name: 'admin-knowledge', component: RoutePlaceholderView, meta: { role: 'admin', title: '知识库管理', subtitle: '管理员资源空间' } },
  { path: '/admin/platform', name: 'admin-platform', component: RoutePlaceholderView, meta: { role: 'admin', title: '平台设置', subtitle: '管理员平台空间' } },
]

export const router = createRouter({
  history: createWebHistory(import.meta.env.BASE_URL),
  routes,
})
