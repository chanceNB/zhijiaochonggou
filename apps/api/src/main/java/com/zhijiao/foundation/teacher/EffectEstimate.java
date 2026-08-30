package com.zhijiao.foundation.teacher;

public record EffectEstimate(double predictedLift, double predictionLow, double predictionHigh) {
    public EffectEstimate {
        if (!Double.isFinite(predictedLift) || !Double.isFinite(predictionLow) || !Double.isFinite(predictionHigh)
                || predictionLow < 0 || predictedLift < predictionLow || predictedLift > predictionHigh
                || predictionHigh > 1) {
            throw new IllegalArgumentException("Effect estimate must be ordered and bounded between 0 and 1");
        }
    }
}
