package com.zhijiao.foundation.student.learning.algorithm;

import java.util.List;

public final class BktModel {
    private final BktParameters parameters;

    public BktModel(BktParameters parameters) {
        this.parameters = parameters;
    }

    public double posterior(List<Boolean> observations) {
        double mastery = parameters.initialMastery();
        for (Boolean observation : observations) {
            if (observation == null) {
                continue;
            }
            double correctProbability = mastery * (1.0 - parameters.slip())
                    + (1.0 - mastery) * parameters.guess();
            double denominator = observation ? correctProbability : 1.0 - correctProbability;
            double numerator = observation
                    ? mastery * (1.0 - parameters.slip())
                    : mastery * parameters.slip();
            double posterior = denominator <= 0.0 ? mastery : numerator / denominator;
            mastery = posterior + (1.0 - posterior) * parameters.transition();
        }
        return clamp(mastery);
    }

    private double clamp(double value) {
        return Math.max(0.0, Math.min(1.0, value));
    }
}
