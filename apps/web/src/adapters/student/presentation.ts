const technicalIdPattern = /^(stu|course|kp|q|attempt|wrong|practice|session|intervention|outcome)[-_]/i

export function isTechnicalIdentifier(value: unknown): value is string {
  return typeof value === 'string' && technicalIdPattern.test(value.trim())
}

export function displayStudentName(_value?: string | null): string {
  return '学生'
}

export function displayCourseName(value?: string | null): string {
  if (!value || isTechnicalIdentifier(value)) return '当前课程'
  return value
}

export function displayKnowledgePoint(value?: string | null, displayName?: string | null): string {
  const readableName = typeof displayName === 'string' ? displayName.trim() : ''
  if (readableName && !isTechnicalIdentifier(readableName)) return readableName
  if (!value || isTechnicalIdentifier(value)) return '当前知识点'
  return value
}

export function displayActionTitle(value?: string | null): string {
  const normalized = value?.trim().toLowerCase()
  if (!normalized || normalized === 'continue learning' || normalized === 'continue_learning') return '继续学习'
  if (normalized === 'review current topic' || normalized === 'review_current_topic') return '复习当前知识点'
  if (normalized === 'start practice' || normalized === 'start_practice') return '开始练习'
  if (normalized === 'concept_remediation') return '概念边界校准'
  if (normalized === 'visual_transfer_practice') return '可视化迁移练习'
  if (normalized === 'ai_guided_variation') return '分层变式反馈'
  if (isTechnicalIdentifier(value)) return '继续学习'
  return value ?? '继续学习'
}

const misconceptionLabels: Record<string, string> = {
  ANSWER_MISMATCH: '答案与知识点理解不一致',
  CONFUSED_BFS_DFS: '遍历方法辨析还不够清晰',
  BFS_QUEUE_ORDER: '遍历顺序理解还不够清晰',
  DFS_BACKTRACKING: '深度遍历过程还需要复盘',
}

export function displayMisconception(value?: string | null): string {
  const raw = value?.trim()
  if (!raw) return '答题思路需要复盘'
  const byCode = misconceptionLabels[raw.toUpperCase()]
  if (byCode) return byCode
  if (/confused|misconception|answer mismatch|unweighted shortest path/i.test(raw)) return '知识点辨析还不够清晰'
  if (/^[A-Z0-9_-]+$/.test(raw) || isTechnicalIdentifier(raw)) return '答题思路需要复盘'
  return /[A-Za-z]/.test(raw) ? '答题思路需要复盘' : raw
}

export function displayQuestionSummary(_questionId?: string | null, stem?: string | null): string {
  const readableStem = stem?.trim()
  return readableStem || '这道题的题目内容暂未提供'
}

export function displayLearningStateStatus(value?: string | null): string {
  const labels: Record<string, string> = { UPDATED: '已更新', COMPLETED: '已完成', NOT_RECOMPUTED: '待更新' }
  return labels[value?.toUpperCase() ?? ''] ?? '已记录'
}

export function displayTransferValidation(value?: string | null): string {
  const labels: Record<string, string> = { PASS: '已通过', FAIL: '待复习', NOT_RUN: '未验证' }
  return labels[value?.toUpperCase() ?? ''] ?? '未验证'
}
