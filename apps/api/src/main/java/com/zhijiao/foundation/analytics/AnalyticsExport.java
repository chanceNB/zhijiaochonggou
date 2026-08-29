package com.zhijiao.foundation.analytics;

import java.time.Instant;
import java.util.List;

public record AnalyticsExport(
        String exportId,
        String status,
        String scope,
        String demoRunId,
        Instant createdAt,
        Instant completedAt,
        List<String> files,
        String manifestPath
) {
}
