package com.zhijiao.foundation.student.learning;

import java.util.List;

public record LearningStateView(
        StudentKnowledgeState state,
        StudentAbilityState ability,
        List<WeakKnowledgePointCandidate> weakKnowledgePoints
) {
}
