package com.zhijiao.foundation.analytics;

import java.time.Instant;

public record AnalyticsFreshness(
        String datasetKey,
        Instant latestSourceEventTime,
        Instant latestProjectionTime,
        Instant observedAt,
        long rowCount,
        String sourceVersion
) {
}
