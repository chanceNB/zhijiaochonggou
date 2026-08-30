package com.zhijiao.foundation.student.learning;

import java.time.LocalDate;
import java.util.List;

public record GrowthReadModel(
        String studentId,
        String courseId,
        double mastery,
        List<TrendPoint> trend,
        int completedTasks,
        int repairedMisconceptions,
        LatestIntervention latestIntervention
) {
    public record TrendPoint(LocalDate date, double mastery) {
    }

    public record LatestIntervention(
            String strategyCode,
            double masteryBefore,
            double masteryAfter,
            String transferValidation,
            Double predictedLift,
            Double actualLift,
            Double predictionDeviation,
            Double confidenceBefore,
            Double confidenceAfter,
            Double forgettingRiskBefore,
            Double forgettingRiskAfter,
            Integer evidenceCountBefore,
            Integer evidenceCountAfter
    ) {
        public LatestIntervention(String strategyCode, double masteryBefore, double masteryAfter,
                                  String transferValidation) {
            this(strategyCode, masteryBefore, masteryAfter, transferValidation, null, null, null,
                    null, null, null, null, null, null);
        }
    }
}
