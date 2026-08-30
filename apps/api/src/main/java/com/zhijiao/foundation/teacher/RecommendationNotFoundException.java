package com.zhijiao.foundation.teacher;

public class RecommendationNotFoundException extends RuntimeException {
    public RecommendationNotFoundException(String recommendationId) {
        super("Analysis recommendation not found: " + recommendationId);
    }
}
