package com.zhijiao.foundation.student.learning.algorithm;

import java.time.Instant;

public record ConfidenceEvidence(
        int evidenceCount,
        double thetaStandardError,
        double observationConsistency,
        Instant lastEvidenceAt,
        Instant referenceTime
) {
}
