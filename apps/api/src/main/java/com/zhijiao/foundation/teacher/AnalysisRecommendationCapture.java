package com.zhijiao.foundation.teacher;

import java.time.Instant;
import java.util.List;

public record AnalysisRecommendationCapture(
        String studentId,
        String courseId,
        String classId,
        String knowledgePointId,
        String demoRunId,
        String demoCaseId,
        String correlationId,
        String analysisSummary,
        List<String> evidenceRefs,
        List<Candidate> candidates,
        String source,
        Instant generatedAt,
        String idempotencyKey
) {
    public record Candidate(
            String strategyCode,
            String title,
            String rationale,
            String actionDescription
    ) {
    }
}
