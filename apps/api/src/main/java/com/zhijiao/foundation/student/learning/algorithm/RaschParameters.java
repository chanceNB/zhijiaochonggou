package com.zhijiao.foundation.student.learning.algorithm;

public record RaschParameters(
        double priorMean,
        double priorVariance,
        double difficultyEpsilon,
        int maxIterations,
        double tolerance,
        String modelVersion
) {
    public RaschParameters {
        if (!Double.isFinite(priorMean) || !Double.isFinite(priorVariance) || priorVariance <= 0.0) {
            throw new IllegalArgumentException("Rasch prior must be finite and have positive variance");
        }
        if (!Double.isFinite(difficultyEpsilon) || difficultyEpsilon <= 0.0 || difficultyEpsilon >= 0.5) {
            throw new IllegalArgumentException("difficultyEpsilon must be in (0,0.5)");
        }
        if (maxIterations < 1 || !Double.isFinite(tolerance) || tolerance <= 0.0) {
            throw new IllegalArgumentException("Invalid Rasch solver settings");
        }
        if (modelVersion == null || modelVersion.isBlank()) {
            throw new IllegalArgumentException("modelVersion must not be blank");
        }
    }
}
