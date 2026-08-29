package com.zhijiao.foundation.student.coach;

import com.zhijiao.foundation.knowledge.Citation;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThatCode;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class DiagnosticQuestionValidatorTest {

    private final DiagnosticQuestionValidator validator = new DiagnosticQuestionValidator();

    @Test
    void acceptsExactlyTwoWellFormedSingleChoiceQuestions() {
        assertThatCode(() -> validator.validate(List.of(question("q-1"), question("q-2")),
                "kp-graph-bfs-dfs", 2)).doesNotThrowAnyException();
    }

    @Test
    void rejectsWrongCountDuplicateOptionsAndMissingCorrectAnswer() {
        DiagnosticQuestion invalid = new DiagnosticQuestion(
                "q-1", "kp-graph-bfs-dfs", "SINGLE_CHOICE", "Which structure?",
                List.of(new QuestionOption("A", "队列"), new QuestionOption("A", "栈")),
                "B", "说明", new DiagnosticTarget("BFS_QUEUE_ORDER", "访问顺序"),
                0.5, List.of(new Citation("doc-1", "chunk-1", "讲义", "队列", 0.9)),
                "test", "test", "test");

        assertThatThrownBy(() -> validator.validate(List.of(invalid), "kp-graph-bfs-dfs", 2))
                .isInstanceOf(DiagnosticValidationException.class);
    }

    private DiagnosticQuestion question(String id) {
        return new DiagnosticQuestion(
                id, "kp-graph-bfs-dfs", "SINGLE_CHOICE", "Which structure?",
                List.of(new QuestionOption("A", "队列"), new QuestionOption("B", "栈")),
                "A", "说明", new DiagnosticTarget("BFS_QUEUE_ORDER", "访问顺序"),
                0.5, List.of(new Citation("doc-1", "chunk-1", "讲义", "队列", 0.9)),
                "test", "test", "test");
    }
}
