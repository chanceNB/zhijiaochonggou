package com.zhijiao.foundation.teacher;

import java.time.Instant;

public record InterventionAssignment(
        String assignmentId,
        String interventionId,
        String practiceSetId,
        String studentId,
        String courseId,
        String classId,
        String knowledgePointId,
        String status,
        Instant dueAt,
        Instant createdAt
) {
}
