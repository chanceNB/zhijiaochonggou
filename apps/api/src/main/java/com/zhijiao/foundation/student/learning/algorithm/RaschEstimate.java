package com.zhijiao.foundation.student.learning.algorithm;

public record RaschEstimate(double theta, double standardError) {
    public RaschEstimate {
        if (!Double.isFinite(theta) || !Double.isFinite(standardError) || standardError <= 0.0) {
            throw new IllegalArgumentException("Rasch estimate must be finite");
        }
    }
}
