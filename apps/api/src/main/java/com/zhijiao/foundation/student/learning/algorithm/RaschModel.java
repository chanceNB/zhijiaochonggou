package com.zhijiao.foundation.student.learning.algorithm;

import java.util.List;

public final class RaschModel {
    private final RaschParameters parameters;

    public RaschModel(RaschParameters parameters) {
        this.parameters = parameters;
    }

    public double itemParameter(double normalizedDifficulty) {
        double p = Math.max(parameters.difficultyEpsilon(),
                Math.min(1.0 - parameters.difficultyEpsilon(), normalizedDifficulty));
        return Math.log(p / (1.0 - p));
    }

    public RaschEstimate estimate(List<RaschObservation> observations) {
        double theta = parameters.priorMean();
        for (int iteration = 0; iteration < parameters.maxIterations(); iteration++) {
            double gradient = -(theta - parameters.priorMean()) / parameters.priorVariance();
            double information = 1.0 / parameters.priorVariance();
            for (RaschObservation observation : observations) {
                double probability = sigmoid(theta - itemParameter(observation.normalizedDifficulty()));
                gradient += (observation.correct() ? 1.0 : 0.0) - probability;
                information += probability * (1.0 - probability);
            }
            double step = gradient / information;
            theta += step;
            if (Math.abs(step) <= parameters.tolerance()) {
                break;
            }
        }
        double information = 1.0 / parameters.priorVariance();
        for (RaschObservation observation : observations) {
            double probability = sigmoid(theta - itemParameter(observation.normalizedDifficulty()));
            information += probability * (1.0 - probability);
        }
        return new RaschEstimate(theta, Math.sqrt(1.0 / information));
    }

    private double sigmoid(double value) {
        if (value >= 0.0) {
            double e = Math.exp(-value);
            return 1.0 / (1.0 + e);
        }
        double e = Math.exp(value);
        return e / (1.0 + e);
    }
}
