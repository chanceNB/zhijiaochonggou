package com.zhijiao.foundation.analytics;

public class AnalyticsExportNotFoundException extends RuntimeException {
    public AnalyticsExportNotFoundException(String exportId) {
        super("Analytics export not found: " + exportId);
    }
}
