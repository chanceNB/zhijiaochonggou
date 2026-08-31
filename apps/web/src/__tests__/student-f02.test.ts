import { beforeEach, describe, expect, it, vi } from 'vitest'
import { createPinia, setActivePinia } from 'pinia'
import { flushPromises, mount } from '@vue/test-utils'
import { AxiosError, AxiosHeaders } from 'axios'
import { usePracticeStore } from '../stores/practiceStore'
import { useCoachStore } from '../stores/coachStore'
import { useWrongBookStore } from '../stores/wrongBookStore'
import { getPracticeSet, submitPracticeAttempt, completePracticeSet, generateSimilarQuestions } from '../api/student/practice'
import { addAttemptToWrongBook, reviewWrongBookItem } from '../api/student/wrongBook'
import { generateSimilarQuestions as generateSimilarCoachQuestions } from '../api/student/coach'
import { getStudentToday } from '../api/student/today'
import { toWrongBookPageVm } from '../adapters/student/practice'
import StudentPracticeHubPage from '../views/StudentPracticeHubPage.vue'
import StudentWrongBookPage from '../views/StudentWrongBookPage.vue'
import StudentPracticeRunnerPage from '../views/StudentPracticeRunnerPage.vue'
import { router } from '../router'
import type { PracticeSetResponseDto, SimilarSetResponseDto, TodayResponseDto, WrongBookPageDto } from '../types/contracts/student'

vi.mock('../api/student/practice', () => ({
  getPracticeSet: vi.fn(),
  submitPracticeAttempt: vi.fn(),
  completePracticeSet: vi.fn(),
  generateSimilarQuestions: vi.fn(),
}))
vi.mock('../api/student/wrongBook', () => ({
  getWrongBook: vi.fn(),
  reviewWrongBookItem: vi.fn(),
  addAttemptToWrongBook: vi.fn(),
}))
vi.mock('../api/student/today', () => ({ getStudentToday: vi.fn() }))
vi.mock('../api/student/coach', () => ({
  createCoachSession: vi.fn(),
  createDiagnosticSet: vi.fn(),
  getCoachSession: vi.fn(),
  sendCoachMessage: vi.fn(),
  generateSimilarQuestions: vi.fn(),
}))

const setDto = (attempts: PracticeSetResponseDto['attempts'] = []): PracticeSetResponseDto => ({
  practiceSetId: 'set-1', studentId: 'stu-xiaoming', courseId: 'course-data-structures', classId: 'class-1', source: 'AI_COACH_DIAGNOSTIC', status: 'OPEN', coachSessionId: 'session-1',
  questions: [
    { questionId: 'q-1', knowledgePointId: 'kp-1', questionType: 'SINGLE_CHOICE', stem: '第一题', options: [{ optionId: 'A', text: 'A' }, { optionId: 'B', text: 'B' }], difficulty: 0.4 },
    { questionId: 'q-2', knowledgePointId: 'kp-1', questionType: 'SINGLE_CHOICE', stem: '第二题', options: [{ optionId: 'A', text: 'A' }, { optionId: 'B', text: 'B' }], difficulty: 0.5 },
  ], attempts,
})

beforeEach(() => { setActivePinia(createPinia()); vi.clearAllMocks() })

const todayDisplayDto: TodayResponseDto = {
  studentId: 'stu-display-only',
  nextAction: { type: 'AI_COACH_DIAGNOSTIC', title: 'Continue learning', knowledgePointId: 'kp-private', estimatedMinutes: 10 },
  teacherAssignment: null,
  learningState: { knowledgePointId: 'kp-private', mastery: 0.2, confidence: 0.4, forgettingRisk: 0.6, evidenceCount: 1 },
  demoCaseId: null,
}

const wrongBookDisplayDto: WrongBookPageDto = {
  items: [{
    wrongItemId: 'wrong-private', studentId: 'stu-display-only', courseId: 'course-private', classId: 'class-private',
    questionId: 'q-private', sourceAttemptId: 'attempt-private', knowledgePointId: 'kp-private',
    questionType: 'SINGLE_CHOICE', options: [{ optionId: 'A', text: '相邻' }, { optionId: 'B', text: '连通' }],
    reason: 'Confused BFS and DFS for unweighted shortest path', status: 'TO_REVIEW', reviewCount: 0,
    addedAt: '2026-08-31T00:00:00Z', repairedAt: null, dataOrigin: 'LIVE_DEMO', demoRunId: null,
    demoCaseId: null, correlationId: null, sourceVersion: null,
  }], page: 1, size: 20, total: 1,
}

describe('F02 student presentation layer', () => {
  it('maps technical misconception and identifiers to student-safe labels', () => {
    const vm = toWrongBookPageVm(wrongBookDisplayDto)
    expect(vm.items[0].knowledgePointLabel).toBe('当前知识点')
    expect(vm.items[0].questionSummary).toBe('这道题的题目内容暂未提供')
    expect(vm.items[0].reasonLabel).toBe('知识点辨析还不够清晰')
    expect(vm.items[0].reasonLabel).not.toContain('BFS')
  })

  it('does not render student, course, knowledge-point, question, or attempt ids in Hub UI', async () => {
    vi.mocked(getStudentToday).mockResolvedValue(todayDisplayDto)
    const { getWrongBook } = await import('../api/student/wrongBook')
    vi.mocked(getWrongBook).mockResolvedValue(wrongBookDisplayDto)
    await router.push('/student/practice')
    const wrapper = mount(StudentPracticeHubPage, { global: { plugins: [router] } })
    await flushPromises()
    const rendered = wrapper.text()
    expect(rendered).toContain('继续学习')
    expect(rendered).not.toMatch(/stu-|course-|kp-|attempt-|wrong-|q-/i)
  })

  it('does not render raw ids or technical misconception text in WrongBook UI', async () => {
    const { getWrongBook } = await import('../api/student/wrongBook')
    vi.mocked(getWrongBook).mockResolvedValue(wrongBookDisplayDto)
    await router.push('/student/wrong-book')
    const wrapper = mount(StudentWrongBookPage, { global: { plugins: [router] } })
    await flushPromises()
    const rendered = wrapper.text()
    expect(rendered).toContain('当前知识点')
    expect(rendered).toContain('知识点辨析还不够清晰')
    expect(rendered).not.toMatch(/stu-|course-|kp-|attempt-|wrong-|q-|Confused BFS/i)
  })

  it('renders single-choice options, submits the option id, and moves a correct item to mastered', async () => {
    const initial = {
      ...wrongBookDisplayDto,
      items: [{ ...wrongBookDisplayDto.items[0], wrongItemId: 'wrong-review', status: 'TO_REVIEW', reviewCount: 0 }],
    }
    const mastered = {
      ...initial,
      items: [{ ...initial.items[0], status: 'MASTERED', reviewCount: 1, repairedAt: '2026-08-31T00:00:00Z' }],
    }
    const { getWrongBook } = await import('../api/student/wrongBook')
    vi.mocked(getWrongBook).mockResolvedValueOnce(initial).mockResolvedValueOnce(mastered)
    vi.mocked(reviewWrongBookItem).mockResolvedValue({ correct: true, status: 'MASTERED', reviewCount: 1 })
    await router.push('/student/wrong-book')
    const wrapper = mount(StudentWrongBookPage, { global: { plugins: [router] } })
    await flushPromises()
    expect(wrapper.find('.review-options').exists()).toBe(true)
    expect(wrapper.text()).toContain('相邻')
    expect(wrapper.text()).not.toContain('correctAnswer')
    await wrapper.findAll('.review-option')[0].trigger('click')
    expect(wrapper.find('.review-option.selected').text()).toContain('相邻')
    await wrapper.find('form').trigger('submit')
    await flushPromises()
    expect(reviewWrongBookItem).toHaveBeenCalledWith(expect.objectContaining({ answer: 'A' }))
    expect(wrapper.text()).toContain('回答正确')
    expect(wrapper.text()).toContain('本题已掌握')
    expect(wrapper.findAll('[role="tab"]')[2].attributes('aria-selected')).toBe('true')
    wrapper.unmount()
  })

  it('uses backend incorrect feedback without client-side grading', async () => {
    const { getWrongBook } = await import('../api/student/wrongBook')
    vi.mocked(getWrongBook).mockResolvedValue(wrongBookDisplayDto)
    vi.mocked(reviewWrongBookItem).mockResolvedValue({ correct: false, status: 'TO_REVIEW', reviewCount: 1 })
    await router.push('/student/wrong-book')
    const wrapper = mount(StudentWrongBookPage, { global: { plugins: [router] } })
    await flushPromises()
    await wrapper.findAll('.review-option')[1].trigger('click')
    await wrapper.find('form').trigger('submit')
    await flushPromises()
    expect(reviewWrongBookItem).toHaveBeenCalledWith(expect.objectContaining({ answer: 'B' }))
    expect(wrapper.text()).toContain('回答错误')
    expect(wrapper.text()).toContain('仍需继续复习')
    expect(wrapper.findAll('[role="tab"]')[0].attributes('aria-selected')).toBe('true')
    wrapper.unmount()
  })

  it('filters the WrongBook by the real mastered status', async () => {
    const { getWrongBook } = await import('../api/student/wrongBook')
    vi.mocked(getWrongBook).mockResolvedValue(wrongBookDisplayDto)
    await router.push('/student/wrong-book')
    const wrapper = mount(StudentWrongBookPage, { global: { plugins: [router] } })
    await flushPromises()
    const tabs = wrapper.findAll('[role="tab"]')
    await tabs[2].trigger('click')
    expect(tabs[2].attributes('aria-selected')).toBe('true')
    expect(wrapper.find('.list-empty').text()).toContain('没有匹配的错题')
    expect(wrapper.find('.wrong-detail--empty').exists()).toBe(true)
  })
})

describe('F02 practice result contract', () => {
  it.each([
    [0, 2, 0], [1, 2, 50], [2, 2, 100],
  ])('keeps backend accuracy for %i/%i answers', async (correctCount, _questionCount, accuracyPercent) => {
    vi.mocked(getPracticeSet).mockResolvedValue(setDto())
    vi.mocked(submitPracticeAttempt)
      .mockResolvedValueOnce({ attemptId: 'attempt-1', correct: correctCount >= 1, correctAnswer: 'A', explanation: '解释一', misconceptionCode: correctCount >= 1 ? null : 'ANSWER_MISMATCH', canAddWrongBook: correctCount < 1, canGenerateSimilar: correctCount < 1 })
      .mockResolvedValueOnce({ attemptId: 'attempt-2', correct: correctCount >= 2, correctAnswer: 'A', explanation: '解释二', misconceptionCode: correctCount >= 2 ? null : 'ANSWER_MISMATCH', canAddWrongBook: correctCount < 2, canGenerateSimilar: correctCount < 2 })
    vi.mocked(completePracticeSet).mockResolvedValue({ outcomeId: `outcome-${correctCount}`, practiceSetId: 'set-1', accuracy: accuracyPercent / 100, attemptCount: 2, learningStateStatus: 'UPDATED', transferValidation: null, learningStateAfter: null, interventionOutcomeId: null })
    const store = usePracticeStore()
    await store.load('set-1')
    await store.submitCurrent('A', 10)
    store.setActiveIndex(1)
    await store.submitCurrent('A', 10)
    const result = await store.complete()
    expect(result?.accuracyPercent).toBe(accuracyPercent)
  })
})

describe('F02 wrong book and similar question contracts', () => {
  it('keeps readable discussion context for the real coach message', () => {
    const store = useCoachStore()
    store.setPracticeContext({
      practiceSetId: 'set-1', questionId: 'q-1', attemptId: 'attempt-private',
      coachSessionId: 'session-1',
      questionStem: '题目内容', knowledgePointName: '当前知识点', selectedAnswer: 'B',
      correctAnswer: 'A', explanation: '系统解释', misconceptionLabel: '答题思路需要复盘',
    })
    expect(store.practiceContext?.questionStem).toBe('题目内容')
    expect(store.practiceContext?.attemptId).toBe('attempt-private')
    expect(store.practiceContext?.kind).toBe('PRACTICE')
    expect(store.practiceContext?.coachSessionId).toBe('session-1')
  })

  it('uses the real sourceAttemptId when generating similar questions', async () => {
    vi.mocked(generateSimilarQuestions).mockResolvedValue({ practiceSetId: 'similar-set', questions: [] })
    const result = await generateSimilarQuestions({ sessionId: 'session-1', sourceAttemptId: 'attempt-real', count: 1, idempotencyKey: 'similar-key' })
    expect(result.practiceSetId).toBe('similar-set')
    expect(generateSimilarQuestions).toHaveBeenCalledWith({ sessionId: 'session-1', sourceAttemptId: 'attempt-real', count: 1, idempotencyKey: 'similar-key' })
  })

  it('returns the real wrong-book item and focuses it after a successful add', async () => {
    const set = setDto([{ attemptId: 'attempt-real', questionId: 'q-1', selectedAnswer: 'B', correct: false, responseTimeMs: 1000, attemptTime: '2026-08-31T00:00:00Z' }])
    vi.mocked(getPracticeSet).mockResolvedValue(set)
    vi.mocked(addAttemptToWrongBook).mockResolvedValue({
      wrongItemId: 'wrong-real', studentId: 'stu-xiaoming', courseId: 'course-data-structures', classId: 'class-1',
      questionId: 'q-1', sourceAttemptId: 'attempt-real', knowledgePointId: 'kp-1', knowledgePointName: '排序',
      questionSummary: '原题题干', reasonDisplayName: '答题思路需要复盘', reason: null, status: 'TO_REVIEW', reviewCount: 0,
      addedAt: '2026-08-31T00:00:00Z', repairedAt: null, dataOrigin: 'LIVE_DEMO', demoRunId: null, demoCaseId: null,
      correlationId: null, sourceVersion: null,
    })
    const practice = usePracticeStore()
    await practice.load('set-1')
    const result = await practice.addWrongBook('attempt-real')
    const wrongBook = useWrongBookStore()
    wrongBook.focusItem(result!.wrongItemId)
    expect(result?.sourceAttemptId).toBe('attempt-real')
    expect(practice.actionFeedback).toBe('已加入错题本')
    expect(wrongBook.selectedWrongItemId).toBe('wrong-real')
    expect(addAttemptToWrongBook).toHaveBeenCalledWith('attempt-real')
  })

  it('shows wrong-book success feedback in the runner and exposes the real item link', async () => {
    vi.mocked(getPracticeSet).mockResolvedValue(setDto([
      { attemptId: 'attempt-real', questionId: 'q-1', selectedAnswer: 'B', correct: false, responseTimeMs: 1000, attemptTime: '2026-08-31T00:00:00Z' },
    ]))
    vi.mocked(addAttemptToWrongBook).mockResolvedValue({
      wrongItemId: 'wrong-real', studentId: 'stu-xiaoming', courseId: 'course-data-structures', classId: 'class-1',
      questionId: 'q-1', sourceAttemptId: 'attempt-real', knowledgePointId: 'kp-1', knowledgePointName: '排序',
      questionSummary: '原题题干', reasonDisplayName: '答题思路需要复盘', reason: null, status: 'TO_REVIEW', reviewCount: 0,
      addedAt: '2026-08-31T00:00:00Z', repairedAt: null, dataOrigin: 'LIVE_DEMO', demoRunId: null, demoCaseId: null,
      correlationId: null, sourceVersion: null,
    })
    const coach = useCoachStore()
    coach.session = {
      sessionId: 'session-1', studentId: 'stu-xiaoming', courseId: 'course-data-structures', knowledgePointId: 'kp-1',
      mode: 'DIAGNOSTIC', status: 'ACTIVE', ragStatus: 'EMPTY',
      context: { mastery: 0.2, confidence: 0.4, forgettingRisk: 0.6, weaknessScore: 0 }, messages: [],
    }
    await router.push('/student/practice/set-1')
    const wrapper = mount(StudentPracticeRunnerPage, { global: { plugins: [router] } })
    await flushPromises()
    await wrapper.get('button.secondary-action').trigger('click')
    await flushPromises()
    expect(wrapper.text()).toContain('✓ 已加入错题本')
    expect(wrapper.text()).toContain('查看错题本')
    expect(wrapper.findAll('button').some((button) => button.text() === '加入错题本')).toBe(false)
    expect(useWrongBookStore().selectedWrongItemId).toBe('wrong-real')
    wrapper.unmount()
  })

  it('shows similar-question loading and success feedback before navigating', async () => {
    vi.mocked(getPracticeSet)
      .mockResolvedValueOnce(setDto([
        { attemptId: 'attempt-real', questionId: 'q-1', selectedAnswer: 'B', correct: false, responseTimeMs: 1000, attemptTime: '2026-08-31T00:00:00Z' },
      ]))
      .mockResolvedValueOnce({
        ...setDto(),
        practiceSetId: 'similar-set',
        source: 'AI_COACH_SIMILAR',
        questions: [{ ...setDto().questions[0], stem: '生成的真实类似题' }],
      })
    let resolveSimilar!: (value: SimilarSetResponseDto) => void
    vi.mocked(generateSimilarCoachQuestions).mockReturnValue(new Promise((resolve) => { resolveSimilar = resolve }))
    const coach = useCoachStore()
    coach.session = {
      sessionId: 'session-1', studentId: 'stu-xiaoming', courseId: 'course-data-structures', knowledgePointId: 'kp-1',
      mode: 'DIAGNOSTIC', status: 'ACTIVE', ragStatus: 'EMPTY',
      context: { mastery: 0.2, confidence: 0.4, forgettingRisk: 0.6, weaknessScore: 0 }, messages: [],
    }
    await router.push('/student/practice/set-1')
    const wrapper = mount(StudentPracticeRunnerPage, { global: { plugins: [router] } })
    await flushPromises()
    const similarButton = wrapper.findAll('button').find((button) => button.text() === '生成类似题')
    expect(similarButton).toBeDefined()
    const pending = similarButton!.trigger('click')
    await flushPromises()
    expect(wrapper.text()).toContain('AI 正在生成...')
    resolveSimilar({ practiceSetId: 'similar-set', questions: [] })
    await pending
    await flushPromises()
    expect(router.currentRoute.value.path).toBe('/student/practice/similar-set')
    expect(wrapper.text()).toContain('生成的真实类似题')
    expect(getPracticeSet).toHaveBeenLastCalledWith('similar-set')
    wrapper.unmount()
  })

  it('preserves a wrong-book review idempotency key in the request contract', async () => {
    vi.mocked(reviewWrongBookItem).mockResolvedValue({ correct: true, status: 'MASTERED', reviewCount: 1 })
    await reviewWrongBookItem({ wrongItemId: 'wrong-1', answer: 'A', durationSeconds: 10, idempotencyKey: 'review-key' })
    expect(reviewWrongBookItem).toHaveBeenCalledWith({ wrongItemId: 'wrong-1', answer: 'A', durationSeconds: 10, idempotencyKey: 'review-key' })
  })

  it('reloads after a duplicate attempt conflict instead of inventing a result', async () => {
    vi.mocked(getPracticeSet).mockResolvedValue(setDto())
    const conflict = new AxiosError('conflict', 'ERR_BAD_REQUEST', undefined, undefined, {
      status: 409, statusText: 'Conflict', headers: new AxiosHeaders(), config: { headers: new AxiosHeaders() },
      data: { code: 'DOMAIN_RULE_VIOLATION', message: 'duplicate', requestId: 'req-1', data: null, timestamp: '2026-08-31T00:00:00Z' },
    })
    vi.mocked(submitPracticeAttempt).mockRejectedValue(conflict)
    const store = usePracticeStore()
    await store.load('set-1')
    const result = await store.submitCurrent('A', 10)
    expect(result).toBeNull()
    expect(getPracticeSet).toHaveBeenCalledTimes(2)
    expect(store.data?.practiceSetId).toBe('set-1')
  })
})
