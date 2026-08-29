package com.zhijiao.foundation.student.practice;

import java.util.List;

/** Student-facing question projection. Answer keys stay in InternalQuestion only. */
public record StudentQuestion(
        String questionId,
        String knowledgePointId,
        String questionType,
        String stem,
        List<QuestionOptionView> options,
        double difficulty
) {
    public static StudentQuestion from(InternalQuestion question) {
        return new StudentQuestion(question.questionId(), question.knowledgePointId(), question.questionType(),
                question.stem(), List.copyOf(question.options()), question.difficulty());
    }
}
