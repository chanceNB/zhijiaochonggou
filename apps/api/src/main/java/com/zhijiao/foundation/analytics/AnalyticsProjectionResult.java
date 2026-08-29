package com.zhijiao.foundation.analytics;

import java.time.Instant;

public record AnalyticsProjectionResult(
        int dimensionRows,
        int learningStateRows,
        int practiceAttemptRows,
        int wrongBookRows,
        Instant observedAt
) {
}
