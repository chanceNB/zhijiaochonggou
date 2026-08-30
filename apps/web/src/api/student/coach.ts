import { apiClient, parseApiEnvelope } from '@/api/client'
import {
  CoachMessageResponseSchema,
  CoachSessionResponseSchema,
  DiagnosticSetResponseSchema,
  SimilarSetResponseSchema,
  type CoachMessageResponseDto,
  type CoachSessionResponseDto,
  type DiagnosticSetResponseDto,
  type SimilarSetResponseDto,
} from '@/types/contracts/student'

export interface CreateCoachSessionInput {
  studentId: string
  courseId: string
  knowledgePointId?: string | null
  mode?: 'TUTOR' | 'DIAGNOSTIC'
}

const idempotencyKey = (prefix: string) => {
  const random = typeof crypto !== 'undefined' && 'randomUUID' in crypto ? crypto.randomUUID() : `${Date.now()}-${Math.random()}`
  return `${prefix}-${random}`
}

export async function createCoachSession(input: CreateCoachSessionInput): Promise<CoachSessionResponseDto> {
  const response = await apiClient.post('/student/coach/sessions', input, {
    headers: { 'Idempotency-Key': idempotencyKey('coach-session') },
  })
  const envelope = parseApiEnvelope(response.data)
  return CoachSessionResponseSchema.parse(envelope.data)
}

export async function getCoachSession(sessionId: string): Promise<CoachSessionResponseDto> {
  const response = await apiClient.get(`/student/coach/sessions/${encodeURIComponent(sessionId)}`)
  const envelope = parseApiEnvelope(response.data)
  return CoachSessionResponseSchema.parse(envelope.data)
}

export async function sendCoachMessage(sessionId: string, message: string): Promise<CoachMessageResponseDto> {
  const response = await apiClient.post(
    `/student/coach/sessions/${encodeURIComponent(sessionId)}/messages`,
    { message },
  )
  const envelope = parseApiEnvelope(response.data)
  return CoachMessageResponseSchema.parse(envelope.data)
}

export async function createDiagnosticSet(
  sessionId: string,
  knowledgePointId: string,
): Promise<DiagnosticSetResponseDto> {
  const response = await apiClient.post(
    `/student/coach/sessions/${encodeURIComponent(sessionId)}/diagnostic-sets`,
    { knowledgePointId, questionCount: 2 },
    { headers: { 'Idempotency-Key': idempotencyKey('coach-diagnostic') } },
  )
  const envelope = parseApiEnvelope(response.data)
  return DiagnosticSetResponseSchema.parse(envelope.data)
}

export async function generateSimilarQuestions(input: {
  sessionId: string
  sourceAttemptId: string
  count?: number
  idempotencyKey?: string
}): Promise<SimilarSetResponseDto> {
  const response = await apiClient.post(
    `/student/coach/sessions/${encodeURIComponent(input.sessionId)}/similar-questions`,
    { sourceAttemptId: input.sourceAttemptId, count: input.count ?? 1 },
    { headers: { 'Idempotency-Key': input.idempotencyKey ?? idempotencyKey('similar-questions') } },
  )
  const envelope = parseApiEnvelope(response.data)
  return SimilarSetResponseSchema.parse(envelope.data)
}
