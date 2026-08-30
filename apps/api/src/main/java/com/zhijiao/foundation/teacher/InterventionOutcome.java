package com.zhijiao.foundation.teacher;

import java.time.Instant;

public record InterventionOutcome(
        String outcomeId,
        String interventionId,
        String assignmentId,
        String practiceSetId,
        String studentId,
        String courseId,
        String classId,
        String knowledgePointId,
        double predictedLift,
        double predictionLow,
        double predictionHigh,
        double masteryBefore,
        double confidenceBefore,
        double forgettingRiskBefore,
        Double weaknessScoreBefore,
        int evidenceCountBefore,
        double masteryAfter,
        double confidenceAfter,
        double forgettingRiskAfter,
        int evidenceCountAfter,
        double actualLift,
        double predictionDeviation,
        String transferValidation,
        double practiceAccuracyAfter,
        String dataOrigin,
        String demoRunId,
        String demoCaseId,
        String correlationId,
        String sourceVersion,
        Instant completedAt
) {
}
