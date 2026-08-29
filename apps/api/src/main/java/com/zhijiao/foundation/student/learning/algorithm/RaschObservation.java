package com.zhijiao.foundation.student.learning.algorithm;

public record RaschObservation(boolean correct, double normalizedDifficulty) {
    public RaschObservation {
        if (!Double.isFinite(normalizedDifficulty) || normalizedDifficulty < 0.0 || normalizedDifficulty > 1.0) {
            throw new IllegalArgumentException("normalizedDifficulty must be in [0,1]");
        }
    }
}
