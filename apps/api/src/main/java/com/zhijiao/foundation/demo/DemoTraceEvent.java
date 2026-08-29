package com.zhijiao.foundation.demo;

import java.time.Instant;

public record DemoTraceEvent(String stage, String ref, Instant eventTime) {
}
