package com.zhijiao.foundation.student.practice;

import org.junit.jupiter.api.Test;

import java.time.Instant;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class AnswerEvaluatorTest {
    private static final InternalQuestion QUESTION = new InternalQuestion(
            "q-1", "ps-1", "AI_COACH_DIAGNOSTIC", null, "kp-graph-basics", "SINGLE_CHOICE",
            "两个顶点之间有边相连时，关系称为（）。",
            List.of(new QuestionOptionView("A", "邻接矩阵"), new QuestionOptionView("B", "相邻"),
                    new QuestionOptionView("C", "连通"), new QuestionOptionView("D", "关联")),
            "B", "解释", 0.5, Instant.now());

    @Test
    void acceptsOptionIdForSingleChoice() {
        assertThat(AnswerEvaluator.evaluate(QUESTION, "B")).isTrue();
    }

    @Test
    void acceptsUniqueExactOptionTextForLegacyClients() {
        assertThat(AnswerEvaluator.evaluate(QUESTION, "相邻")).isTrue();
    }

    @Test
    void rejectsWrongOptionIdAndWrongOptionText() {
        assertThat(AnswerEvaluator.evaluate(QUESTION, "A")).isFalse();
        assertThat(AnswerEvaluator.evaluate(QUESTION, "连通")).isFalse();
    }

    @Test
    void doesNotPerformFuzzyTextMatching() {
        assertThat(AnswerEvaluator.evaluate(QUESTION, "相 邻")).isFalse();
    }
}
