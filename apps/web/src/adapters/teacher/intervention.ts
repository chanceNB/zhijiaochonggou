import type {
  InterventionOutcomeDto,
  InterventionResponseDto,
  RecommendationSnapshotDto,
} from '@/types/contracts/teacher'

export type RecommendationVm = RecommendationSnapshotDto
export type InterventionVm = InterventionResponseDto
export type OutcomeVm = InterventionOutcomeDto

const strategyLabels: Record<string, string> = {
  CONCEPT_REMEDIATION: '概念边界校准',
  VISUAL_TRANSFER_PRACTICE: '可视化迁移练习',
  AI_GUIDED_VARIATION: '分层变式反馈',
}

export function strategyLabel(code: string): string {
  return strategyLabels[code] ?? '已选教学方案'
}

export function statusLabel(value?: string | null): string {
  const labels: Record<string, string> = {
    PENDING_TEACHER_REVIEW: '待教师复核', PROPOSED: '待审核', APPROVED: '已审核', COMMITTED: '已下发',
    PENDING_STUDENT: '等待学生完成', IN_PROGRESS: '学生进行中', COMPLETED: '已完成',
    PASS: '迁移验证通过', FAIL: '迁移验证未通过', NOT_RUN: '尚未验证',
  }
  return labels[value ?? ''] ?? value ?? '未知状态'
}

export function percent(value: number): string {
  return `${Math.round(value * 100)}%`
}

export function signedPercent(value: number): string {
  const rounded = Math.round(value * 100)
  return `${rounded > 0 ? '+' : ''}${rounded}%`
}

export function formatTimestamp(value?: string | null): string {
  if (!value) return '尚未记录'
  return new Intl.DateTimeFormat('zh-CN', { month: 'numeric', day: 'numeric', hour: '2-digit', minute: '2-digit' }).format(new Date(value))
}
