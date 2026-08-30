import type {
  PracticeAttemptResponseDto,
  PracticeOutcomeDto,
  PracticeSetResponseDto,
  StudentQuestionDto,
  WrongBookItemDto,
  WrongBookPageDto,
} from '@/types/contracts/student'

export interface PracticeQuestionVm extends StudentQuestionDto {
  index: number
}

export interface PracticeSetVm {
  practiceSetId: string
  studentId: string
  courseId: string
  classId: string
  source: string
  status: string
  coachSessionId: string | null
  questions: PracticeQuestionVm[]
  attempts: PracticeSetResponseDto['attempts']
}

export interface AttemptFeedbackVm extends PracticeAttemptResponseDto {
  selectedAnswer: string
}

export interface PracticeOutcomeVm extends PracticeOutcomeDto {
  accuracyPercent: number
}

export interface WrongBookItemVm extends WrongBookItemDto {
  statusLabel: string
  reasonLabel: string
}

export interface WrongBookPageVm {
  items: WrongBookItemVm[]
  page: number
  size: number
  total: number
}

export function toPracticeSetVm(dto: PracticeSetResponseDto): PracticeSetVm {
  return {
    practiceSetId: dto.practiceSetId,
    studentId: dto.studentId,
    courseId: dto.courseId,
    classId: dto.classId,
    source: dto.source,
    status: dto.status,
    coachSessionId: dto.coachSessionId ?? null,
    questions: dto.questions.map((question, index) => ({ ...question, index })),
    attempts: dto.attempts,
  }
}

export function toAttemptFeedbackVm(dto: PracticeAttemptResponseDto, selectedAnswer: string): AttemptFeedbackVm {
  return { ...dto, selectedAnswer }
}

export function toPracticeOutcomeVm(dto: PracticeOutcomeDto): PracticeOutcomeVm {
  return { ...dto, accuracyPercent: Math.round(dto.accuracy * 100) }
}

const statusLabels: Record<string, string> = {
  TO_REVIEW: '待复习',
  LEARNING: '复习中',
  MASTERED: '已掌握',
}

export function toWrongBookItemVm(dto: WrongBookItemDto): WrongBookItemVm {
  return {
    ...dto,
    statusLabel: statusLabels[dto.status] ?? dto.status,
    reasonLabel: dto.reason?.trim() || '答题错误',
  }
}

export function toWrongBookPageVm(dto: WrongBookPageDto): WrongBookPageVm {
  return { items: dto.items.map(toWrongBookItemVm), page: dto.page, size: dto.size, total: dto.total }
}
