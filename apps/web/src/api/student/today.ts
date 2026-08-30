import { apiClient, parseApiEnvelope } from '@/api/client'
import { TodayResponseSchema, type TodayResponseDto } from '@/types/contracts/student'

export interface TodayQuery {
  studentId?: string
  courseId?: string
}

export async function getStudentToday(query: TodayQuery = {}): Promise<TodayResponseDto> {
  const response = await apiClient.get('/student/today', { params: query })
  const envelope = parseApiEnvelope(response.data)
  return TodayResponseSchema.parse(envelope.data)
}
