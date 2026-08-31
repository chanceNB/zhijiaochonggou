package com.zhijiao.foundation.student.practice;

import org.junit.jupiter.api.Test;

import java.time.Instant;
import java.time.Clock;
import java.util.List;
import java.util.Optional;
import com.fasterxml.jackson.databind.ObjectMapper;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class PracticeWrongBookServiceTest {

    @Test
    void studentQuestionDoesNotExposeAnswerKeyBeforeSubmission() {
        InternalQuestion internal = new InternalQuestion(
                "q-1", "ps-1", "AI_COACH_DIAGNOSTIC", null, "kp-graph-bfs-dfs",
                "SINGLE_CHOICE", "BFS uses which structure?",
                List.of(new QuestionOptionView("A", "Queue"), new QuestionOptionView("B", "Stack")),
                "A", "Queue maintains breadth-first order.", 0.5, Instant.now());

        StudentQuestion student = StudentQuestion.from(internal);

        assertThat(student.questionId()).isEqualTo("q-1");
        String json;
        try {
            json = new ObjectMapper().writeValueAsString(student);
        } catch (Exception exception) {
            throw new AssertionError(exception);
        }
        assertThat(json).doesNotContain("correctAnswer", "explanation");
    }

    @Test
    void gradingUsesServerSideAnswerKeyAndWrongBookRequiresExplicitWrongAttempt() {
        PracticeRepository repository = mock(PracticeRepository.class);
        InternalQuestion question = new InternalQuestion(
                "q-1", "ps-1", "AI_COACH_DIAGNOSTIC", null, "kp-graph-bfs-dfs",
                "SINGLE_CHOICE", "BFS uses which structure?",
                List.of(new QuestionOptionView("A", "Queue"), new QuestionOptionView("B", "Stack")),
                "A", "Queue maintains breadth-first order.", 0.5, Instant.now());
        when(repository.findQuestion("ps-1", "q-1")).thenReturn(Optional.of(question));
        when(repository.findAttemptByIdempotency("ps-1", "q-1", "idem-1")).thenReturn(Optional.empty());
        when(repository.nextAttemptIndex("stu-xiaoming", "course-data-structures", "kp-graph-bfs-dfs"))
                .thenReturn(1);
        when(repository.insertAttempt(any())).thenAnswer(invocation -> invocation.getArgument(0));
        PracticeService service = new PracticeService(repository, null, null, null);

        PracticeAttemptResult result = service.submitForTest("ps-1", "q-1", "B", 12, "idem-1");

        assertThat(result.correct()).isFalse();
        assertThat(result.correctAnswer()).isEqualTo("A");
        assertThat(result.canGenerateSimilar()).isFalse();
    }

    @Test
    void reviewResolvesQuestionWithinTheSourcePracticeSet() {
        PracticeRepository repository = mock(PracticeRepository.class);
        Instant now = Instant.parse("2026-08-29T00:00:00Z");
        WrongBookItem item = new WrongBookItem("wrong-1", "stu-xiaoming", "course-data-structures",
                "class-cs-2024-01", "q-reused", "attempt-2", "kp-graph-bfs-dfs", null,
                "TO_REVIEW", 0, now, null, "LIVE_DEMO", "demo-2", "DEMO-GRAPH-001", "corr-2", "source-v1");
        PracticeRepository.AttemptRow attempt = new PracticeRepository.AttemptRow(
                "attempt-2", "ps-2", now, "stu-xiaoming", "course-data-structures", "class-cs-2024-01",
                "kp-graph-bfs-dfs", "q-reused", "AI_COACH_DIAGNOSTIC", "MEDIUM", false, 10, 10000, 1,
                "B", "LIVE_DEMO", "source-v1", "baseline-ds-v1", "demo-2", "DEMO-GRAPH-001", "corr-2", null, null);
        InternalQuestion scopedQuestion = new InternalQuestion("q-reused", "ps-2", "AI_COACH_DIAGNOSTIC", null,
                "kp-graph-bfs-dfs", "SINGLE_CHOICE", "Scoped question",
                List.of(new QuestionOptionView("A", "Queue"), new QuestionOptionView("B", "Stack")),
                "B", "Scoped explanation", 0.5, now);
        when(repository.findWrongBook("stu-xiaoming", "wrong-1")).thenReturn(Optional.of(item));
        when(repository.findAttemptById("attempt-2")).thenReturn(Optional.of(attempt));
        when(repository.findActiveDemo("stu-xiaoming", "course-data-structures"))
                .thenReturn(Optional.of(new PracticeRepository.DemoContext("demo-2", "DEMO-GRAPH-001", "corr-2", "baseline-ds-v1")));
        when(repository.findQuestion("ps-2", "q-reused")).thenReturn(Optional.of(scopedQuestion));
        when(repository.updateWrongBookReview(item, true, now)).thenReturn(new WrongBookItem(
                item.wrongItemId(), item.studentId(), item.courseId(), item.classId(), item.questionId(),
                item.sourceAttemptId(), item.knowledgePointId(), item.reason(), "MASTERED", 1, item.addedAt(), now,
                item.dataOrigin(), item.demoRunId(), item.demoCaseId(), item.correlationId(), item.sourceVersion()));

        WrongBookService service = new WrongBookService(repository, Clock.fixed(now, java.time.ZoneOffset.UTC));
        WrongBookService.WrongBookReviewResult result = service.review("stu-xiaoming", "wrong-1", "B", 10);

        assertThat(result.correct()).isTrue();
        assertThat(result.status()).isEqualTo("MASTERED");
        verify(repository).findQuestion("ps-2", "q-reused");
    }

    @Test
    void incorrectReviewRemainsToReview() {
        PracticeRepository repository = mock(PracticeRepository.class);
        Instant now = Instant.parse("2026-08-29T00:00:00Z");
        WrongBookItem item = new WrongBookItem("wrong-2", "stu-xiaoming", "course-data-structures",
                "class-cs-2024-01", "q-2", "attempt-3", "kp-graph-basics", null,
                "LEARNING", 2, now, now, "LIVE_DEMO", "demo-2", "DEMO-GRAPH-001", "corr-2", "source-v1");
        PracticeRepository.AttemptRow attempt = new PracticeRepository.AttemptRow(
                "attempt-3", "ps-3", now, "stu-xiaoming", "course-data-structures", "class-cs-2024-01",
                "kp-graph-basics", "q-2", "AI_COACH_DIAGNOSTIC", "MEDIUM", false, 10, 10000, 1,
                "A", "LIVE_DEMO", "source-v1", "baseline-ds-v1", "demo-2", "DEMO-GRAPH-001", "corr-2", null, null);
        InternalQuestion question = new InternalQuestion("q-2", "ps-3", "AI_COACH_DIAGNOSTIC", null,
                "kp-graph-basics", "SINGLE_CHOICE", "题目",
                List.of(new QuestionOptionView("A", "相邻"), new QuestionOptionView("B", "连通")),
                "A", "解释", 0.5, now);
        when(repository.findWrongBook("stu-xiaoming", "wrong-2")).thenReturn(Optional.of(item));
        when(repository.findAttemptById("attempt-3")).thenReturn(Optional.of(attempt));
        when(repository.findActiveDemo("stu-xiaoming", "course-data-structures"))
                .thenReturn(Optional.of(new PracticeRepository.DemoContext("demo-2", "DEMO-GRAPH-001", "corr-2", "baseline-ds-v1")));
        when(repository.findQuestion("ps-3", "q-2")).thenReturn(Optional.of(question));
        when(repository.updateWrongBookReview(item, false, now)).thenReturn(new WrongBookItem(
                item.wrongItemId(), item.studentId(), item.courseId(), item.classId(), item.questionId(),
                item.sourceAttemptId(), item.knowledgePointId(), item.reason(), "TO_REVIEW", 3, item.addedAt(), item.repairedAt(),
                item.dataOrigin(), item.demoRunId(), item.demoCaseId(), item.correlationId(), item.sourceVersion()));

        WrongBookService service = new WrongBookService(repository, Clock.fixed(now, java.time.ZoneOffset.UTC));
        WrongBookService.WrongBookReviewResult result = service.review("stu-xiaoming", "wrong-2", "B", 10);

        assertThat(result.correct()).isFalse();
        assertThat(result.status()).isEqualTo("TO_REVIEW");
    }
}
