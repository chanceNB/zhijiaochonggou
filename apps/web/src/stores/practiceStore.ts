import { defineStore } from 'pinia'
import { toApiError } from '@/api/client'
import {
  completePracticeSet,
  getPracticeSet,
  submitPracticeAttempt,
} from '@/api/student/practice'
import { addAttemptToWrongBook } from '@/api/student/wrongBook'
import {
  toAttemptFeedbackVm,
  toPracticeOutcomeVm,
  toPracticeSetVm,
  type AttemptFeedbackVm,
  type PracticeOutcomeVm,
  type PracticeSetVm,
} from '@/adapters/student/practice'
import type { StudentUiState, WrongBookItemDto } from '@/types/contracts/student'

export const usePracticeStore = defineStore('practice', {
  state: () => ({
    state: 'INITIAL' as StudentUiState,
    data: null as PracticeSetVm | null,
    error: null as string | null,
    errorCode: null as string | null,
    activeIndex: 0,
    feedbackByQuestion: {} as Record<string, AttemptFeedbackVm>,
    submittingQuestionId: null as string | null,
    completing: false,
    outcome: null as PracticeOutcomeVm | null,
    actionFeedback: null as string | null,
    addingWrongBook: false,
    addedWrongBookItem: null as WrongBookItemDto | null,
  }),
  getters: {
    currentQuestion: (store) => store.data?.questions[store.activeIndex] ?? null,
    answeredCount: (store) => Object.keys(store.feedbackByQuestion).length,
    isComplete: (store) => Boolean(store.data && store.data.questions.length > 0 && Object.keys(store.feedbackByQuestion).length >= store.data.questions.length),
  },
  actions: {
    clear() {
      this.state = 'INITIAL'
      this.data = null
      this.error = null
      this.errorCode = null
      this.activeIndex = 0
      this.feedbackByQuestion = {}
      this.submittingQuestionId = null
      this.completing = false
      this.outcome = null
      this.actionFeedback = null
      this.addingWrongBook = false
      this.addedWrongBookItem = null
    },
    async load(practiceSetId: string, force = false) {
      if (!force && this.data?.practiceSetId === practiceSetId && this.state === 'READY') return this.data
      if (this.data?.practiceSetId !== practiceSetId) {
        this.feedbackByQuestion = {}
        this.activeIndex = 0
        this.outcome = null
        this.addedWrongBookItem = null
      }
      this.state = 'LOADING'
      this.error = null
      this.errorCode = null
      try {
        const vm = toPracticeSetVm(await getPracticeSet(practiceSetId))
        this.data = vm
        this.activeIndex = Math.min(this.activeIndex, Math.max(0, vm.questions.length - 1))
        this.state = vm.questions.length ? 'READY' : 'EMPTY'
        for (const attempt of vm.attempts) {
          if (!this.feedbackByQuestion[attempt.questionId]) {
            this.feedbackByQuestion[attempt.questionId] = {
              attemptId: attempt.attemptId,
              correct: attempt.correct,
              correctAnswer: '',
              explanation: '',
              misconceptionCode: null,
              canAddWrongBook: !attempt.correct,
              canGenerateSimilar: !attempt.correct && Boolean(vm.coachSessionId),
              selectedAnswer: attempt.selectedAnswer,
            }
          }
        }
        return vm
      } catch (error) {
        const apiError = toApiError(error)
        this.state = apiError.code === 'FORBIDDEN' ? 'FORBIDDEN' : 'ERROR'
        this.errorCode = apiError.code
        this.error = '当前练习暂时无法加载，请稍后重试'
        return null
      }
    },
    setActiveIndex(index: number) {
      if (!this.data) return
      this.activeIndex = Math.max(0, Math.min(index, this.data.questions.length - 1))
    },
    async submitCurrent(answer: string, durationSeconds: number) {
      const question = this.currentQuestion
      const practiceSetId = this.data?.practiceSetId
      if (!question || !practiceSetId || this.feedbackByQuestion[question.questionId]) return null
      this.submittingQuestionId = question.questionId
      this.actionFeedback = null
      try {
        const dto = await submitPracticeAttempt({ practiceSetId, questionId: question.questionId, answer, durationSeconds })
        const feedback = toAttemptFeedbackVm(dto, answer)
        this.feedbackByQuestion[question.questionId] = feedback
        this.state = 'READY'
        return feedback
      } catch (error) {
        const apiError = toApiError(error)
        this.errorCode = apiError.code
        this.error = apiError.code === 'DOMAIN_RULE_VIOLATION' ? '这道题的提交状态已变化，请刷新后继续' : '提交答案失败，请重试'
        if (apiError.code === 'DOMAIN_RULE_VIOLATION') await this.load(practiceSetId, true)
        return null
      } finally {
        this.submittingQuestionId = null
      }
    },
    async complete() {
      const practiceSetId = this.data?.practiceSetId
      if (!practiceSetId || !this.isComplete || this.completing) return null
      this.completing = true
      try {
        this.outcome = toPracticeOutcomeVm(await completePracticeSet(practiceSetId))
        return this.outcome
      } catch (error) {
        const apiError = toApiError(error)
        this.errorCode = apiError.code
        this.error = '练习结果暂时无法生成，请重试'
        return null
      } finally {
        this.completing = false
      }
    },
    async addWrongBook(attemptId: string) {
      if (this.addingWrongBook) return this.addedWrongBookItem
      if (this.addedWrongBookItem?.sourceAttemptId === attemptId) return this.addedWrongBookItem
      this.addingWrongBook = true
      this.actionFeedback = null
      try {
        const item = await addAttemptToWrongBook(attemptId)
        this.addedWrongBookItem = item
        this.actionFeedback = '已加入错题本'
        return item
      } catch (error) {
        this.actionFeedback = toApiError(error).message || '加入错题本失败，请重试'
        return null
      } finally {
        this.addingWrongBook = false
      }
    },
  },
})
