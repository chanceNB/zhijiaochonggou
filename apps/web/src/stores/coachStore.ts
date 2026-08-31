import { defineStore } from 'pinia'
import { toApiError } from '@/api/client'
import {
  createCoachSession,
  createDiagnosticSet,
  getCoachSession,
  sendCoachMessage,
  generateSimilarQuestions,
} from '@/api/student/coach'
import { localMessageId, toAssistantMessageVm, toCoachSessionVm } from '@/adapters/student/coach'
import type {
  CoachMessageVm,
  CoachSessionVm,
  PracticeDiscussionContext,
  StudentUiState,
} from '@/types/contracts/student'

export const useCoachStore = defineStore('coach', {
  state: () => ({
    state: 'INITIAL' as StudentUiState,
    session: null as CoachSessionVm | null,
    sessions: [] as CoachSessionVm[],
    error: null as string | null,
    errorCode: null as string | null,
    sending: false,
    diagnosticState: 'INITIAL' as StudentUiState,
    diagnosticError: null as string | null,
    lastActions: [] as Array<{ type: string; label: string }>,
    lastMessage: null as CoachMessageVm | null,
    currentPracticeSetId: null as string | null,
    practiceContext: null as PracticeDiscussionContext | null,
    similarState: 'INITIAL' as StudentUiState,
    similarError: null as string | null,
  }),
  getters: {
    activeSessionId: (store) => store.session?.sessionId ?? null,
    messages: (store) => store.session?.messages ?? [],
    context: (store) => store.session?.context ?? null,
    canStartDiagnostic: (store) => Boolean(store.session?.sessionId && store.session.knowledgePointId),
    hasPracticeSet: (store) => Boolean(store.currentPracticeSetId),
  },
  actions: {
    clearError() {
      this.error = null
      this.errorCode = null
    },
    setFailure(error: unknown, fallback: string) {
      const apiError = toApiError(error)
      this.errorCode = apiError.code
      this.error = fallback
      this.state = apiError.code === 'FORBIDDEN' ? 'FORBIDDEN' : 'DEGRADED'
    },
    async createSession(input: { studentId: string; courseId: string; knowledgePointId?: string | null; mode?: 'TUTOR' | 'DIAGNOSTIC' }) {
      this.state = 'LOADING'
      this.clearError()
      try {
        const dto = await createCoachSession(input)
        const session = toCoachSessionVm(dto)
        this.session = session
        this.sessions = [session, ...this.sessions.filter((item) => item.sessionId !== session.sessionId)]
        this.state = 'READY'
        this.lastActions = []
        return session
      } catch (error) {
        this.setFailure(error, 'AI 学习教练暂时无法连接，请稍后重试')
        return null
      }
    },
    async restoreSession(sessionId: string) {
      const cached = this.sessions.find((item) => item.sessionId === sessionId)
      if (cached) {
        this.session = cached
        this.state = 'READY'
        return cached
      }
      this.state = 'LOADING'
      this.clearError()
      try {
        const dto = await getCoachSession(sessionId)
        const session = toCoachSessionVm(dto)
        this.session = session
        this.sessions = [session, ...this.sessions.filter((item) => item.sessionId !== session.sessionId)]
        this.state = 'READY'
        return session
      } catch (error) {
        const apiError = toApiError(error)
        this.errorCode = apiError.code
        this.error = '当前版本未提供历史会话恢复接口，可开始新会话'
        this.state = 'DEGRADED'
        this.session = null
        return null
      }
    },
    selectSession(sessionId: string) {
      const session = this.sessions.find((item) => item.sessionId === sessionId)
      if (session) this.session = session
      return session ?? null
    },
    async sendMessage(message: string) {
      const value = message.trim()
      if (!value || !this.session) return false
      this.sending = true
      this.clearError()
      this.lastActions = []
      const userMessage: CoachMessageVm = {
        id: localMessageId(),
        role: 'USER',
        content: value,
        citations: [],
        ragStatus: null,
      }
      this.session.messages.push(userMessage)
      try {
        const dto = await sendCoachMessage(this.session.sessionId, value)
        const assistant = toAssistantMessageVm(dto)
        this.session.messages.push(assistant)
        this.lastMessage = assistant
        this.lastActions = dto.actions
        this.state = 'READY'
        return true
      } catch (error) {
        const apiError = toApiError(error)
        this.errorCode = apiError.code
        this.error = apiError.code === 'AI_UPSTREAM_ERROR' ? 'AI 服务暂不可用' : '消息发送失败，请重试'
        this.state = apiError.code === 'AI_UPSTREAM_ERROR' ? 'DEGRADED' : 'ERROR'
        return false
      } finally {
        this.sending = false
      }
    },
    async startDiagnostic() {
      if (!this.session?.knowledgePointId) return null
      this.diagnosticState = 'SUBMITTING'
      this.diagnosticError = null
      try {
        const result = await createDiagnosticSet(this.session.sessionId, this.session.knowledgePointId)
        this.diagnosticState = 'SUCCESS'
        this.currentPracticeSetId = result.practiceSetId
        return result.practiceSetId
      } catch (error) {
        const apiError = toApiError(error)
        this.diagnosticError = apiError.code === 'AI_UPSTREAM_ERROR' ? 'AI 服务暂不可用，请稍后重试' : '诊断练习暂时无法创建，请重试'
        this.diagnosticState = apiError.code === 'AI_UPSTREAM_ERROR' ? 'DEGRADED' : 'ERROR'
        return null
      }
    },
    async generateSimilar(sourceAttemptId: string, count = 1) {
      if (!this.session?.sessionId) return null
      this.similarState = 'SUBMITTING'
      this.similarError = null
      try {
        const result = await generateSimilarQuestions({ sessionId: this.session.sessionId, sourceAttemptId, count })
        this.similarState = 'SUCCESS'
        this.currentPracticeSetId = result.practiceSetId
        return result.practiceSetId
      } catch (error) {
        const apiError = toApiError(error)
        this.similarError = apiError.code === 'AI_UPSTREAM_ERROR'
          ? 'AI 服务暂不可用，请稍后重试'
          : '类似题生成失败，请重试'
        this.similarState = 'ERROR'
        return null
      }
    },
    setPracticeSet(practiceSetId: string | null) {
      this.currentPracticeSetId = practiceSetId
    },
    setPracticeContext(context: Omit<PracticeDiscussionContext, 'kind'> & { kind?: 'PRACTICE' }) {
      this.practiceContext = context
        ? { ...context, kind: 'PRACTICE' }
        : null
      this.currentPracticeSetId = context.practiceSetId ?? null
    },
    setWrongBookContext(context: Omit<PracticeDiscussionContext, 'kind'>) {
      this.practiceContext = { ...context, kind: 'WRONG_BOOK' }
    },
    clearPracticeContext() {
      this.practiceContext = null
    },
  },
})
