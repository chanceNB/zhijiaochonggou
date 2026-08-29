package com.zhijiao.foundation.knowledge;

public record Citation(
        String documentId,
        String chunkId,
        String title,
        String excerpt,
        double score
) {
}
