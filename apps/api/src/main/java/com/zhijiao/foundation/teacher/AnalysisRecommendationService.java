package com.zhijiao.foundation.teacher;

import com.zhijiao.foundation.analytics.DomainEventOutboxRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Clock;
import java.time.Instant;
import java.util.List;
import java.util.HashSet;
import java.util.Set;
import java.util.UUID;
import java.util.stream.IntStream;

@Service
public class AnalysisRecommendationService {
    public static final String SOURCE = "SMARTBI_AICHAT";
    public static final String CAPTURE_MODE = "MANUAL";
    public static final String SOURCE_VERSION = "teacher-recommendation-v1";

    private final AnalysisRecommendationRepository repository;
    private final DomainEventOutboxRepository outbox;
    private final Clock clock;

    @Autowired
    public AnalysisRecommendationService(AnalysisRecommendationRepository repository,
                                         DomainEventOutboxRepository outbox, Clock clock) {
        this.repository = repository;
        this.outbox = outbox;
        this.clock = clock == null ? Clock.systemUTC() : clock;
    }

    public AnalysisRecommendationService(AnalysisRecommendationRepository repository, Clock clock) {
        this(repository, null, clock);
    }

    @Transactional
    public AnalysisRecommendation capture(AnalysisRecommendationCapture command) {
        require(command.idempotencyKey(), "Idempotency-Key is required");
        AnalysisRecommendation existing = repository.findByIdempotencyKey(command.idempotencyKey()).orElse(null);
        if (existing != null) return existing;
        if (!SOURCE.equals(command.source())) {
            throw new IllegalArgumentException("Only SMARTBI_AICHAT recommendations can be captured");
        }
        require(command.studentId(), "studentId is required");
        require(command.courseId(), "courseId is required");
        require(command.knowledgePointId(), "knowledgePointId is required");
        require(command.analysisSummary(), "analysisSummary is required");
        if (command.candidates() == null || command.candidates().size() != 3) {
            throw new IllegalArgumentException("Exactly three recommendation candidates are required");
        }
        AnalysisRecommendationRepository.DemoContext context = repository
                .findDemoContext(command.studentId(), command.courseId(), command.demoRunId())
                .orElseThrow(() -> new IllegalArgumentException("An active demo run is required for recommendation capture"));
        if (command.classId() != null && !command.classId().isBlank() && !context.classId().equals(command.classId())) {
            throw new IllegalArgumentException("classId does not match demo run");
        }
        if (command.demoCaseId() != null && !command.demoCaseId().isBlank()
                && !context.demoCaseId().equals(command.demoCaseId())) {
            throw new IllegalArgumentException("demoCaseId does not match demo run");
        }
        if (command.correlationId() != null && !command.correlationId().isBlank()
                && !context.correlationId().equals(command.correlationId())) {
            throw new IllegalArgumentException("correlationId does not match demo run");
        }
        Instant capturedAt = Instant.now(clock);
        Instant generatedAt = command.generatedAt() == null ? capturedAt : command.generatedAt();
        Set<String> strategyCodes = new HashSet<>();
        List<AnalysisRecommendation.Candidate> candidates = IntStream.range(0, command.candidates().size())
                .mapToObj(index -> {
                    AnalysisRecommendationCapture.Candidate candidate = command.candidates().get(index);
                    require(candidate.strategyCode(), "candidate strategyCode is required");
                    require(candidate.title(), "candidate title is required");
                    require(candidate.rationale(), "candidate rationale is required");
                    require(candidate.actionDescription(), "candidate actionDescription is required");
                    if (!strategyCodes.add(candidate.strategyCode().trim())) {
                        throw new IllegalArgumentException("Candidate strategyCode values must be unique");
                    }
                    return new AnalysisRecommendation.Candidate(0, candidate.strategyCode().trim(), candidate.title().trim(),
                            candidate.rationale().trim(), candidate.actionDescription().trim(), null);
                }).toList();
        List<AnalysisRecommendation.Candidate> snapshotted = IntStream.range(0, candidates.size()).mapToObj(index -> {
            AnalysisRecommendation.Candidate candidate = candidates.get(index);
            return new AnalysisRecommendation.Candidate(index + 1, candidate.strategyCode(),
                        candidate.title(), candidate.rationale(), candidate.actionDescription(),
                        repository.snapshot(new AnalysisRecommendationCapture.Candidate(candidate.strategyCode(),
                                candidate.title(), candidate.rationale(), candidate.actionDescription())));
        }).toList();
        AnalysisRecommendation recommendation = new AnalysisRecommendation(
                "rec-" + UUID.randomUUID().toString().replace("-", ""), command.studentId(), command.courseId(),
                context.classId(), command.knowledgePointId(), context.demoRunId(), context.demoCaseId(),
                context.correlationId(), command.analysisSummary().trim(), command.evidenceRefs(), snapshotted, SOURCE, CAPTURE_MODE,
                "PENDING_TEACHER_REVIEW", generatedAt, capturedAt, SOURCE_VERSION);
        repository.insert(recommendation, command.idempotencyKey());
        if (outbox != null) {
            outbox.append("AnalysisRecommendation", recommendation.recommendationId(), "RECOMMENDATION_CAPTURED",
                    capturedAt, SOURCE_VERSION, "MANUAL_CAPTURE", recommendation.demoRunId(), recommendation.demoCaseId(),
                    recommendation.correlationId());
        }
        return recommendation;
    }

    @Transactional(readOnly = true)
    public AnalysisRecommendation get(String recommendationId) {
        return repository.findById(recommendationId)
                .orElseThrow(() -> new RecommendationNotFoundException(recommendationId));
    }

    private void require(String value, String message) {
        if (value == null || value.isBlank()) throw new IllegalArgumentException(message);
    }
}
