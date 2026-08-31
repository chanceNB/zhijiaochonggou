package com.zhijiao.foundation.student.practice;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.zhijiao.foundation.knowledge.KnowledgeQueryPort;
import com.zhijiao.foundation.student.coach.DiagnosticQuestionValidator;
import com.zhijiao.foundation.student.coach.LlmPort;
import com.zhijiao.foundation.student.coach.LlmUnavailableException;
import org.junit.jupiter.api.Test;

import java.time.Clock;
import java.time.Instant;
import java.time.ZoneOffset;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class SimilarQuestionServiceTest {
    private static final Instant NOW = Instant.parse("2026-08-29T00:00:00Z");

    @Test
    void onlyIncorrectAttemptCanGenerateSimilarQuestions() {
        PracticeRepository repository = mock(PracticeRepository.class);
        PracticeRepository.AttemptRow attempt = attempt("attempt-correct", "ps-1", true, "coach-1");
        when(repository.findAttemptById("attempt-correct")).thenReturn(Optional.of(attempt));
        SimilarQuestionService service = service(repository, mock(LlmPort.class));

        assertThatThrownBy(() -> service.generate("coach-1", "attempt-correct", 1))
                .isInstanceOf(DomainRuleViolationException.class)
                .hasMessageContaining("incorrect attempt");
    }

    @Test
    void attemptMustBelongToTheRequestedCoachSessionEvenWhenLegacyRowHasNoSessionId() {
        PracticeRepository repository = mock(PracticeRepository.class);
        LlmPort llm = mock(LlmPort.class);
        PracticeRepository.AttemptRow attempt = attempt("attempt-orphan", "ps-1", false, null);
        when(repository.findAttemptById("attempt-orphan")).thenReturn(Optional.of(attempt));
        SimilarQuestionService service = service(repository, llm);

        assertThatThrownBy(() -> service.generate("coach-1", "attempt-orphan", 1))
                .isInstanceOf(DomainRuleViolationException.class)
                .hasMessageContaining("coach session");
        verify(llm, never()).complete(any());
    }

    @Test
    void resetDemoAttemptCannotGenerateSimilarQuestions() {
        PracticeRepository repository = mock(PracticeRepository.class);
        LlmPort llm = mock(LlmPort.class);
        PracticeRepository.AttemptRow attempt = attempt("attempt-reset", "ps-1", false, "coach-1");
        when(repository.findAttemptById("attempt-reset")).thenReturn(Optional.of(attempt));
        when(repository.findActiveDemo("stu-xiaoming", "course-data-structures"))
                .thenReturn(Optional.of(new PracticeRepository.DemoContext("demo-2", "DEMO-GRAPH-001", "corr-2", "baseline-ds-v1")));
        SimilarQuestionService service = service(repository, llm);

        assertThatThrownBy(() -> service.generate("coach-1", "attempt-reset", 1))
                .isInstanceOf(DomainRuleViolationException.class)
                .hasMessageContaining("reset demo run");
        verify(llm, never()).complete(any());
    }

    @Test
    void preservesLlmAvailabilityFailureInsteadOfReportingInvalidOutput() {
        PracticeRepository repository = mock(PracticeRepository.class);
        LlmPort llm = mock(LlmPort.class);
        PracticeRepository.AttemptRow attempt = attempt("attempt-unavailable", "ps-1", false, "coach-1");
        when(repository.findAttemptById("attempt-unavailable")).thenReturn(Optional.of(attempt));
        when(repository.findQuestion("ps-1", "q-1")).thenReturn(Optional.of(new InternalQuestion(
                "q-1", "ps-1", "AI_COACH_DIAGNOSTIC", null, "kp-graph-bfs-dfs", "SINGLE_CHOICE",
                "Original question", java.util.List.of(new QuestionOptionView("A", "A"), new QuestionOptionView("B", "B")),
                "A", "Explanation", 0.5, NOW)));
        when(repository.findActiveDemo("stu-xiaoming", "course-data-structures"))
                .thenReturn(Optional.of(new PracticeRepository.DemoContext("demo-1", "DEMO-GRAPH-001", "corr-1", "baseline-ds-v1")));
        when(llm.complete(any())).thenThrow(new LlmUnavailableException("LLM credentials are not configured"));
        SimilarQuestionService service = service(repository, llm);

        assertThatThrownBy(() -> service.generate("coach-1", "attempt-unavailable", 1))
                .isInstanceOf(LlmUnavailableException.class)
                .hasMessageContaining("credentials");
    }

    private SimilarQuestionService service(PracticeRepository repository, LlmPort llm) {
        return new SimilarQuestionService(repository, null, mock(KnowledgeQueryPort.class), llm,
                new ObjectMapper(), new DiagnosticQuestionValidator(), Clock.fixed(NOW, ZoneOffset.UTC));
    }

    private PracticeRepository.AttemptRow attempt(String attemptId, String practiceSetId, boolean correct,
                                                  String coachSessionId) {
        return new PracticeRepository.AttemptRow(attemptId, practiceSetId, NOW, "stu-xiaoming",
                "course-data-structures", "class-cs-2024-01", "kp-graph-bfs-dfs", "q-1",
                "AI_COACH_DIAGNOSTIC", "MEDIUM", correct, 10, 10000, 1, "B", "LIVE_DEMO",
                "source-v1", "baseline-ds-v1", "demo-1", "DEMO-GRAPH-001", "corr-1", coachSessionId, null);
    }
}
