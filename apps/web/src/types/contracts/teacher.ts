import { z } from 'zod'

const Timestamp = z.string().datetime({ offset: true })

export const TeacherStudentContextSchema = z.object({
  studentId: z.string(), displayName: z.string(), courseId: z.string(), courseName: z.string(),
  classId: z.string(), className: z.string(), demoRunId: z.string().nullable().optional(),
  demoCaseId: z.string().nullable().optional(), correlationId: z.string().nullable().optional(),
})
export const TeacherPriorityItemSchema = z.object({
  type: z.string(), title: z.string(), description: z.string(), status: z.string(),
  knowledgePointName: z.string().nullable().optional(), strategy: z.string().nullable().optional(),
})
export const TeacherRecommendationSchema = z.object({
  recommendationId: z.string(), summary: z.string(), status: z.string(), knowledgePointId: z.string().optional(), knowledgePointName: z.string(), capturedAt: Timestamp,
  demoRunId: z.string().nullable().optional(), demoCaseId: z.string().nullable().optional(), correlationId: z.string().nullable().optional(),
})
export const TeacherPendingOutcomeSchema = z.object({
  interventionId: z.string(), strategy: z.string(), status: z.string(), knowledgePointName: z.string(), committedAt: Timestamp.nullable().optional(),
  demoRunId: z.string().nullable().optional(), demoCaseId: z.string().nullable().optional(), correlationId: z.string().nullable().optional(),
})
export const TeacherWorkbenchSchema = z.object({
  currentStudent: TeacherStudentContextSchema.nullable(),
  priorityItems: z.array(TeacherPriorityItemSchema),
  pendingRecommendations: z.array(TeacherRecommendationSchema),
  pendingOutcomes: z.array(TeacherPendingOutcomeSchema),
})

export const TeacherStudentSchema = z.object({
  studentId: z.string(), displayName: z.string(), courseId: z.string(), courseName: z.string(),
  classId: z.string(), className: z.string(), demoRunId: z.string().nullable().optional(),
  demoCaseId: z.string().nullable().optional(), correlationId: z.string().nullable().optional(),
})
export const TeacherLearningStateSchema = z.object({
  knowledgePointId: z.string().optional(), knowledgePointName: z.string(), mastery: z.number(), confidence: z.number(), forgettingRisk: z.number(),
  evidenceCount: z.number().int(), weaknessScore: z.number().nullable().optional(), reasonCodes: z.string().nullable().optional(), computedAt: Timestamp,
})
export const TeacherRecentAttemptSchema = z.object({
  questionId: z.string(), knowledgePointName: z.string(), questionSummary: z.string(), correct: z.boolean(),
  durationSeconds: z.number().int(), attemptTime: Timestamp, misconceptionCode: z.string().nullable().optional(),
  demoRunId: z.string().nullable().optional(), demoCaseId: z.string().nullable().optional(), correlationId: z.string().nullable().optional(),
})
export const TeacherDiagnosisSummarySchema = z.object({
  severity: z.string(), confidence: z.number(), primaryHypothesis: z.string(),
  evidence: z.array(z.string()), counterEvidence: z.array(z.string()),
})
export const TeacherInterventionSchema = z.object({
  strategy: z.string(), status: z.string(), teacherRationale: z.string(), assignmentStatus: z.string().nullable().optional(),
  transferValidation: z.string().nullable().optional(), practiceAccuracyAfter: z.number().nullable().optional(),
  demoRunId: z.string().nullable().optional(), demoCaseId: z.string().nullable().optional(), correlationId: z.string().nullable().optional(),
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
  courseName: z.string(), className: z.string(), demoRunId: z.string().nullable().optional(),
  demoCaseId: z.string().nullable().optional(), correlationId: z.string().nullable().optional(),
})

export const RecommendationCandidateSchema = z.object({
  candidateIndex: z.number().int().optional(), strategyCode: z.string(), title: z.string(), rationale: z.string(),
  actionDescription: z.string(), sourceSnapshot: z.string().optional(),
})
export const RecommendationSnapshotSchema = z.object({
  recommendationId: z.string(), studentId: z.string(), courseId: z.string(), classId: z.string(), knowledgePointId: z.string(),
  demoRunId: z.string().nullable().optional(), demoCaseId: z.string().nullable().optional(), correlationId: z.string().nullable().optional(),
  analysisSummary: z.string(), evidenceRefs: z.array(z.string()), candidates: z.array(RecommendationCandidateSchema).length(3),
  source: z.string(), captureMode: z.string(), status: z.string(), generatedAt: Timestamp, capturedAt: Timestamp, sourceVersion: z.string(),
})
export const InterventionAssignmentSchema = z.object({
  assignmentId: z.string(), interventionId: z.string(), practiceSetId: z.string(), studentId: z.string(), courseId: z.string(),
  classId: z.string(), knowledgePointId: z.string(), status: z.enum(['PENDING_STUDENT', 'IN_PROGRESS', 'COMPLETED']),
  dueAt: Timestamp.nullable().optional(), createdAt: Timestamp, demoRunId: z.string().nullable().optional(), demoCaseId: z.string().nullable().optional(),
  correlationId: z.string().nullable().optional(), sourceVersion: z.string().nullable().optional(),
})
export const InterventionResponseSchema = z.object({
  interventionId: z.string(), recommendationId: z.string(), strategyCode: z.string(), predictedLift: z.number(),
  predictionInterval: z.object({ low: z.number(), high: z.number() }), status: z.enum(['PROPOSED', 'APPROVED', 'COMMITTED']), version: z.number().int(),
  assignmentId: z.string().nullable().optional(), practiceSetId: z.string().nullable().optional(), assignment: InterventionAssignmentSchema.nullable().optional(),
})
export const InterventionOutcomeSchema = z.object({
  outcomeId: z.string(), interventionId: z.string(), assignmentId: z.string(), practiceSetId: z.string(), studentId: z.string(), courseId: z.string(),
  classId: z.string(), knowledgePointId: z.string(), predictedLift: z.number(), predictionLow: z.number(), predictionHigh: z.number(),
  masteryBefore: z.number(), confidenceBefore: z.number(), forgettingRiskBefore: z.number(), weaknessScoreBefore: z.number().nullable().optional(), evidenceCountBefore: z.number().int(),
  masteryAfter: z.number(), confidenceAfter: z.number(), forgettingRiskAfter: z.number(), evidenceCountAfter: z.number().int(), actualLift: z.number(), predictionDeviation: z.number(),
  transferValidation: z.enum(['PASS', 'FAIL', 'NOT_RUN']), practiceAccuracyAfter: z.number(), dataOrigin: z.string(), demoRunId: z.string().nullable().optional(),
  demoCaseId: z.string().nullable().optional(), correlationId: z.string().nullable().optional(), sourceVersion: z.string(), completedAt: Timestamp,
})

export type TeacherWorkbenchDto = z.infer<typeof TeacherWorkbenchSchema>
export type TeacherProfileDto = z.infer<typeof TeacherProfileSchema>
export type TeacherDiagnosisDto = z.infer<typeof TeacherDiagnosisSchema>
export type RecommendationSnapshotDto = z.infer<typeof RecommendationSnapshotSchema>
export type InterventionAssignmentDto = z.infer<typeof InterventionAssignmentSchema>
export type InterventionResponseDto = z.infer<typeof InterventionResponseSchema>
export type InterventionOutcomeDto = z.infer<typeof InterventionOutcomeSchema>
