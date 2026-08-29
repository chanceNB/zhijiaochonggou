package com.zhijiao.foundation.student.learning.algorithm;

import org.junit.jupiter.api.Test;

import java.time.Duration;
import java.time.Instant;

import static org.assertj.core.api.Assertions.assertThat;

class ConfidenceModelTest {

    private final ConfidenceModel model = new ConfidenceModel(
            new ConfidenceParameters(3.0, 30.0, 0.40, 0.25, 0.20, 0.15, "STATE_CONFIDENCE_V1"));
    private final Instant reference = Instant.parse("2026-08-29T00:00:00Z");

    @Test
    void sufficientRecentConsistentEvidenceHasHigherConfidence() {
        double low = model.calculate(new ConfidenceEvidence(1, 1.8, 0.5,
                reference.minus(Duration.ofDays(25)), reference));
        double high = model.calculate(new ConfidenceEvidence(8, 0.35, 1.0,
                reference.minus(Duration.ofDays(1)), reference));

        assertThat(high).isGreaterThan(low);
    }

    @Test
    void conflictingEvidenceLowersConfidenceAndResultIsBounded() {
        double conflicting = model.calculate(new ConfidenceEvidence(8, 0.35, 0.0,
                reference.minus(Duration.ofDays(1)), reference));

        assertThat(conflicting).isBetween(0.0, 1.0);
        assertThat(conflicting).isLessThan(model.calculate(new ConfidenceEvidence(8, 0.35, 1.0,
                reference.minus(Duration.ofDays(1)), reference)));
    }
}
