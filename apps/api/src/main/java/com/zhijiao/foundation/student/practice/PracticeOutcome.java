package com.zhijiao.foundation.student.practice;

public record PracticeOutcome(String outcomeId, String practiceSetId, double accuracy, int attemptCount,
                              String learningStateStatus) {
}
