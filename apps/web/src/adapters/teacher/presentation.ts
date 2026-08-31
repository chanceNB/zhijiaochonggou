const strategyLabels: Record<string, string> = {
  VISUAL_TRANSFER_PRACTICE: '可视化迁移练习',
  CONCEPT_REMEDIATION: '概念补强',
  AI_GUIDED_VARIATION: 'AI 引导变式练习',
}

const statusLabels: Record<string, string> = {
  AVAILABLE: '待关注', PENDING_STUDENT: '待完成', IN_PROGRESS: '进行中', COMPLETED: '已完成',
  PROPOSED: '待确认', APPROVED: '已确认', COMMITTED: '已提交', PASS: '已通过', FAIL: '未通过', NOT_RUN: '未执行',
  PENDING_TEACHER_REVIEW: '待教师复核',
}

export function teacherStrategyLabel(value: string | null | undefined): string {
  if (!value) return '未指定策略'
  return strategyLabels[value] ?? value
}

export function teacherStatusLabel(value: string | null | undefined): string {
  if (!value) return '暂无'
  return statusLabels[value] ?? value
}
