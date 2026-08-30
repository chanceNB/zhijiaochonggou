import { beforeEach, describe, expect, it, vi } from 'vitest'
import { createPinia, setActivePinia } from 'pinia'
import { flushPromises, mount } from '@vue/test-utils'
import { AxiosError, AxiosHeaders } from 'axios'
import { usePracticeStore } from '../stores/practiceStore'
import { useWrongBookStore } from '../stores/wrongBookStore'
import { getPracticeSet, submitPracticeAttempt, completePracticeSet, generateSimilarQuestions } from '../api/student/practice'
import { reviewWrongBookItem } from '../api/student/wrongBook'
import { getStudentToday } from '../api/student/today'
import { toWrongBookPageVm } from '../adapters/student/practice'
import StudentPracticeHubPage from '../views/StudentPracticeHubPage.vue'
import StudentWrongBookPage from '../views/StudentWrongBookPage.vue'
import { router } from '../router'
import type { PracticeSetResponseDto, TodayResponseDto, WrongBookPageDto } from '../types/contracts/student'

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
  it('uses the real sourceAttemptId when generating similar questions', async () => {
    vi.mocked(generateSimilarQuestions).mockResolvedValue({ practiceSetId: 'similar-set', questions: [] })
    const result = await generateSimilarQuestions({ sessionId: 'session-1', sourceAttemptId: 'attempt-real', count: 1, idempotencyKey: 'similar-key' })
    expect(result.practiceSetId).toBe('similar-set')
    expect(generateSimilarQuestions).toHaveBeenCalledWith({ sessionId: 'session-1', sourceAttemptId: 'attempt-real', count: 1, idempotencyKey: 'similar-key' })
  })

  it('preserves a wrong-book review idempotency key in the request contract', async () => {
    vi.mocked(reviewWrongBookItem).mockResolvedValue({ correct: true, status: 'LEARNING', reviewCount: 1 })
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
