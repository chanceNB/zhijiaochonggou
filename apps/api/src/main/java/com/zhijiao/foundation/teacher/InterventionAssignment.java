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
        Instant createdAt,
        String demoRunId,
        String demoCaseId,
        String correlationId,
        String sourceVersion
) {
    public InterventionAssignment(String assignmentId, String interventionId, String practiceSetId,
                                  String studentId, String courseId, String classId, String knowledgePointId,
                                  String status, Instant dueAt, Instant createdAt) {
        this(assignmentId, interventionId, practiceSetId, studentId, courseId, classId, knowledgePointId,
                status, dueAt, createdAt, null, null, null, null);
    }
}
