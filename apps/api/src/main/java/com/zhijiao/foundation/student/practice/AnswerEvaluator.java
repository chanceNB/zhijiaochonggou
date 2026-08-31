package com.zhijiao.foundation.student.practice;

import java.util.List;
import java.util.Objects;

/** Deterministic answer semantics shared by practice submission and wrong-book review. */
public final class AnswerEvaluator {
    private AnswerEvaluator() {
    }

    public static boolean evaluate(InternalQuestion question, String submittedAnswer) {
        if (question == null || submittedAnswer == null) return false;
        String answer = submittedAnswer.trim();
        if (answer.isEmpty()) return false;

        String correctAnswer = question.correctAnswer() == null ? "" : question.correctAnswer().trim();
        if (!"SINGLE_CHOICE".equalsIgnoreCase(question.questionType())) {
            return Objects.equals(correctAnswer, answer);
        }
        if (Objects.equals(correctAnswer, answer)) return true;

        List<QuestionOptionView> exactTextMatches = question.options().stream()
                .filter(option -> option != null && Objects.equals(option.text(), answer))
                .toList();
        return exactTextMatches.size() == 1
                && Objects.equals(exactTextMatches.get(0).optionId(), correctAnswer);
    }
}
