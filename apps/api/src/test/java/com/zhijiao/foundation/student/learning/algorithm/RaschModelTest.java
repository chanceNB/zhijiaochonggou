package com.zhijiao.foundation.student.learning.algorithm;

import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class RaschModelTest {

    private final RaschModel model = new RaschModel(new RaschParameters(
            0.0, 1.0, 1.0e-6, 100, 1.0e-10, "RASCH_MAP_V1"));

    @Test
    void normalizedDifficultyUsesProtectedLogitTransform() {
        assertThat(model.itemParameter(0.5)).isCloseTo(0.0, within(1.0e-12));
        assertThat(model.itemParameter(0.75)).isGreaterThan(model.itemParameter(0.25));
    }

    @Test
    void allCorrectAndAllWrongMapEstimatesRemainFinite() {
        RaschEstimate allCorrect = model.estimate(List.of(
                new RaschObservation(true, 0.25),
                new RaschObservation(true, 0.50),
                new RaschObservation(true, 0.75)));
        RaschEstimate allWrong = model.estimate(List.of(
                new RaschObservation(false, 0.25),
                new RaschObservation(false, 0.50),
                new RaschObservation(false, 0.75)));

        assertThat(allCorrect.theta()).isFinite();
        assertThat(allWrong.theta()).isFinite();
        assertThat(allCorrect.standardError()).isFinite().isPositive();
        assertThat(allWrong.standardError()).isFinite().isPositive();
    }

    @Test
    void higherPerformanceProducesHigherThetaDeterministically() {
        List<RaschObservation> stronger = List.of(
                new RaschObservation(true, 0.25),
                new RaschObservation(true, 0.50),
                new RaschObservation(true, 0.75));
        List<RaschObservation> weaker = List.of(
                new RaschObservation(false, 0.25),
                new RaschObservation(false, 0.50),
                new RaschObservation(false, 0.75));

        RaschEstimate first = model.estimate(stronger);
        RaschEstimate second = model.estimate(stronger);

        assertThat(first.theta()).isGreaterThan(model.estimate(weaker).theta());
        assertThat(first.theta()).isEqualTo(second.theta());
        assertThat(first.standardError()).isEqualTo(second.standardError());
    }

    private static org.assertj.core.data.Offset<Double> within(double value) {
        return org.assertj.core.data.Offset.offset(value);
    }
}
