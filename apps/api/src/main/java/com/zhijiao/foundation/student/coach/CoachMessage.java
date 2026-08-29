package com.zhijiao.foundation.student.coach;

import java.time.Instant;
import java.util.List;

public record CoachMessage(
        String messageId,
        String sessionId,
        String messageType,
        String content,
        String modelProvider,
        String modelVersion,
        String promptVersion,
        RagStatus ragStatus,
        Instant createdAt,
        List<com.zhijiao.foundation.knowledge.Citation> citations
) {
}
