package com.zhijiao.foundation.student.practice;

import java.time.Instant;

public record WrongBookItem(String wrongItemId, String studentId, String courseId, String classId,
                            String questionId, String sourceAttemptId, String knowledgePointId,
                            String reason, String status, int reviewCount, Instant addedAt,
                            Instant repairedAt, String dataOrigin, String demoRunId,
                            String demoCaseId, String correlationId, String sourceVersion) {
}
