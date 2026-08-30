package com.zhijiao.foundation.student.practice;

public record PracticeOutcome(String outcomeId, String practiceSetId, double accuracy, int attemptCount,
                              String learningStateStatus, String transferValidation,
                              LearningStateAfter learningStateAfter, String interventionOutcomeId) {
    public PracticeOutcome(String outcomeId, String practiceSetId, double accuracy, int attemptCount,
                           String learningStateStatus) {
        this(outcomeId, practiceSetId, accuracy, attemptCount, learningStateStatus, null, null, null);
    }

    public record LearningStateAfter(double mastery, double confidence, double forgettingRisk, int evidenceCount) {
    }
}
