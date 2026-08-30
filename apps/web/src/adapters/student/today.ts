import type { TodayResponseDto, TodayVm } from '@/types/contracts/student'
import { displayActionTitle } from '@/adapters/student/presentation'

export function toTodayVm(dto: TodayResponseDto): TodayVm {
  return {
    studentId: dto.studentId,
    nextAction: { ...dto.nextAction, title: displayActionTitle(dto.nextAction.title) },
    teacherAssignment: dto.teacherAssignment,
    learningState: dto.learningState,
    demoCaseId: dto.demoCaseId ?? null,
  }
}
