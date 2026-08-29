package com.zhijiao.foundation.student.learning.algorithm;

public record BktParameters(
        double initialMastery,
        double transition,
        double slip,
        double guess,
        String modelVersion
) {
    public BktParameters {
        requireProbability(initialMastery, "initialMastery");
        requireProbability(transition, "transition");
        requireProbability(slip, "slip");
        requireProbability(guess, "guess");
        if (modelVersion == null || modelVersion.isBlank()) {
            throw new IllegalArgumentException("modelVersion must not be blank");
        }
    }

    private static void requireProbability(double value, String name) {
        if (!Double.isFinite(value) || value < 0.0 || value > 1.0) {
            throw new IllegalArgumentException(name + " must be in [0,1]");
        }
    }
}
