package com.zhijiao.foundation.student.learning;

import java.time.Instant;

public record PracticeObservation(
        String attemptId,
        String studentId,
        String courseId,
        String classId,
        String knowledgePointId,
        String questionId,
        boolean correct,
        int responseTimeMs,
        int attemptIndex,
        Instant attemptTime,
        double itemDifficulty
) {
}
