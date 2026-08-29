package com.zhijiao.foundation.knowledge;

import java.util.List;

public record KnowledgeSearchResult(
        String chunkId,
        String documentId,
        String title,
        String contentExcerpt,
        double score,
        List<String> metadata
) {
    public Citation toCitation() {
        return new Citation(documentId, chunkId, title, contentExcerpt, score);
    }
}
