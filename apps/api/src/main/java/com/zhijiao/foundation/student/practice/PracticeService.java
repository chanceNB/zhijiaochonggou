package com.zhijiao.foundation.student.practice;

import com.zhijiao.foundation.demo.BaselineSeedService;
import com.zhijiao.foundation.student.learning.LearningStateEngine;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Clock;
import java.time.Instant;
import java.util.List;
import java.util.UUID;

@Service
public class PracticeService {
    private final PracticeRepository repository;
    private final LearningStateEngine learningStateEngine;
    private final Clock clock;
    private final PracticeContextResolver contextResolver;

    public PracticeService(PracticeRepository repository, LearningStateEngine learningStateEngine,
                           Clock clock, PracticeContextResolver contextResolver) {
        this.repository = repository;
        this.learningStateEngine = learningStateEngine;
        this.clock = clock == null ? Clock.systemUTC() : clock;
        this.contextResolver = contextResolver == null ? new PracticeContextResolver(repository) : contextResolver;
    }

    @Transactional(readOnly = true)
    public PracticeSetView getPracticeSet(String practiceSetId) {
        PracticeSet set = repository.findSet(practiceSetId).orElseThrow(() -> new PracticeSetNotFoundException(practiceSetId));
        List<StudentQuestion> questions = repository.findQuestions(practiceSetId).stream().map(StudentQuestion::from).toList();
        List<PracticeAttemptSummary> attempts = repository.findAttempts(practiceSetId).stream()
                .map(attempt -> new PracticeAttemptSummary(attempt.attemptId(), attempt.questionId(), attempt.selectedAnswer(),
                        attempt.correct(), attempt.responseTimeMs(), attempt.attemptTime())).toList();
        return new PracticeSetView(set, questions, attempts);
    }

    @Transactional
    public PracticeAttemptResult submit(String practiceSetId, String questionId, String answer,
                                        int durationSeconds, String idempotencyKey) {
        PracticeSet set = repository.findSet(practiceSetId).orElseThrow(() -> new PracticeSetNotFoundException(practiceSetId));
        if (!"OPEN".equals(set.status())) throw new DomainRuleViolationException("Practice set is already completed");
        if (idempotencyKey == null || idempotencyKey.isBlank()) {
            throw new IllegalArgumentException("Idempotency-Key is required");
        }
        InternalQuestion question = repository.findQuestion(practiceSetId, questionId)
                .orElseThrow(() -> new DomainRuleViolationException("Question does not belong to practice set"));
        if (answer == null || answer.isBlank() || durationSeconds <= 0) {
            throw new IllegalArgumentException("answer and positive durationSeconds are required");
        }
        PracticeRepository.AttemptRow existing = repository.findAttemptByIdempotency(practiceSetId, questionId, idempotencyKey).orElse(null);
        if (existing != null) return replayOrConflict(existing, answer, durationSeconds, question);
        PracticeRepository.DemoContext demo = contextResolver.activeDemo(set.studentId(), set.courseId());
        ensureActiveDemo(set, repository.findAttempts(practiceSetId), demo);
        repository.bindDemoContext(practiceSetId, demo);
        PracticeRepository.AttemptRow attempt = grade(set, question, answer.trim(), durationSeconds, idempotencyKey, demo);
        repository.insertAttempt(attempt);
        // INSERT ... ON CONFLICT DO NOTHING makes the replay path safe on PostgreSQL,
        // where a caught unique-constraint error would otherwise abort the transaction.
        PracticeRepository.AttemptRow persisted = repository.findAttemptByIdempotency(practiceSetId, questionId, idempotencyKey)
                .orElse(attempt);
        return replayOrConflictIfExisting(persisted, attempt, answer, durationSeconds, question);
    }

    /** Small deterministic seam used by unit tests without a database/demo run. */
    public PracticeAttemptResult submitForTest(String practiceSetId, String questionId, String answer,
                                               int durationSeconds, String idempotencyKey) {
        InternalQuestion question = repository.findQuestion(practiceSetId, questionId)
                .orElseThrow(() -> new PracticeSetNotFoundException(practiceSetId));
        PracticeSet set = new PracticeSet(practiceSetId, "stu-xiaoming", "course-data-structures", "class-cs-2024-01",
                null, "AI_COACH_DIAGNOSTIC", "OPEN", "demo-test", BaselineSeedService.DEMO_CASE_ID,
                "corr-test", BaselineSeedService.SOURCE_VERSION, Instant.now(clock), null);
        PracticeRepository.AttemptRow attempt = grade(set, question, answer.trim(), durationSeconds, idempotencyKey,
                new PracticeRepository.DemoContext("demo-test", BaselineSeedService.DEMO_CASE_ID, "corr-test", BaselineSeedService.BASELINE_VERSION));
        return result(attempt, question);
    }

    @Transactional
    public PracticeOutcome complete(String practiceSetId) {
        PracticeSet set = repository.findSet(practiceSetId).orElseThrow(() -> new PracticeSetNotFoundException(practiceSetId));
        PracticeOutcome existing = repository.findOutcome(practiceSetId).orElse(null);
        if (existing != null) return existing;
        List<PracticeRepository.AttemptRow> attempts = repository.findAttempts(practiceSetId);
        PracticeRepository.DemoContext activeDemo = contextResolver.activeDemo(set.studentId(), set.courseId());
        ensureActiveDemo(set, attempts, activeDemo);
        repository.bindDemoContext(practiceSetId, activeDemo);
        double accuracy = attempts.isEmpty() ? 0.0 : attempts.stream().filter(PracticeRepository.AttemptRow::correct).count() / (double) attempts.size();
        Instant now = Instant.now(clock);
        repository.markCompleted(practiceSetId, now);
        String stateStatus = "NOT_RECOMPUTED";
        if (!attempts.isEmpty() && learningStateEngine != null && attempts.get(0).demoRunId() != null) {
            learningStateEngine.recomputeForStudentCourse(set.studentId(), set.courseId(), now,
                    attempts.get(0).demoRunId(), attempts.get(0).demoCaseId(), attempts.get(0).correlationId());
            stateStatus = "UPDATED";
        }
        PracticeRepository.DemoContext demo = activeDemo;
        return repository.insertOutcome(new PracticeRepository.PracticeOutcomeData(
                "outcome-" + UUID.randomUUID().toString().replace("-", ""), practiceSetId, set.studentId(), set.courseId(),
                accuracy, attempts.size(), attempts.isEmpty() ? "LIVE_DEMO" : attempts.get(0).dataOrigin(), demo.demoRunId(),
                demo.demoCaseId(), demo.correlationId(), set.sourceVersion(), now, stateStatus));
    }

    private PracticeAttemptResult replayOrConflict(PracticeRepository.AttemptRow existing, String answer,
                                                   int durationSeconds, InternalQuestion question) {
        if (!existing.selectedAnswer().equals(answer.trim()) || existing.durationSeconds() != durationSeconds) {
            throw new DomainRuleViolationException("Idempotency key was already used with a different submission");
        }
        return result(existing, question);
    }

    private PracticeAttemptResult replayOrConflictIfExisting(PracticeRepository.AttemptRow persisted,
                                                              PracticeRepository.AttemptRow attempted,
                                                              String answer, int durationSeconds,
                                                              InternalQuestion question) {
        if (persisted.attemptId().equals(attempted.attemptId())) {
            return result(attempted, question);
        }
        return replayOrConflict(persisted, answer, durationSeconds, question);
    }

    private void ensureActiveDemo(PracticeSet set, List<PracticeRepository.AttemptRow> attempts,
                                  PracticeRepository.DemoContext activeDemo) {
        if (set.demoRunId() != null && !set.demoRunId().equals(activeDemo.demoRunId())) {
            throw new DomainRuleViolationException("Practice set belongs to a reset demo run");
        }
        for (PracticeRepository.AttemptRow attempt : attempts) {
            if (attempt.demoRunId() != null && !attempt.demoRunId().equals(activeDemo.demoRunId())) {
                throw new DomainRuleViolationException("Practice set contains an attempt from a reset demo run");
            }
        }
    }

    private PracticeRepository.AttemptRow grade(PracticeSet set, InternalQuestion question, String answer,
                                                int durationSeconds, String idempotencyKey, PracticeRepository.DemoContext demo) {
        boolean correct = question.correctAnswer().equals(answer);
        int index = repository.nextAttemptIndex(set.studentId(), set.courseId(), question.knowledgePointId());
        return PracticeRepository.AttemptRow.newLive(
                "attempt-" + UUID.randomUUID().toString().replace("-", ""), set.practiceSetId(), Instant.now(clock),
                set.studentId(), set.courseId(), set.classId(), question.knowledgePointId(), question.questionId(),
                set.source(), difficultyLabel(question.difficulty()), correct, durationSeconds, durationSeconds * 1000, index,
                answer, set.sourceVersion(), set.sourceVersion(), demo, set.coachSessionId(), idempotencyKey);
    }

    private PracticeAttemptResult result(PracticeRepository.AttemptRow attempt, InternalQuestion question) {
        return new PracticeAttemptResult(attempt.attemptId(), attempt.correct(), question.correctAnswer(), question.explanation(),
                attempt.correct() ? null : "ANSWER_MISMATCH", !attempt.correct(), !attempt.correct());
    }

    private String difficultyLabel(double difficulty) {
        return difficulty >= 0.67 ? "HARD" : difficulty >= 0.34 ? "MEDIUM" : "EASY";
    }
}
