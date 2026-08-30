package com.zhijiao.foundation.teacher;

import java.time.Instant;

public record Intervention(
        String interventionId,
        String recommendationId,
        String studentId,
        String courseId,
        String classId,
        String knowledgePointId,
        String strategyCode,
        String teacherRationale,
        double predictedLift,
        double predictionLow,
        double predictionHigh,
        String status,
        int version,
        String assignmentId,
        String demoRunId,
        String demoCaseId,
        String correlationId,
        String sourceVersion,
        String idempotencyKey,
        String approveIdempotencyKey,
        String commitIdempotencyKey,
        Instant createdAt,
        Instant approvedAt,
        Instant committedAt
) {
}
