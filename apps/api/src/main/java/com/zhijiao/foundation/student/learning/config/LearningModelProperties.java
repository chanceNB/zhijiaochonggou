package com.zhijiao.foundation.student.learning.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "app.learning")
public class LearningModelProperties {
    private final Bkt bkt = new Bkt();
    private final Rasch rasch = new Rasch();
    private final Forgetting forgetting = new Forgetting();
    private final Confidence confidence = new Confidence();

    public Bkt getBkt() {
        return bkt;
    }

    public Rasch getRasch() {
        return rasch;
    }

    public Forgetting getForgetting() {
        return forgetting;
    }

    public Confidence getConfidence() {
        return confidence;
    }

    public static class Bkt {
        private double initialMastery = 0.20;
        private double transition = 0.15;
        private double slip = 0.10;
        private double guess = 0.20;
        private String modelVersion = "BKT_V1_FIXED_PARAMS";

        public double getInitialMastery() { return initialMastery; }
        public void setInitialMastery(double value) { initialMastery = value; }
        public double getTransition() { return transition; }
        public void setTransition(double value) { transition = value; }
        public double getSlip() { return slip; }
        public void setSlip(double value) { slip = value; }
        public double getGuess() { return guess; }
        public void setGuess(double value) { guess = value; }
        public String getModelVersion() { return modelVersion; }
        public void setModelVersion(String value) { modelVersion = value; }
    }

    public static class Rasch {
        private double priorMean = 0.0;
        private double priorVariance = 1.0;
        private double difficultyEpsilon = 1.0e-6;
        private int maxIterations = 100;
        private double tolerance = 1.0e-10;
        private String modelVersion = "RASCH_MAP_V1";

        public double getPriorMean() { return priorMean; }
        public void setPriorMean(double value) { priorMean = value; }
        public double getPriorVariance() { return priorVariance; }
        public void setPriorVariance(double value) { priorVariance = value; }
        public double getDifficultyEpsilon() { return difficultyEpsilon; }
        public void setDifficultyEpsilon(double value) { difficultyEpsilon = value; }
        public int getMaxIterations() { return maxIterations; }
        public void setMaxIterations(int value) { maxIterations = value; }
        public double getTolerance() { return tolerance; }
        public void setTolerance(double value) { tolerance = value; }
        public String getModelVersion() { return modelVersion; }
        public void setModelVersion(String value) { modelVersion = value; }
    }

    public static class Forgetting {
        private double windowDays = 30.0;
        private double densityTarget = 5.0;
        private double recencyWeight = 0.50;
        private double gapWeight = 0.20;
        private double sparsityWeight = 0.20;
        private double masteryWeight = 0.10;
        private String modelVersion = "RECENCY_GAP_V1";

        public double getWindowDays() { return windowDays; }
        public void setWindowDays(double value) { windowDays = value; }
        public double getDensityTarget() { return densityTarget; }
        public void setDensityTarget(double value) { densityTarget = value; }
        public double getRecencyWeight() { return recencyWeight; }
        public void setRecencyWeight(double value) { recencyWeight = value; }
        public double getGapWeight() { return gapWeight; }
        public void setGapWeight(double value) { gapWeight = value; }
        public double getSparsityWeight() { return sparsityWeight; }
        public void setSparsityWeight(double value) { sparsityWeight = value; }
        public double getMasteryWeight() { return masteryWeight; }
        public void setMasteryWeight(double value) { masteryWeight = value; }
        public String getModelVersion() { return modelVersion; }
        public void setModelVersion(String value) { modelVersion = value; }
    }

    public static class Confidence {
        private double evidenceScale = 3.0;
        private double recencyWindowDays = 30.0;
        private double evidenceWeight = 0.40;
        private double uncertaintyWeight = 0.25;
        private double consistencyWeight = 0.20;
        private double recencyWeight = 0.15;
        private String modelVersion = "STATE_CONFIDENCE_V1";

        public double getEvidenceScale() { return evidenceScale; }
        public void setEvidenceScale(double value) { evidenceScale = value; }
        public double getRecencyWindowDays() { return recencyWindowDays; }
        public void setRecencyWindowDays(double value) { recencyWindowDays = value; }
        public double getEvidenceWeight() { return evidenceWeight; }
        public void setEvidenceWeight(double value) { evidenceWeight = value; }
        public double getUncertaintyWeight() { return uncertaintyWeight; }
        public void setUncertaintyWeight(double value) { uncertaintyWeight = value; }
        public double getConsistencyWeight() { return consistencyWeight; }
        public void setConsistencyWeight(double value) { consistencyWeight = value; }
        public double getRecencyWeight() { return recencyWeight; }
        public void setRecencyWeight(double value) { recencyWeight = value; }
        public String getModelVersion() { return modelVersion; }
        public void setModelVersion(String value) { modelVersion = value; }
    }
}
