package com.zhijiao.foundation.knowledge;

import java.time.Instant;
import java.util.List;

public record KnowledgeChunk(
        String chunkId,
        String documentId,
        String courseId,
        String knowledgePointId,
        String title,
        String content,
        int chunkIndex,
        List<Double> embedding,
        List<String> metadata,
        String sourceVersion,
        Instant createdAt
) {
}
