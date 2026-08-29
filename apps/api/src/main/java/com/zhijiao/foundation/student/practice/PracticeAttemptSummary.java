package com.zhijiao.foundation.student.practice;

import java.time.Instant;

public record PracticeAttemptSummary(String attemptId, String questionId, String selectedAnswer,
                                     boolean correct, int responseTimeMs, Instant attemptTime) {
}
