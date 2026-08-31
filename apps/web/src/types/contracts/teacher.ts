import { z } from 'zod'

const Timestamp = z.string().datetime({ offset: true })

export const TeacherStudentContextSchema = z.object({
  studentId: z.string(), displayName: z.string(), courseId: z.string(), courseName: z.string(),
  classId: z.string(), className: z.string(), demoCaseId: z.string().nullable().optional(),
})
export const TeacherPriorityItemSchema = z.object({
  type: z.string(), title: z.string(), description: z.string(), status: z.string(),
  knowledgePointName: z.string().nullable().optional(), strategy: z.string().nullable().optional(),
})
export const TeacherRecommendationSchema = z.object({
  recommendationId: z.string(), summary: z.string(), status: z.string(), knowledgePointName: z.string(), capturedAt: Timestamp,
})
export const TeacherPendingOutcomeSchema = z.object({
  interventionId: z.string(), strategy: z.string(), status: z.string(), knowledgePointName: z.string(), committedAt: Timestamp.nullable().optional(),
})
export const TeacherWorkbenchSchema = z.object({
  currentStudent: TeacherStudentContextSchema.nullable(),
  priorityItems: z.array(TeacherPriorityItemSchema),
  pendingRecommendations: z.array(TeacherRecommendationSchema),
  pendingOutcomes: z.array(TeacherPendingOutcomeSchema),
})

export const TeacherStudentSchema = z.object({
  studentId: z.string(), displayName: z.string(), courseId: z.string(), courseName: z.string(),
  classId: z.string(), className: z.string(),
})
export const TeacherLearningStateSchema = z.object({
  knowledgePointName: z.string(), mastery: z.number(), confidence: z.number(), forgettingRisk: z.number(),
  evidenceCount: z.number().int(), weaknessScore: z.number().nullable().optional(), reasonCodes: z.string().nullable().optional(), computedAt: Timestamp,
})
export const TeacherRecentAttemptSchema = z.object({
  questionId: z.string(), knowledgePointName: z.string(), questionSummary: z.string(), correct: z.boolean(),
  durationSeconds: z.number().int(), attemptTime: Timestamp, misconceptionCode: z.string().nullable().optional(),
})
export const TeacherDiagnosisSummarySchema = z.object({
  severity: z.string(), confidence: z.number(), primaryHypothesis: z.string(),
  evidence: z.array(z.string()), counterEvidence: z.array(z.string()),
})
export const TeacherInterventionSchema = z.object({
  strategy: z.string(), status: z.string(), teacherRationale: z.string(), assignmentStatus: z.string().nullable().optional(),
  transferValidation: z.string().nullable().optional(), practiceAccuracyAfter: z.number().nullable().optional(),
})
export const TeacherProfileSchema = z.object({
  student: TeacherStudentSchema,
  learningState: TeacherLearningStateSchema.nullable(),
  recentAttempts: z.array(TeacherRecentAttemptSchema),
  diagnosis: TeacherDiagnosisSummarySchema.nullable(),
  intervention: TeacherInterventionSchema.nullable(),
})
export const TeacherDiagnosisSchema = z.object({
  caseId: z.string(), severity: z.string(), confidence: z.number(), primaryHypothesis: z.string(),
  evidence: z.array(z.string()), counterEvidence: z.array(z.string()), studentName: z.string(),
  courseName: z.string(), className: z.string(),
})

export type TeacherWorkbenchDto = z.infer<typeof TeacherWorkbenchSchema>
export type TeacherProfileDto = z.infer<typeof TeacherProfileSchema>
export type TeacherDiagnosisDto = z.infer<typeof TeacherDiagnosisSchema>
