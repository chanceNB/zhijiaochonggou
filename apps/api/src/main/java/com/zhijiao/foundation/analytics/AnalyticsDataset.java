package com.zhijiao.foundation.analytics;

public record AnalyticsDataset(
        String datasetKey,
        String objectName,
        String kind,
        String grain,
        long rowCount,
        String sourceVersion,
        String name,
        long rows
) {
}
