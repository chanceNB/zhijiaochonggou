import type { TodayResponseDto, TodayVm } from '@/types/contracts/student'

export function toTodayVm(dto: TodayResponseDto): TodayVm {
  return {
    studentId: dto.studentId,
    nextAction: dto.nextAction,
    teacherAssignment: dto.teacherAssignment,
    learningState: dto.learningState,
    demoCaseId: dto.demoCaseId ?? null,
  }
}
