import { apiClient, parseApiEnvelope } from '@/api/client'
import {
  InterventionOutcomeSchema, InterventionResponseSchema, RecommendationSnapshotSchema, TeacherDiagnosisSchema, TeacherProfileSchema,
  TeacherWorkbenchSchema, type InterventionOutcomeDto, type InterventionResponseDto, type RecommendationSnapshotDto,
  type TeacherDiagnosisDto, type TeacherProfileDto, type TeacherWorkbenchDto,
} from '@/types/contracts/teacher'

export async function getTeacherWorkbench(): Promise<TeacherWorkbenchDto> {
  const response = await apiClient.get('/teacher/workbench')
  return TeacherWorkbenchSchema.parse(parseApiEnvelope(response.data).data)
}

export async function getTeacherProfile(studentId: string, courseId: string): Promise<TeacherProfileDto> {
  const response = await apiClient.get(`/teacher/students/${encodeURIComponent(studentId)}/profile`, { params: { courseId } })
  return TeacherProfileSchema.parse(parseApiEnvelope(response.data).data)
}

export async function getTeacherDiagnosis(caseId: string): Promise<TeacherDiagnosisDto> {
  const response = await apiClient.get(`/teacher/diagnosis-cases/${encodeURIComponent(caseId)}`)
  return TeacherDiagnosisSchema.parse(parseApiEnvelope(response.data).data)
}

function idempotencyKey(prefix: string): string {
  const random = typeof crypto !== 'undefined' && 'randomUUID' in crypto ? crypto.randomUUID() : `${Date.now()}-${Math.random().toString(16).slice(2)}`
  return `${prefix}-${random}`
}

export async function getAnalysisRecommendation(recommendationId: string): Promise<RecommendationSnapshotDto> {
  const response = await apiClient.get(`/teacher/analysis-recommendations/${encodeURIComponent(recommendationId)}`)
  return RecommendationSnapshotSchema.parse(parseApiEnvelope(response.data).data)
}

export async function proposeIntervention(body: { recommendationId: string; strategyCode: string; teacherRationale: string }): Promise<InterventionResponseDto> {
  const response = await apiClient.post('/teacher/interventions', body, { headers: { 'Idempotency-Key': idempotencyKey('web-propose') } })
  return InterventionResponseSchema.parse(parseApiEnvelope(response.data).data)
}

export async function approveIntervention(interventionId: string, version: number): Promise<InterventionResponseDto> {
  const response = await apiClient.post(`/teacher/interventions/${encodeURIComponent(interventionId)}/approve`, null, {
    headers: { 'If-Match': String(version), 'Idempotency-Key': idempotencyKey('web-approve') },
  })
  return InterventionResponseSchema.parse(parseApiEnvelope(response.data).data)
}

export async function commitIntervention(interventionId: string, version: number, dueAt?: string | null): Promise<InterventionResponseDto> {
  const response = await apiClient.post(`/teacher/interventions/${encodeURIComponent(interventionId)}/commit`, dueAt ? { dueAt } : {}, {
    headers: { 'If-Match': String(version), 'Idempotency-Key': idempotencyKey('web-commit') },
  })
  return InterventionResponseSchema.parse(parseApiEnvelope(response.data).data)
}

export async function getIntervention(interventionId: string): Promise<InterventionResponseDto> {
  const response = await apiClient.get(`/teacher/interventions/${encodeURIComponent(interventionId)}`)
  return InterventionResponseSchema.parse(parseApiEnvelope(response.data).data)
}

export async function getInterventionByRecommendation(recommendationId: string): Promise<InterventionResponseDto> {
  const response = await apiClient.get(`/teacher/interventions/by-recommendation/${encodeURIComponent(recommendationId)}`)
  return InterventionResponseSchema.parse(parseApiEnvelope(response.data).data)
}

export async function getInterventionOutcome(interventionId: string): Promise<InterventionOutcomeDto> {
  const response = await apiClient.get(`/teacher/interventions/${encodeURIComponent(interventionId)}/outcome`)
  return InterventionOutcomeSchema.parse(parseApiEnvelope(response.data).data)
}
