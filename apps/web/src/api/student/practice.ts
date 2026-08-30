import { apiClient, parseApiEnvelope } from '@/api/client'
import {
  PracticeAttemptResponseSchema,
  PracticeOutcomeSchema,
  PracticeSetResponseSchema,
  SimilarSetResponseSchema,
  type PracticeAttemptResponseDto,
  type PracticeOutcomeDto,
  type PracticeSetResponseDto,
  type SimilarSetResponseDto,
} from '@/types/contracts/student'

const idempotencyKey = (prefix: string, stable?: string) => {
  const random = typeof crypto !== 'undefined' && 'randomUUID' in crypto ? crypto.randomUUID() : `${Date.now()}-${Math.random()}`
  return `${prefix}-${stable ?? random}`
}

export async function getPracticeSet(practiceSetId: string): Promise<PracticeSetResponseDto> {
  const response = await apiClient.get(`/student/practice-sets/${encodeURIComponent(practiceSetId)}`)
  const envelope = parseApiEnvelope(response.data)
  return PracticeSetResponseSchema.parse(envelope.data)
}

export async function submitPracticeAttempt(input: {
  practiceSetId: string
  questionId: string
  answer: string
  durationSeconds: number
  idempotencyKey?: string
}): Promise<PracticeAttemptResponseDto> {
  const response = await apiClient.post(
    `/student/practice-sets/${encodeURIComponent(input.practiceSetId)}/attempts`,
    { questionId: input.questionId, answer: input.answer, durationSeconds: input.durationSeconds },
    { headers: { 'Idempotency-Key': input.idempotencyKey ?? idempotencyKey('practice-attempt', `${input.practiceSetId}-${input.questionId}`) } },
  )
  const envelope = parseApiEnvelope(response.data)
  return PracticeAttemptResponseSchema.parse(envelope.data)
}

export async function completePracticeSet(practiceSetId: string, key?: string): Promise<PracticeOutcomeDto> {
  const response = await apiClient.post(
    `/student/practice-sets/${encodeURIComponent(practiceSetId)}/complete`,
    undefined,
    { headers: { 'Idempotency-Key': key ?? idempotencyKey('practice-complete', practiceSetId) } },
  )
  const envelope = parseApiEnvelope(response.data)
  return PracticeOutcomeSchema.parse(envelope.data)
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
    { headers: { 'Idempotency-Key': input.idempotencyKey ?? idempotencyKey('similar-questions', input.sourceAttemptId) } },
  )
  const envelope = parseApiEnvelope(response.data)
  return SimilarSetResponseSchema.parse(envelope.data)
}
