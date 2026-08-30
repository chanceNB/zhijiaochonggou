package com.zhijiao.foundation.teacher;

public interface EffectEstimator {
    EffectEstimate estimate(AnalysisRecommendation recommendation, AnalysisRecommendation.Candidate candidate);
}
