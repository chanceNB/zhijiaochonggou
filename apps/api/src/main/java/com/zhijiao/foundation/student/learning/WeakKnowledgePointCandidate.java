package com.zhijiao.foundation.student.learning;

import java.util.List;

public record WeakKnowledgePointCandidate(
        String studentId,
        String courseId,
        String knowledgePointId,
        String knowledgePointName,
        double weaknessScore,
        double confidence,
        int evidenceCount,
        int rankPosition,
        List<String> reasonCodes,
        String modelVersion
) {
}
