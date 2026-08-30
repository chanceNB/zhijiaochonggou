package com.zhijiao.foundation.teacher;

import java.time.Instant;
import java.util.List;

/** Immutable snapshot of the recommendation shown by SmartBI AIChat. */
public record AnalysisRecommendation(
        String recommendationId,
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
        String captureMode,
        String status,
        Instant generatedAt,
        Instant capturedAt,
        String sourceVersion
) {
    public AnalysisRecommendation {
        candidates = candidates == null ? List.of() : List.copyOf(candidates);
        evidenceRefs = evidenceRefs == null ? List.of() : List.copyOf(evidenceRefs);
    }

    public record Candidate(
            int candidateIndex,
            String strategyCode,
            String title,
            String rationale,
            String actionDescription,
            String sourceSnapshot
    ) {
    }
}
