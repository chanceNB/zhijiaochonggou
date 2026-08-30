import type {
  CoachMessageResponseDto,
  CoachMessageVm,
  CoachSessionResponseDto,
  CoachSessionVm,
} from '@/types/contracts/student'

const localMessageId = () => {
  const random = typeof crypto !== 'undefined' && 'randomUUID' in crypto ? crypto.randomUUID() : `${Date.now()}-${Math.random()}`
  return `ui-message-${random}`
}

export function toCoachSessionVm(dto: CoachSessionResponseDto): CoachSessionVm {
  return {
    sessionId: dto.sessionId,
    studentId: dto.studentId,
    courseId: dto.courseId,
    knowledgePointId: dto.knowledgePointId ?? null,
    mode: dto.mode,
    status: dto.status,
    ragStatus: dto.ragStatus,
    context: dto.context,
    messages: dto.messages.map((message) => ({
      id: message.messageId ?? localMessageId(),
      role: message.messageType === 'USER' ? 'USER' : 'ASSISTANT',
      content: message.content,
      citations: message.citations,
      ragStatus: message.ragStatus,
    })),
  }
}

export function toAssistantMessageVm(dto: CoachMessageResponseDto): CoachMessageVm {
  return {
    id: localMessageId(),
    role: 'ASSISTANT',
    content: dto.assistantMessage,
    citations: dto.citations,
    ragStatus: dto.ragStatus,
  }
}

export { localMessageId }
