package com.zhijiao.foundation.student.practice;

import java.time.Instant;
import java.util.List;

public record WrongBookItem(String wrongItemId, String studentId, String courseId, String classId,
                            String questionId, String sourceAttemptId, String knowledgePointId,
                            String reason, String status, int reviewCount, Instant addedAt,
                            Instant repairedAt, String dataOrigin, String demoRunId,
                            String demoCaseId, String correlationId, String sourceVersion,
                            String knowledgePointName, String questionSummary, String reasonDisplayName,
                            String questionType, List<QuestionOptionView> options) {
    public WrongBookItem {
        options = options == null ? List.of() : List.copyOf(options);
    }

    public WrongBookItem(String wrongItemId, String studentId, String courseId, String classId,
                         String questionId, String sourceAttemptId, String knowledgePointId,
                         String reason, String status, int reviewCount, Instant addedAt,
                         Instant repairedAt, String dataOrigin, String demoRunId,
                         String demoCaseId, String correlationId, String sourceVersion) {
        this(wrongItemId, studentId, courseId, classId, questionId, sourceAttemptId, knowledgePointId,
                reason, status, reviewCount, addedAt, repairedAt, dataOrigin, demoRunId,
                demoCaseId, correlationId, sourceVersion, null, null, null, null, List.of());
    }

    public WrongBookItem(String wrongItemId, String studentId, String courseId, String classId,
                         String questionId, String sourceAttemptId, String knowledgePointId,
                         String reason, String status, int reviewCount, Instant addedAt,
                         Instant repairedAt, String dataOrigin, String demoRunId,
                         String demoCaseId, String correlationId, String sourceVersion,
                         String knowledgePointName, String questionSummary, String reasonDisplayName) {
        this(wrongItemId, studentId, courseId, classId, questionId, sourceAttemptId, knowledgePointId,
                reason, status, reviewCount, addedAt, repairedAt, dataOrigin, demoRunId,
                demoCaseId, correlationId, sourceVersion, knowledgePointName, questionSummary,
                reasonDisplayName, null, List.of());
    }
}
