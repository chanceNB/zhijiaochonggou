package com.zhijiao.foundation.student.learning;

import java.time.Instant;

public record StudentKnowledgeState(
        String studentId,
        String courseId,
        String classId,
        String knowledgePointId,
        String knowledgePointName,
        double masteryProbability,
        double confidence,
        double forgettingRisk,
        int evidenceCount,
        Instant lastEvidenceAt,
        String masteryModelVersion,
        String abilityModelVersion,
        String forgettingModelVersion,
        String confidenceModelVersion,
        Instant computedAt,
        String sourceVersion
) {
}
