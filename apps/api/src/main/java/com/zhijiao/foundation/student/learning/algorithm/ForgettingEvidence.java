package com.zhijiao.foundation.student.learning.algorithm;

import java.time.Duration;
import java.time.Instant;

public record ForgettingEvidence(
        Instant referenceTime,
        Instant lastEvidenceAt,
        Duration lastPracticeGap,
        int evidenceCount,
        double masteryProbability
) {
}
