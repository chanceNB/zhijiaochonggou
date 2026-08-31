import { apiClient, parseApiEnvelope } from '@/api/client'
import { TeacherDiagnosisSchema, TeacherProfileSchema, TeacherWorkbenchSchema, type TeacherDiagnosisDto, type TeacherProfileDto, type TeacherWorkbenchDto } from '@/types/contracts/teacher'

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
