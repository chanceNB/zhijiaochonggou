package com.zhijiao.foundation.student.coach;

import java.time.Instant;

public record CoachSession(
        String sessionId,
        String studentId,
        String courseId,
        String knowledgePointId,
        String mode,
        String status,
        RagStatus ragStatus,
        double mastery,
        double confidence,
        double forgettingRisk,
        double weaknessScore,
        String reasonCodes,
        String learningModelVersion,
        String sourceVersion,
        Instant createdAt,
        Instant updatedAt
) {
}
