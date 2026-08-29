package com.zhijiao.foundation.student.learning.algorithm;

import java.time.Duration;

public final class ForgettingRiskModel {
    private final ForgettingParameters parameters;

    public ForgettingRiskModel(ForgettingParameters parameters) {
        this.parameters = parameters;
    }

    public double calculate(ForgettingEvidence evidence) {
        double recencyDays = Math.max(0.0,
                Duration.between(evidence.lastEvidenceAt(), evidence.referenceTime()).toSeconds() / 86400.0);
        double gapDays = evidence.lastPracticeGap() == null ? parameters.windowDays()
                : Math.max(0.0, evidence.lastPracticeGap().toSeconds() / 86400.0);
        double recency = clamp(recencyDays / parameters.windowDays());
        double gap = clamp(gapDays / parameters.windowDays());
        double sparsity = 1.0 - clamp(evidence.evidenceCount() / parameters.densityTarget());
        double lowMastery = 1.0 - clamp(evidence.masteryProbability());
        return clamp(parameters.recencyWeight() * recency
                + parameters.gapWeight() * gap
                + parameters.sparsityWeight() * sparsity
                + parameters.masteryWeight() * lowMastery);
    }

    private double clamp(double value) {
        return Math.max(0.0, Math.min(1.0, value));
    }
}
