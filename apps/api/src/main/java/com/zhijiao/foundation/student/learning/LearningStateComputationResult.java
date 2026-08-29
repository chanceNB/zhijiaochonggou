package com.zhijiao.foundation.student.learning;

public record LearningStateComputationResult(
        String baselineVersion,
        int abilityCount,
        int knowledgeStateCount,
        int candidateCount
) {
}
