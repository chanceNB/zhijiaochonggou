package com.zhijiao.foundation.knowledge;

import java.util.List;

public interface KnowledgeQueryPort {
    List<KnowledgeSearchResult> search(String courseId, String knowledgePointId, String query, int topK);
}
