package com.zhijiao.foundation.student.practice;

import java.time.Instant;

public record PracticeSet(
        String practiceSetId,
        String studentId,
        String courseId,
        String classId,
        String coachSessionId,
        String source,
        String status,
        String demoRunId,
        String demoCaseId,
        String correlationId,
        String sourceVersion,
        Instant createdAt,
        Instant completedAt
) {
}
