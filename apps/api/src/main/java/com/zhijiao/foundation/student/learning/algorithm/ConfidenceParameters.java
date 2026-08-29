package com.zhijiao.foundation.student.learning.algorithm;

public record ConfidenceParameters(
        double evidenceScale,
        double recencyWindowDays,
        double evidenceWeight,
        double uncertaintyWeight,
        double consistencyWeight,
        double recencyWeight,
        String modelVersion
) {
    public ConfidenceParameters {
        if (!Double.isFinite(evidenceScale) || evidenceScale <= 0.0
                || !Double.isFinite(recencyWindowDays) || recencyWindowDays <= 0.0) {
            throw new IllegalArgumentException("Confidence scales must be positive");
        }
        requireWeight(evidenceWeight, "evidenceWeight");
        requireWeight(uncertaintyWeight, "uncertaintyWeight");
        requireWeight(consistencyWeight, "consistencyWeight");
        requireWeight(recencyWeight, "recencyWeight");
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
