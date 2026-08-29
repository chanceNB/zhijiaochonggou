package com.zhijiao.foundation.knowledge;

import java.time.Instant;

public record KnowledgeDocument(
        String documentId,
        String courseId,
        String title,
        String source,
        String status,
        String dataOrigin,
        String sourceVersion,
        Instant createdAt,
        Instant indexedAt
) {
}
