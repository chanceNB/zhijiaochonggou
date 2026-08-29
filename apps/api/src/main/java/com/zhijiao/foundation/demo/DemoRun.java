package com.zhijiao.foundation.demo;

import java.time.Instant;

public record DemoRun(
        String demoRunId,
        String demoCaseId,
        String baselineVersion,
        String studentId,
        String courseId,
        String classId,
        String status,
        String stage,
        String correlationId,
        Instant createdAt,
        Instant resetAt,
        String resetFromDemoRunId
) {
}
