package com.zhijiao.foundation.student.learning.algorithm;

public record ForgettingParameters(
        double windowDays,
        double densityTarget,
        double recencyWeight,
        double gapWeight,
        double sparsityWeight,
        double masteryWeight,
        String modelVersion
) {
    public ForgettingParameters {
        if (!Double.isFinite(windowDays) || windowDays <= 0.0
                || !Double.isFinite(densityTarget) || densityTarget <= 0.0) {
            throw new IllegalArgumentException("Forgetting windows must be positive");
        }
        requireWeight(recencyWeight, "recencyWeight");
        requireWeight(gapWeight, "gapWeight");
        requireWeight(sparsityWeight, "sparsityWeight");
        requireWeight(masteryWeight, "masteryWeight");
        if (modelVersion == null || modelVersion.isBlank()) {
            throw new IllegalArgumentException("modelVersion must not be blank");
        }
    }

    private static void requireWeight(double value, String name) {
        if (!Double.isFinite(value) || value < 0.0 || value > 1.0) {
            throw new IllegalArgumentException(name + " must be in [0,1]");
        }
    }
}
