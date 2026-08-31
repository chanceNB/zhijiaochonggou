import { beforeEach, describe, expect, it, vi } from 'vitest'
import { flushPromises, mount } from '@vue/test-utils'
import { createPinia, setActivePinia } from 'pinia'
import { router } from '../router'
import TeacherWorkbenchPage from '../views/TeacherWorkbenchPage.vue'
import TeacherStudentProfilePage from '../views/TeacherStudentProfilePage.vue'
import TeacherDiagnosisCasePage from '../views/TeacherDiagnosisCasePage.vue'
import TeacherTopHeader from '../components/TeacherTopHeader.vue'
import { getTeacherDiagnosis, getTeacherProfile, getTeacherWorkbench } from '../api/teacher'
import { teacherStatusLabel, teacherStrategyLabel, teacherTimestampLabel } from '../adapters/teacher/presentation'
import { useTeacherStore } from '../stores/teacherStore'

vi.mock('../api/teacher', () => ({
  getTeacherWorkbench: vi.fn(), getTeacherProfile: vi.fn(), getTeacherDiagnosis: vi.fn(),
}))

const workbench = {
  currentStudent: { studentId: 'stu-1', displayName: '测试学生', courseId: 'course-1', courseName: '数据结构', classId: 'class-1', className: '计算机1班', demoCaseId: 'case-1' },
  priorityItems: [{ type: 'LEARNING_ISSUE', title: '当前学习问题', description: '来自真实学习证据', status: 'AVAILABLE', knowledgePointName: '图遍历', strategy: null }],
  pendingRecommendations: [], pendingOutcomes: [],
}
const profile = {
  student: workbench.currentStudent, learningState: { knowledgePointName: '图遍历', mastery: .4, confidence: .8, forgettingRisk: .2, evidenceCount: 4, weaknessScore: .6, reasonCodes: 'LOW_MASTERY', computedAt: '2026-08-29T00:00:00Z' },
  recentAttempts: [], diagnosis: null, intervention: null,
}

describe('F03 teacher read-model pages', () => {
  beforeEach(async () => {
    setActivePinia(createPinia())
    vi.mocked(getTeacherWorkbench).mockResolvedValue(workbench)
    vi.mocked(getTeacherProfile).mockResolvedValue(profile)
    vi.mocked(getTeacherDiagnosis).mockResolvedValue({ caseId: 'case-1', severity: 'MEDIUM', confidence: .8, primaryHypothesis: '需要巩固', evidence: ['最近一次答题未答对，发生于 8月31日 12:41。'], counterEvidence: [], studentName: '测试学生', courseName: '数据结构', className: '计算机1班' })
    await router.push('/teacher/workbench')
  })

  it('renders current student facts and real queues without cohort dashboard metrics', async () => {
    const wrapper = mount(TeacherWorkbenchPage, { global: { plugins: [router] } })
    await flushPromises()
    expect(wrapper.text()).toContain('测试学生')
    expect(wrapper.text()).toContain('当前学习问题')
    expect(wrapper.text()).not.toContain('班级人数')
    expect(wrapper.text()).not.toContain('高风险排行榜')
  })

  it('keeps teacher-facing strategy and status labels readable', () => {
    expect(teacherStrategyLabel('VISUAL_TRANSFER_PRACTICE')).toBe('可视化迁移练习')
    expect(teacherStatusLabel('COMMITTED')).toBe('已提交')
  })

  it('preserves forbidden and not-found error states for the UI', () => {
    const store = useTeacherStore()
    expect(store.classify('FORBIDDEN')).toBe('FORBIDDEN')
    expect(store.classify('RESOURCE_NOT_FOUND')).toBe('NOT_FOUND')
  })

  it('renders an empty current intervention without inventing historical state', async () => {
    await router.push('/teacher/students/stu-1?courseId=course-1')
    const wrapper = mount(TeacherStudentProfilePage, { global: { plugins: [router] } })
    await flushPromises()
    expect(wrapper.text()).toContain('暂无干预记录')
  })

  it('keeps diagnosis evidence timestamps human-readable', async () => {
    expect(teacherTimestampLabel('2026-08-31T04:41:54.911141Z')).toMatch(/8(?:\/|月)31/)
    await router.push('/teacher/diagnosis-cases/case-1')
    const wrapper = mount(TeacherDiagnosisCasePage, { global: { plugins: [router] } })
    await flushPromises()
    expect(wrapper.text()).toContain('8月31日 12:41')
    expect(wrapper.text()).not.toContain('2026-08-31T04:41:54')
  })

  it('keeps teacher topbar controls display-only until real handlers exist', () => {
    const wrapper = mount(TeacherTopHeader, { global: { plugins: [router] } })
    expect(wrapper.findAll('button')).toHaveLength(0)
    expect((wrapper.get('input[type="search"]').element as HTMLInputElement).disabled).toBe(true)
  })
})
