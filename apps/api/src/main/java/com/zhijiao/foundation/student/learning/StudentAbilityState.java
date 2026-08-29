package com.zhijiao.foundation.student.learning;

import java.time.Instant;

public record StudentAbilityState(
        String studentId,
        String courseId,
        double theta,
        double thetaUncertainty,
        String abilityModelVersion,
        Instant computedAt,
        String sourceVersion
) {
}
