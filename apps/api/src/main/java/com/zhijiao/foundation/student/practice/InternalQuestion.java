package com.zhijiao.foundation.student.practice;

import java.time.Instant;
import java.util.List;

public record InternalQuestion(
        String questionId,
        String practiceSetId,
        String source,
        String parentQuestionId,
        String knowledgePointId,
        String questionType,
        String stem,
        List<QuestionOptionView> options,
        String correctAnswer,
        String explanation,
        double difficulty,
        Instant createdAt
) {
}
