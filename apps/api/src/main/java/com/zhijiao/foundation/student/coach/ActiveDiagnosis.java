package com.zhijiao.foundation.student.coach;

import java.util.List;

public record ActiveDiagnosis(String knowledgePointId, String target, List<String> reasonCodes,
                              double mastery, double confidence, double weaknessScore) {
}
