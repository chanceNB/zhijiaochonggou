import { z } from 'zod'

export const TeacherAssignmentSchema = z.object({
  assignmentId: z.string(),
  interventionId: z.string(),
  practiceSetId: z.string(),
  studentId: z.string(),
  courseId: z.string(),
  classId: z.string(),
  knowledgePointId: z.string(),
  status: z.enum(['PENDING_STUDENT', 'IN_PROGRESS', 'COMPLETED']),
  dueAt: z.string().datetime({ offset: true }).nullable().optional(),
  createdAt: z.string().datetime({ offset: true }),
  demoRunId: z.string().nullable().optional(),
  demoCaseId: z.string().nullable().optional(),
  correlationId: z.string().nullable().optional(),
  sourceVersion: z.string().nullable().optional(),
  knowledgePointName: z.string().nullable().optional(),
  title: z.string().nullable().optional(),
  source: z.string().nullable().optional(),
})

export const LearningStateSummarySchema = z.object({
  knowledgePointId: z.string(),
  knowledgePointName: z.string().nullable().optional(),
  mastery: z.number().min(0).max(1),
  confidence: z.number().min(0).max(1),
  forgettingRisk: z.number().min(0).max(1),
  evidenceCount: z.number().int().nonnegative(),
})

export const NextActionSchema = z.object({
  type: z.string(),
  title: z.string(),
  knowledgePointId: z.string(),
  knowledgePointName: z.string().nullable().optional(),
  estimatedMinutes: z.number().int().nonnegative(),
})

export const TodayResponseSchema = z.object({
  studentId: z.string(),
  nextAction: NextActionSchema,
  teacherAssignment: TeacherAssignmentSchema.nullable(),
  learningState: LearningStateSummarySchema,
  demoCaseId: z.string().nullable().optional(),
})

export const CoachLearningContextSchema = z.object({
  mastery: z.number().min(0).max(1),
  confidence: z.number().min(0).max(1),
  forgettingRisk: z.number().min(0).max(1),
  weaknessScore: z.number().min(0).max(1),
  reasonCodes: z.string().nullable().optional(),
  modelVersion: z.string().nullable().optional(),
  sourceVersion: z.string().nullable().optional(),
})

export const CitationSchema = z.object({
  documentId: z.string(),
  chunkId: z.string(),
  title: z.string(),
  excerpt: z.string(),
  score: z.number(),
})

export const CoachMessageSchema = z.object({
  messageId: z.string().optional(),
  sessionId: z.string().optional(),
  messageType: z.string().optional(),
  content: z.string(),
  modelProvider: z.string().nullable().optional(),
  modelVersion: z.string().nullable().optional(),
  promptVersion: z.string().nullable().optional(),
  ragStatus: z.enum(['INDEXED', 'EMPTY', 'DEGRADED']).nullable().optional(),
  createdAt: z.string().datetime({ offset: true }).nullable().optional(),
  citations: z.array(CitationSchema).default([]),
})

export const CoachSessionResponseSchema = z.object({
  sessionId: z.string(),
  studentId: z.string(),
  courseId: z.string(),
  knowledgePointId: z.string().nullable().optional(),
  mode: z.string(),
  status: z.string(),
  ragStatus: z.enum(['INDEXED', 'EMPTY', 'DEGRADED']),
  context: CoachLearningContextSchema,
  messages: z.array(CoachMessageSchema).default([]),
  diagnosticQuestions: z.array(z.unknown()).default([]),
})

export const CoachActionSchema = z.object({
  type: z.string(),
  label: z.string(),
})

export const CoachMessageResponseSchema = z.object({
  assistantMessage: z.string(),
  citations: z.array(CitationSchema).default([]),
  actions: z.array(CoachActionSchema).default([]),
  ragStatus: z.enum(['INDEXED', 'EMPTY', 'DEGRADED']),
})

export const DiagnosticSetResponseSchema = z.object({
  practiceSetId: z.string(),
  questionCount: z.literal(2),
  questions: z.array(z.unknown()),
  ragStatus: z.enum(['INDEXED', 'EMPTY', 'DEGRADED']),
})

export const QuestionOptionSchema = z.object({
  optionId: z.string(),
  text: z.string(),
})

export const StudentQuestionSchema = z.object({
  questionId: z.string(),
  knowledgePointId: z.string(),
  knowledgePointName: z.string().nullable().optional(),
  questionType: z.string(),
  stem: z.string(),
  options: z.array(QuestionOptionSchema),
  difficulty: z.number(),
})

export const PracticeAttemptSummarySchema = z.object({
  attemptId: z.string(),
  questionId: z.string(),
  selectedAnswer: z.string(),
  correct: z.boolean(),
  responseTimeMs: z.number().int().nonnegative(),
  attemptTime: z.string().datetime({ offset: true }),
})

export const PracticeSetResponseSchema = z.object({
  practiceSetId: z.string(),
  studentId: z.string(),
  courseId: z.string(),
  classId: z.string(),
  source: z.string(),
  status: z.string(),
  coachSessionId: z.string().nullable().optional(),
  demoRunId: z.string().nullable().optional(),
  demoCaseId: z.string().nullable().optional(),
  correlationId: z.string().nullable().optional(),
  sourceVersion: z.string().nullable().optional(),
  questions: z.array(StudentQuestionSchema),
  attempts: z.array(PracticeAttemptSummarySchema),
})

export const PracticeAttemptResponseSchema = z.object({
  attemptId: z.string(),
  correct: z.boolean(),
  correctAnswer: z.string(),
  explanation: z.string(),
  misconceptionCode: z.string().nullable().optional(),
  canAddWrongBook: z.boolean(),
  canGenerateSimilar: z.boolean(),
})

export const LearningStateAfterSchema = z.object({
  mastery: z.number().min(0).max(1),
  confidence: z.number().min(0).max(1),
  forgettingRisk: z.number().min(0).max(1),
  evidenceCount: z.number().int().nonnegative(),
})

export const PracticeOutcomeSchema = z.object({
  outcomeId: z.string(),
  practiceSetId: z.string(),
  accuracy: z.number().min(0).max(1),
  attemptCount: z.number().int().nonnegative(),
  learningStateStatus: z.string(),
  transferValidation: z.string().nullable().optional(),
  learningStateAfter: LearningStateAfterSchema.nullable().optional(),
  interventionOutcomeId: z.string().nullable().optional(),
})

export const WrongBookItemSchema = z.object({
  wrongItemId: z.string(),
  studentId: z.string(),
  courseId: z.string(),
  classId: z.string(),
  questionId: z.string(),
  sourceAttemptId: z.string(),
  knowledgePointId: z.string(),
  knowledgePointName: z.string().nullable().optional(),
  questionStem: z.string().nullable().optional(),
  questionSummary: z.string().nullable().optional(),
  questionType: z.string().nullable().optional(),
  options: z.array(QuestionOptionSchema).optional(),
  reasonDisplayName: z.string().nullable().optional(),
  reason: z.string().nullable().optional(),
  status: z.string(),
  reviewCount: z.number().int().nonnegative(),
  addedAt: z.string().datetime({ offset: true }),
  repairedAt: z.string().datetime({ offset: true }).nullable().optional(),
  dataOrigin: z.string(),
  demoRunId: z.string().nullable().optional(),
  demoCaseId: z.string().nullable().optional(),
  correlationId: z.string().nullable().optional(),
  sourceVersion: z.string().nullable().optional(),
})

export const WrongBookPageSchema = z.object({
  items: z.array(WrongBookItemSchema),
  page: z.number().int(),
  size: z.number().int(),
  total: z.number().int().nonnegative(),
})

export const WrongBookReviewResponseSchema = z.object({
  correct: z.boolean(),
  status: z.string(),
  reviewCount: z.number().int().nonnegative(),
})

export const SimilarSetResponseSchema = z.object({
  practiceSetId: z.string(),
  questions: z.array(StudentQuestionSchema),
})

export type TodayResponseDto = z.infer<typeof TodayResponseSchema>
export type CoachSessionResponseDto = z.infer<typeof CoachSessionResponseSchema>
export type CoachMessageResponseDto = z.infer<typeof CoachMessageResponseSchema>
export type DiagnosticSetResponseDto = z.infer<typeof DiagnosticSetResponseSchema>
export type StudentQuestionDto = z.infer<typeof StudentQuestionSchema>
export type PracticeAttemptSummaryDto = z.infer<typeof PracticeAttemptSummarySchema>
export type PracticeSetResponseDto = z.infer<typeof PracticeSetResponseSchema>
export type PracticeAttemptResponseDto = z.infer<typeof PracticeAttemptResponseSchema>
export type PracticeOutcomeDto = z.infer<typeof PracticeOutcomeSchema>
export type WrongBookItemDto = z.infer<typeof WrongBookItemSchema>
export type WrongBookPageDto = z.infer<typeof WrongBookPageSchema>
export type WrongBookReviewResponseDto = z.infer<typeof WrongBookReviewResponseSchema>
export type SimilarSetResponseDto = z.infer<typeof SimilarSetResponseSchema>

export type StudentUiState =
  | 'INITIAL'
  | 'LOADING'
  | 'READY'
  | 'EMPTY'
  | 'SUBMITTING'
  | 'SUCCESS'
  | 'STALE'
  | 'DEGRADED'
  | 'FORBIDDEN'
  | 'ERROR'

export interface TodayVm {
  studentId: string
  nextAction: TodayResponseDto['nextAction']
  teacherAssignment: TodayResponseDto['teacherAssignment']
  learningState: TodayResponseDto['learningState']
  demoCaseId: string | null
}

export interface CoachContextVm {
  mastery: number
  confidence: number
  forgettingRisk: number
  weaknessScore: number
  reasonCodes?: string | null
  modelVersion?: string | null
  sourceVersion?: string | null
}

export interface CoachMessageVm {
  id: string
  role: 'USER' | 'ASSISTANT'
  content: string
  citations: z.infer<typeof CitationSchema>[]
  ragStatus?: 'INDEXED' | 'EMPTY' | 'DEGRADED' | null
}

export interface CoachSessionVm {
  sessionId: string
  studentId: string
  courseId: string
  knowledgePointId: string | null
  mode: string
  status: string
  ragStatus: 'INDEXED' | 'EMPTY' | 'DEGRADED'
  context: CoachContextVm
  messages: CoachMessageVm[]
}

export interface PracticeDiscussionContext {
  kind: 'PRACTICE' | 'WRONG_BOOK'
  coachSessionId?: string
  practiceSetId?: string
  questionId?: string
  attemptId?: string
  questionStem?: string
  knowledgePointName?: string
  selectedAnswer?: string
  correctAnswer?: string
  explanation?: string
  misconceptionLabel?: string
  wrongBookReason?: string
  reviewStatus?: string
}
