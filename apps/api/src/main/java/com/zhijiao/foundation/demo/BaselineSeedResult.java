package com.zhijiao.foundation.demo;

public record BaselineSeedResult(
        String baselineVersion,
        int courseCount,
        int classCount,
        int studentCount,
        int knowledgePointCount,
        int learningEventCount,
        int practiceAttemptCount
) {
}
