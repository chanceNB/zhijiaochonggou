package com.zhijiao.foundation.student.practice;

import com.zhijiao.foundation.analytics.AnalyticsProjectionService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Clock;
import java.time.Instant;
import java.util.UUID;

@Service
public class WrongBookService {
    private final PracticeRepository repository;
    private final Clock clock;
    private final AnalyticsProjectionService analyticsProjectionService;

    @org.springframework.beans.factory.annotation.Autowired
    public WrongBookService(PracticeRepository repository, Clock clock, AnalyticsProjectionService analyticsProjectionService) {
        this.repository = repository;
        this.clock = clock;
        this.analyticsProjectionService = analyticsProjectionService;
    }

    public WrongBookService(PracticeRepository repository, Clock clock) {
        this(repository, clock, null);
    }

    @Transactional
    public WrongBookItem add(String studentId, String attemptId, String reason) {
        PracticeRepository.AttemptRow attempt = repository.findAttemptById(attemptId)
                .orElseThrow(() -> new PracticeAttemptNotFoundException(attemptId));
        if (!attempt.studentId().equals(studentId)) {
            throw new DomainRuleViolationException("Attempt does not belong to student");
        }
        if (attempt.correct()) throw new DomainRuleViolationException("Only incorrect attempts can be added to wrong book");
        ensureWritableDemo(attempt);
        WrongBookItem existing = repository.findWrongBookBySourceAttempt(studentId, attemptId).orElse(null);
        if (existing != null) return existing;
        PracticeRepository.DemoContext demo = new PracticeRepository.DemoContext(attempt.demoRunId(), attempt.demoCaseId(),
                attempt.correlationId(), attempt.baselineVersion());
        WrongBookItem item = new WrongBookItem("wrong-" + UUID.randomUUID().toString().replace("-", ""), studentId,
                attempt.courseId(), attempt.classId(), attempt.questionId(), attempt.attemptId(), attempt.knowledgePointId(),
                reason, "TO_REVIEW", 0, Instant.now(clock), null, attempt.dataOrigin(), demo.demoRunId(), demo.demoCaseId(),
                demo.correlationId(), attempt.sourceVersion());
        repository.insertWrongBook(item);
        if (analyticsProjectionService != null) analyticsProjectionService.refresh();
        return repository.findWrongBookBySourceAttempt(studentId, attemptId).orElse(item);
    }

    @Transactional(readOnly = true)
    public WrongBookPage list(String studentId, String knowledgePointId, String status, int page, int size) {
        int normalizedPage = Math.max(1, page);
        int normalizedSize = Math.min(100, Math.max(1, size));
        return new WrongBookPage(repository.findWrongBook(studentId, knowledgePointId, status, normalizedPage, normalizedSize),
                normalizedPage, normalizedSize, repository.countWrongBook(studentId, knowledgePointId, status));
    }

    @Transactional
    public WrongBookReviewResult review(String studentId, String wrongItemId, String answer, int durationSeconds) {
        if (answer == null || answer.isBlank() || durationSeconds <= 0) {
            throw new IllegalArgumentException("answer and positive durationSeconds are required");
        }
        WrongBookItem item = repository.findWrongBook(studentId, wrongItemId)
                .orElseThrow(() -> new WrongBookItemNotFoundException(wrongItemId));
        PracticeRepository.AttemptRow sourceAttempt = repository.findAttemptById(item.sourceAttemptId())
                .orElseThrow(() -> new PracticeAttemptNotFoundException(item.sourceAttemptId()));
        if (!sourceAttempt.studentId().equals(studentId)
                || !sourceAttempt.questionId().equals(item.questionId())) {
            throw new DomainRuleViolationException("Wrong-book source attempt does not match student or question");
        }
        ensureWritableDemo(sourceAttempt);
        InternalQuestion question = repository.findQuestion(sourceAttempt.practiceSetId(), item.questionId())
                .orElseThrow(() -> new DomainRuleViolationException("Question is unavailable for review"));
        boolean correct = question.correctAnswer().equals(answer.trim());
        WrongBookItem updated = repository.updateWrongBookReview(item, correct, Instant.now(clock));
        if (analyticsProjectionService != null) analyticsProjectionService.refresh();
        return new WrongBookReviewResult(correct, updated.status(), updated.reviewCount());
    }

    private void ensureWritableDemo(PracticeRepository.AttemptRow attempt) {
        if (!"LIVE_DEMO".equals(attempt.dataOrigin()) || attempt.demoRunId() == null) {
            return;
        }
        PracticeRepository.DemoContext activeDemo = repository.findActiveDemo(attempt.studentId(), attempt.courseId())
                .orElseThrow(() -> new DomainRuleViolationException("An active demo run is required for LIVE_DEMO wrong-book writes"));
        if (!attempt.demoRunId().equals(activeDemo.demoRunId())) {
            throw new DomainRuleViolationException("Wrong-book item belongs to a reset demo run");
        }
    }

    public record WrongBookReviewResult(boolean correct, String status, int reviewCount) {
    }
}
