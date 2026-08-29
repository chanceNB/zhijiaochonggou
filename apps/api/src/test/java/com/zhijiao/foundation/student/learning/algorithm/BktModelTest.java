package com.zhijiao.foundation.student.learning.algorithm;

import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class BktModelTest {

    private final BktParameters parameters = new BktParameters(
            0.2, 0.15, 0.1, 0.2, "BKT_V1_FIXED_PARAMS");

    @Test
    void repeatedCorrectAnswersRaisePosteriorMoreThanRepeatedErrors() {
        BktModel model = new BktModel(parameters);

        double correct = model.posterior(List.of(true, true, true));
        double errors = model.posterior(List.of(false, false, false));

        assertThat(correct).isGreaterThan(errors);
    }

    @Test
    void posteriorIsBoundedAndDeterministic() {
        BktModel model = new BktModel(parameters);

        double first = model.posterior(List.of(true, false, true, false));
        double second = model.posterior(List.of(true, false, true, false));

        assertThat(first).isBetween(0.0, 1.0);
        assertThat(first).isEqualTo(second);
    }
}
