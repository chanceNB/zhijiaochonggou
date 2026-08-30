import { apiClient, parseApiEnvelope } from '@/api/client'
import {
  WrongBookPageSchema,
  WrongBookReviewResponseSchema,
  type WrongBookPageDto,
  type WrongBookReviewResponseDto,
} from '@/types/contracts/student'

const idempotencyKey = (prefix: string, stable?: string) => {
  const random = typeof crypto !== 'undefined' && 'randomUUID' in crypto ? crypto.randomUUID() : `${Date.now()}-${Math.random()}`
  return `${prefix}-${stable ?? random}`
}

export async function getWrongBook(query: { knowledgePointId?: string; status?: string; page?: number; size?: number } = {}): Promise<WrongBookPageDto> {
  const response = await apiClient.get('/student/wrong-book', { params: query })
  const envelope = parseApiEnvelope(response.data)
  return WrongBookPageSchema.parse(envelope.data)
}

export async function addAttemptToWrongBook(attemptId: string, reason?: string): Promise<WrongBookPageDto['items'][number]> {
  const response = await apiClient.post(
    `/student/practice-attempts/${encodeURIComponent(attemptId)}/wrong-book`,
    reason ? { reason } : {},
    { headers: { 'Idempotency-Key': idempotencyKey('wrong-book', attemptId) } },
  )
  const envelope = parseApiEnvelope(response.data)
  return WrongBookPageSchema.shape.items.element.parse(envelope.data)
}

export async function reviewWrongBookItem(input: {
  wrongItemId: string
  answer: string
  durationSeconds: number
  idempotencyKey?: string
}): Promise<WrongBookReviewResponseDto> {
  const response = await apiClient.post(
    `/student/wrong-book/${encodeURIComponent(input.wrongItemId)}/review`,
    { answer: input.answer, durationSeconds: input.durationSeconds },
    { headers: { 'Idempotency-Key': input.idempotencyKey ?? idempotencyKey('wrong-book-review', input.wrongItemId) } },
  )
  const envelope = parseApiEnvelope(response.data)
  return WrongBookReviewResponseSchema.parse(envelope.data)
}
