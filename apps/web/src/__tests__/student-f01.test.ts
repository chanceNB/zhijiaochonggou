import { beforeEach, describe, expect, it, vi } from 'vitest'
import { createPinia, setActivePinia } from 'pinia'
import { AxiosError, AxiosHeaders } from 'axios'
import { useStudentContextStore } from '../stores/studentContextStore'
import { useCoachStore } from '../stores/coachStore'
import { toTodayVm } from '../adapters/student/today'
import { toCoachSessionVm } from '../adapters/student/coach'
import { getStudentToday } from '../api/student/today'
import { createCoachSession, createDiagnosticSet, sendCoachMessage } from '../api/student/coach'
import type { CoachSessionResponseDto, TodayResponseDto } from '../types/contracts/student'

vi.mock('../api/student/today', () => ({ getStudentToday: vi.fn() }))
vi.mock('../api/student/coach', () => ({
  createCoachSession: vi.fn(),
  createDiagnosticSet: vi.fn(),
  getCoachSession: vi.fn(),
  sendCoachMessage: vi.fn(),
}))

const todayDto = (teacherAssignment: TodayResponseDto['teacherAssignment'] = null): TodayResponseDto => ({
  studentId: 'stu-test',
  nextAction: { type: teacherAssignment ? 'TEACHER_ASSIGNMENT' : 'AI_COACH_DIAGNOSTIC', title: 'Review current topic', knowledgePointId: 'kp-test', estimatedMinutes: 10 },
  teacherAssignment,
  learningState: { knowledgePointId: 'kp-test', mastery: 0.42, confidence: 0.61, forgettingRisk: 0.23, evidenceCount: 4 },
  demoCaseId: null,
})

const assignmentDto = {
  assignmentId: 'assignment-1', interventionId: 'intervention-1', practiceSetId: 'practice-1', studentId: 'stu-test', courseId: 'course-test', classId: 'class-test', knowledgePointId: 'kp-test', status: 'PENDING_STUDENT', dueAt: '2026-08-31T10:00:00Z', createdAt: '2026-08-30T10:00:00Z', demoRunId: null, demoCaseId: null, correlationId: null, sourceVersion: null,
} as const

const sessionDto: CoachSessionResponseDto = {
  sessionId: 'session-1', studentId: 'stu-test', courseId: 'course-test', knowledgePointId: 'kp-test', mode: 'TUTOR', status: 'ACTIVE', ragStatus: 'EMPTY',
  context: { mastery: 0.42, confidence: 0.61, forgettingRisk: 0.23, weaknessScore: 0.4, reasonCodes: null, modelVersion: null, sourceVersion: null }, messages: [], diagnosticQuestions: [],
}

beforeEach(() => {
  setActivePinia(createPinia())
  vi.clearAllMocks()
})

describe('F01 student context', () => {
  it('maps Today with no teacher assignment to the AI Coach action', async () => {
    vi.mocked(getStudentToday).mockResolvedValue(todayDto())
    const store = useStudentContextStore()

    await store.load()

    expect(store.state).toBe('READY')
    expect(store.data?.teacherAssignment).toBeNull()
    expect(store.data?.nextAction.type).toBe('AI_COACH_DIAGNOSTIC')
    expect(toTodayVm(todayDto()).learningState.mastery).toBe(0.42)
  })

  it('keeps a real teacher assignment as the primary action', async () => {
    vi.mocked(getStudentToday).mockResolvedValue(todayDto(assignmentDto))
    const store = useStudentContextStore()

    await store.load()

    expect(store.data?.teacherAssignment?.practiceSetId).toBe('practice-1')
    expect(store.data?.teacherAssignment?.status).toBe('PENDING_STUDENT')
  })
})

describe('F01 coach workflow', () => {
  it('preserves citations when the API returns indexed RAG evidence', async () => {
    vi.mocked(createCoachSession).mockResolvedValue(sessionDto)
    vi.mocked(sendCoachMessage).mockResolvedValue({
      assistantMessage: '回答',
      citations: [{ documentId: 'doc-1', chunkId: 'chunk-1', title: '课程讲义', excerpt: '资料摘录', score: 0.9 }],
      actions: [], ragStatus: 'INDEXED',
    })
    const store = useCoachStore()
    await store.createSession({ studentId: 'stu-test', courseId: 'course-test', knowledgePointId: 'kp-test' })
    await store.sendMessage('请解释这个知识点')

    expect(store.messages[1].citations).toHaveLength(1)
    expect(store.messages[1].ragStatus).toBe('INDEXED')
  })

  it('shows the standard no-citation state for empty and degraded RAG', async () => {
    vi.mocked(createCoachSession).mockResolvedValue(sessionDto)
    const store = useCoachStore()
    await store.createSession({ studentId: 'stu-test', courseId: 'course-test', knowledgePointId: 'kp-test' })
    vi.mocked(sendCoachMessage).mockResolvedValueOnce({ assistantMessage: '无资料回答', citations: [], actions: [], ragStatus: 'EMPTY' })
    await store.sendMessage('问题一')
    expect(store.lastMessage?.citations).toHaveLength(0)
    expect(store.lastMessage?.ragStatus).toBe('EMPTY')

    vi.mocked(sendCoachMessage).mockResolvedValueOnce({ assistantMessage: '降级回答', citations: [], actions: [], ragStatus: 'DEGRADED' })
    await store.sendMessage('问题二')
    expect(store.lastMessage?.ragStatus).toBe('DEGRADED')
  })

  it('keeps the session and reports LLM degradation without dropping the user message', async () => {
    vi.mocked(createCoachSession).mockResolvedValue(sessionDto)
    const store = useCoachStore()
    await store.createSession({ studentId: 'stu-test', courseId: 'course-test', knowledgePointId: 'kp-test' })
    const payload = { code: 'AI_UPSTREAM_ERROR', message: 'AI unavailable', requestId: 'req-1', data: null, timestamp: '2026-08-31T00:00:00Z' }
    vi.mocked(sendCoachMessage).mockRejectedValue(new AxiosError('bad gateway', 'ERR_BAD_RESPONSE', undefined, undefined, {
      status: 502,
      statusText: 'Bad Gateway',
      headers: new AxiosHeaders(),
      config: { headers: new AxiosHeaders() },
      data: payload,
    }))

    await store.sendMessage('请继续解释')

    expect(store.state).toBe('DEGRADED')
    expect(store.error).toBe('AI 服务暂不可用')
    expect(store.messages.at(-1)?.content).toBe('请继续解释')
  })

  it('creates exactly two diagnostic questions and returns the practice set id', async () => {
    vi.mocked(createCoachSession).mockResolvedValue(sessionDto)
    vi.mocked(createDiagnosticSet).mockResolvedValue({ practiceSetId: 'practice-diagnostic-1', questionCount: 2, questions: [], ragStatus: 'EMPTY' })
    const store = useCoachStore()
    await store.createSession({ studentId: 'stu-test', courseId: 'course-test', knowledgePointId: 'kp-test' })

    const practiceSetId = await store.startDiagnostic()

    expect(practiceSetId).toBe('practice-diagnostic-1')
    expect(createDiagnosticSet).toHaveBeenCalledWith('session-1', 'kp-test')
    expect(store.diagnosticState).toBe('SUCCESS')
    expect(toCoachSessionVm(sessionDto).knowledgePointId).toBe('kp-test')
  })
})
