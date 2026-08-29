package com.zhijiao.foundation.demo;

import java.util.List;
import java.util.Map;

public record DemoRunTrace(
        String demoRunId,
        String demoCaseId,
        String baselineVersion,
        String correlationId,
        String stage,
        Map<String, String> refs,
        List<DemoTraceEvent> events
) {
}
