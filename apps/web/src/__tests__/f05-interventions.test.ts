import { beforeEach, describe, expect, it, vi } from 'vitest'
import { flushPromises, mount } from '@vue/test-utils'
import { createPinia, setActivePinia } from 'pinia'
import { router } from '../router'
import TeacherInterventionsPage from '../views/TeacherInterventionsPage.vue'
import TeacherInterventionOutcomePage from '../views/TeacherInterventionOutcomePage.vue'
import {
  approveIntervention,
  commitIntervention,
  getAnalysisRecommendation,
  getIntervention,
  getInterventionByRecommendation,
  getInterventionOutcome,
  getTeacherWorkbench,
  proposeIntervention,
} from '../api/teacher'

vi.mock('../api/teacher', () => ({
  getTeacherWorkbench: vi.fn(),
  getTeacherProfile: vi.fn(),
  getTeacherDiagnosis: vi.fn(),
  getAnalysisRecommendation: vi.fn(),
  proposeIntervention: vi.fn(),
  approveIntervention: vi.fn(),
  commitIntervention: vi.fn(),
  getIntervention: vi.fn(),
  getInterventionByRecommendation: vi.fn(),
  getInterventionOutcome: vi.fn(),
}))

const workbench = {
  currentStudent: { studentId: 'stu-1', displayName: '测试学生', courseId: 'course-1', courseName: '数据结构', classId: 'class-1', className: '计算机1班', demoRunId: 'run-1', demoCaseId: 'case-1', correlationId: 'corr-1' },
  priorityItems: [],
  pendingRecommendations: [{ recommendationId: 'rec-1', summary: '需要验证迁移能力', status: 'PENDING_TEACHER_REVIEW', knowledgePointId: 'kp-1', knowledgePointName: '图遍历', capturedAt: '2026-08-31T00:00:00Z', demoRunId: 'run-1', demoCaseId: 'case-1', correlationId: 'corr-1' }],
  pendingOutcomes: [],
}

const recommendation = {
  recommendationId: 'rec-1', studentId: 'stu-1', courseId: 'course-1', classId: 'class-1', knowledgePointId: 'kp-1',
  analysisSummary: '诊断证据显示需要通过迁移练习验证理解。', evidenceRefs: ['答题反馈'],
  candidates: [
    { candidateIndex: 1, strategyCode: 'CONCEPT_REMEDIATION', title: '概念边界校准', rationale: '先澄清核心概念', actionDescription: '概念辨析与低难度练习', sourceSnapshot: '{}' },
    { candidateIndex: 2, strategyCode: 'VISUAL_TRANSFER_PRACTICE', title: '可视化迁移练习', rationale: '观察过程中的迁移表现', actionDescription: '过程演示与变式练习', sourceSnapshot: '{}' },
    { candidateIndex: 3, strategyCode: 'AI_GUIDED_VARIATION', title: '分层变式反馈', rationale: '用分层反馈持续巩固', actionDescription: 'AI 引导与分层变式', sourceSnapshot: '{}' },
  ],
  source: 'SMARTBI_AICHAT', captureMode: 'MANUAL', status: 'PENDING_TEACHER_REVIEW',
  generatedAt: '2026-08-31T00:00:00Z', capturedAt: '2026-08-31T00:00:00Z', sourceVersion: 'v1',
}

const proposed = {
  interventionId: 'int-1', recommendationId: 'rec-1', strategyCode: 'VISUAL_TRANSFER_PRACTICE', predictedLift: .18,
  predictionInterval: { low: .1, high: .26 }, status: 'PROPOSED', version: 1, assignmentId: null, practiceSetId: null, assignment: null,
} as const

const committed = { ...proposed, status: 'COMMITTED', version: 3, assignmentId: 'assign-1', practiceSetId: 'ps-1', assignment: {
  assignmentId: 'assign-1', interventionId: 'int-1', practiceSetId: 'ps-1', studentId: 'stu-1', courseId: 'course-1', classId: 'class-1',
  knowledgePointId: 'kp-1', status: 'PENDING_STUDENT', dueAt: null, createdAt: '2026-08-31T00:00:00Z', sourceVersion: 'v1',
} } as const

describe('F05 teacher intervention lifecycle', () => {
  beforeEach(async () => {
    setActivePinia(createPinia())
    vi.mocked(getTeacherWorkbench).mockResolvedValue(workbench)
    vi.mocked(getAnalysisRecommendation).mockResolvedValue(recommendation)
    vi.mocked(proposeIntervention).mockResolvedValue(proposed)
    vi.mocked(approveIntervention).mockResolvedValue({ ...proposed, status: 'APPROVED', version: 2 })
    vi.mocked(commitIntervention).mockResolvedValue(committed)
    await router.push('/teacher/interventions?recommendationId=rec-1')
  })

  it('renders exactly three real candidates and no placeholder', async () => {
    const wrapper = mount(TeacherInterventionsPage, { global: { plugins: [router] } })
    await flushPromises()
    expect(wrapper.find('[data-testid="teacher-intervention-page"]').exists()).toBe(true)
    expect(wrapper.findAll('[data-testid="strategy-card"]')).toHaveLength(3)
    expect(wrapper.text()).not.toContain('页面结构已就绪')
    expect(wrapper.text()).toContain('可视化迁移练习')
  })

  it('renders candidate titles from the captured recommendation, including edited titles', async () => {
    vi.mocked(getAnalysisRecommendation).mockResolvedValue({
      ...recommendation,
      candidates: recommendation.candidates.map((candidate, index) => index === 1
        ? { ...candidate, title: '教师记录的迁移方案' }
        : candidate),
    })
    await router.push('/teacher/interventions?recommendationId=rec-edited')
    const wrapper = mount(TeacherInterventionsPage, { global: { plugins: [router] } })
    await flushPromises()
    expect(wrapper.text()).toContain('教师记录的迁移方案')
    expect(wrapper.text()).not.toContain('可视化迁移练习')
  })

  it('shows a real empty state when the recommendation does not exist', async () => {
    vi.mocked(getAnalysisRecommendation).mockRejectedValue({ code: 'RESOURCE_NOT_FOUND', message: 'missing' })
    await router.push('/teacher/interventions?recommendationId=rec-missing')
    const wrapper = mount(TeacherInterventionsPage, { global: { plugins: [router] } })
    await flushPromises()
    expect(wrapper.find('[data-testid="recommendation-empty"]').exists()).toBe(true)
    expect(wrapper.findAll('[data-testid="strategy-card"]')).toHaveLength(0)
  })

  it('renders a validation error instead of filling a recommendation with default candidates', async () => {
    vi.mocked(getAnalysisRecommendation).mockResolvedValue({
      ...recommendation,
      candidates: recommendation.candidates.slice(0, 2),
    } as any)
    await router.push('/teacher/interventions?recommendationId=rec-invalid-count')
    const wrapper = mount(TeacherInterventionsPage, { global: { plugins: [router] } })
    await flushPromises()
    expect(wrapper.text()).toContain('候选方案数量不符合约定')
    expect(wrapper.findAll('[data-testid="strategy-card"]')).toHaveLength(0)
  })

  it('requires one selected candidate and a teacher rationale before propose', async () => {
    const wrapper = mount(TeacherInterventionsPage, { global: { plugins: [router] } })
    await flushPromises()
    await wrapper.get('[data-testid="propose-intervention"]').trigger('click')
    expect(wrapper.text()).toContain('请选择一个教学方案')
    await wrapper.get('[data-testid="strategy-card"] input[type="radio"]').setValue(true)
    await wrapper.get('[data-testid="teacher-rationale"]').setValue('先验证迁移过程，再根据结果调整教学节奏。')
    await wrapper.get('[data-testid="propose-intervention"]').trigger('click')
    await flushPromises()
    expect(proposeIntervention).toHaveBeenCalledWith({ recommendationId: 'rec-1', strategyCode: 'CONCEPT_REMEDIATION', teacherRationale: '先验证迁移过程，再根据结果调整教学节奏。' })
    expect(wrapper.text()).toContain('待审核')
  })

  it('uses server versions for approve and commit, then exposes the real assignment', async () => {
    const wrapper = mount(TeacherInterventionsPage, { global: { plugins: [router] } })
    await flushPromises()
    await wrapper.get('[data-testid="strategy-card"] input[type="radio"]').setValue(true)
    await wrapper.get('[data-testid="teacher-rationale"]').setValue('先验证迁移过程，再根据结果调整教学节奏。')
    await wrapper.get('[data-testid="propose-intervention"]').trigger('click')
    await flushPromises()
    await wrapper.get('[data-testid="approve-intervention"]').trigger('click')
    await flushPromises()
    expect(approveIntervention).toHaveBeenCalledWith('int-1', 1)
    await wrapper.get('[data-testid="commit-intervention"]').trigger('click')
    await flushPromises()
    expect(commitIntervention).toHaveBeenCalledWith('int-1', 2)
    expect(wrapper.text()).toContain('assign-1')
    expect(wrapper.text()).toContain('等待学生完成')
  })

  it('sends the selected candidate strategy code rather than its UI position', async () => {
    const wrapper = mount(TeacherInterventionsPage, { global: { plugins: [router] } })
    await flushPromises()
    await wrapper.findAll('[data-testid="strategy-card"]')[1].find('input[type="radio"]').setValue(true)
    await wrapper.get('[data-testid="teacher-rationale"]').setValue('先用过程演示验证迁移，再根据反馈调整练习节奏。')
    await wrapper.get('[data-testid="propose-intervention"]').trigger('click')
    await flushPromises()
    expect(proposeIntervention).toHaveBeenCalledWith({
      recommendationId: 'rec-1', strategyCode: 'VISUAL_TRANSFER_PRACTICE',
      teacherRationale: '先用过程演示验证迁移，再根据反馈调整练习节奏。',
    })
  })

  it('restores the proposed or committed state from the server after refresh', async () => {
    vi.mocked(getIntervention).mockResolvedValue(committed)
    await router.push('/teacher/interventions?recommendationId=rec-1&interventionId=int-1')
    const wrapper = mount(TeacherInterventionsPage, { global: { plugins: [router] } })
    await flushPromises()
    expect(getIntervention).toHaveBeenCalledWith('int-1')
    expect(wrapper.text()).toContain('已下发')
    expect(wrapper.text()).toContain('等待学生完成')
    expect(wrapper.find('[data-testid="teacher-rationale"]').exists()).toBe(false)
  })
})

describe('F05 intervention outcome', () => {
  it('keeps pending outcome empty and renders server outcome values when complete', async () => {
    setActivePinia(createPinia())
    vi.mocked(getIntervention).mockResolvedValue(committed)
    vi.mocked(getInterventionOutcome).mockRejectedValue({ code: 'RESOURCE_NOT_FOUND', message: 'not found' })
    await router.push('/teacher/interventions/int-1')
    const wrapper = mount(TeacherInterventionOutcomePage, { global: { plugins: [router] } })
    await flushPromises()
    expect(wrapper.text()).toContain('等待学生完成任务')

    vi.mocked(getInterventionOutcome).mockResolvedValue({
      outcomeId: 'outcome-1', interventionId: 'int-1', assignmentId: 'assign-1', practiceSetId: 'ps-1', studentId: 'stu-1', courseId: 'course-1', classId: 'class-1', knowledgePointId: 'kp-1',
      predictedLift: .18, predictionLow: .1, predictionHigh: .26, masteryBefore: .32, confidenceBefore: .7, forgettingRiskBefore: .2, weaknessScoreBefore: .68, evidenceCountBefore: 4,
      masteryAfter: .58, confidenceAfter: .82, forgettingRiskAfter: .1, evidenceCountAfter: 6, actualLift: .26, predictionDeviation: .08, transferValidation: 'PASS', practiceAccuracyAfter: 1,
      dataOrigin: 'LIVE_DEMO', demoRunId: 'run-1', demoCaseId: 'case-1', correlationId: 'corr-1', sourceVersion: 'v1', completedAt: '2026-08-31T01:00:00Z',
    })
    await (wrapper.vm as any).load(true)
    await flushPromises()
    expect(wrapper.text()).toContain('实际提升')
    expect(wrapper.text()).toContain('迁移验证通过')
    expect(wrapper.text()).not.toContain('页面结构已就绪')
  })
})
