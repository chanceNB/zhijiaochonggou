package com.zhijiao.foundation.student.learning.algorithm;

import org.junit.jupiter.api.Test;

import java.time.Duration;
import java.time.Instant;

import static org.assertj.core.api.Assertions.assertThat;

class ForgettingRiskModelTest {

    private final ForgettingRiskModel model = new ForgettingRiskModel(
            new ForgettingParameters(30.0, 5.0, 0.50, 0.20, 0.20, 0.10, "RECENCY_GAP_V1"));
    private final Instant reference = Instant.parse("2026-08-29T00:00:00Z");

    @Test
    void recentEvidenceHasLowerRiskThanStaleEvidence() {
        double recent = model.calculate(new ForgettingEvidence(
                reference, reference.minus(Duration.ofDays(1)), Duration.ofDays(1), 4, 0.7));
        double stale = model.calculate(new ForgettingEvidence(
                reference, reference.minus(Duration.ofDays(25)), Duration.ofDays(20), 4, 0.7));

        assertThat(recent).isLessThan(stale);
    }

    @Test
    void denseEvidenceHasLowerRiskThanSparseEvidenceAtSameRecency() {
        double dense = model.calculate(new ForgettingEvidence(
                reference, reference.minus(Duration.ofDays(10)), Duration.ofDays(2), 5, 0.7));
        double sparse = model.calculate(new ForgettingEvidence(
                reference, reference.minus(Duration.ofDays(10)), Duration.ofDays(10), 1, 0.7));

        assertThat(dense).isLessThan(sparse);
        assertThat(dense).isBetween(0.0, 1.0);
        assertThat(sparse).isBetween(0.0, 1.0);
    }
}
