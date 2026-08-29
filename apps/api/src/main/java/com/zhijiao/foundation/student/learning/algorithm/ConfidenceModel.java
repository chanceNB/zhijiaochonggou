package com.zhijiao.foundation.student.learning.algorithm;

import java.time.Duration;

public final class ConfidenceModel {
    private final ConfidenceParameters parameters;

    public ConfidenceModel(ConfidenceParameters parameters) {
        this.parameters = parameters;
    }

    public double calculate(ConfidenceEvidence evidence) {
        double evidenceFactor = 1.0 - Math.exp(-Math.max(0, evidence.evidenceCount())
                / parameters.evidenceScale());
        double uncertaintyFactor = 1.0 / (1.0 + Math.max(0.0, evidence.thetaStandardError()));
        double consistencyFactor = clamp(evidence.observationConsistency());
        double staleDays = Math.max(0.0,
                Duration.between(evidence.lastEvidenceAt(), evidence.referenceTime()).toSeconds() / 86400.0);
        double recencyFactor = Math.exp(-staleDays / parameters.recencyWindowDays());
        return clamp(parameters.evidenceWeight() * evidenceFactor
                + parameters.uncertaintyWeight() * uncertaintyFactor
                + parameters.consistencyWeight() * consistencyFactor
                + parameters.recencyWeight() * recencyFactor);
    }

    private double clamp(double value) {
        return Math.max(0.0, Math.min(1.0, value));
    }
}
