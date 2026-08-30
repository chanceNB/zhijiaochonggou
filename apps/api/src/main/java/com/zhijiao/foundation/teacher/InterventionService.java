package com.zhijiao.foundation.teacher;

import com.zhijiao.foundation.analytics.AnalyticsProjectionService;
import com.zhijiao.foundation.analytics.DomainEventOutboxRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Clock;
import java.time.Instant;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.UUID;

@Service
public class InterventionService {
    public static final String SOURCE_VERSION = "teacher-intervention-v1";

    private final InterventionRepository repository;
    private final AnalysisRecommendationService recommendationService;
    private final EffectEstimator effectEstimator;
    private final DomainEventOutboxRepository outbox;
    private final AnalyticsProjectionService analyticsProjectionService;
    private final Clock clock;

    @Autowired
    public InterventionService(InterventionRepository repository, AnalysisRecommendationService recommendationService,
                               EffectEstimator effectEstimator, DomainEventOutboxRepository outbox,
                               AnalyticsProjectionService analyticsProjectionService, Clock clock) {
        this.repository = repository;
        this.recommendationService = recommendationService;
        this.effectEstimator = effectEstimator;
        this.outbox = outbox;
        this.analyticsProjectionService = analyticsProjectionService;
        this.clock = clock == null ? Clock.systemUTC() : clock;
    }

    public InterventionService(InterventionRepository repository, AnalysisRecommendationService recommendationService,
                               EffectEstimator effectEstimator, Clock clock) {
        this(repository, recommendationService, effectEstimator, null, null, clock);
    }

    @Transactional
    public Intervention propose(String recommendationId, String strategyCode, String teacherRationale,
                                String idempotencyKey) {
        require(idempotencyKey, "Idempotency-Key is required");
        Intervention replay = repository.findByIdempotencyKey(idempotencyKey).orElse(null);
        if (replay != null) return replay;
        require(recommendationId, "recommendationId is required");
        require(strategyCode, "strategyCode is required");
        if (teacherRationale == null || teacherRationale.trim().length() < 10) {
            throw new IllegalArgumentException("teacherRationale must be at least 10 characters");
        }
        AnalysisRecommendation recommendation = recommendationService.get(recommendationId);
        if (repository.findByRecommendationId(recommendationId).isPresent()) {
            throw new IllegalStateException("An intervention already exists for this recommendation");
        }
        AnalysisRecommendation.Candidate candidate = recommendation.candidates().stream()
                .filter(item -> item.strategyCode().equals(strategyCode.trim())).findFirst()
                .orElseThrow(() -> new IllegalArgumentException("strategyCode is not one of the captured candidates"));
        EffectEstimate estimate = effectEstimator.estimate(recommendation, candidate);
        Intervention intervention = new Intervention("int-" + UUID.randomUUID().toString().replace("-", ""),
                recommendation.recommendationId(), recommendation.studentId(), recommendation.courseId(), recommendation.classId(),
                recommendation.knowledgePointId(), candidate.strategyCode(), teacherRationale.trim(), estimate.predictedLift(),
                estimate.predictionLow(), estimate.predictionHigh(), "PROPOSED", 1, null, recommendation.demoRunId(),
                recommendation.demoCaseId(), recommendation.correlationId(), SOURCE_VERSION, idempotencyKey, null, null,
                Instant.now(clock), null, null);
        repository.insert(intervention);
        appendEvent(intervention, "INTERVENTION_PROPOSED", intervention.createdAt());
        refreshProjection();
        return intervention;
    }

    @Transactional
    public Intervention approve(String interventionId, String ifMatch, String idempotencyKey) {
        require(idempotencyKey, "Idempotency-Key is required");
        Intervention current = get(interventionId);
        if (idempotencyKey.equals(current.approveIdempotencyKey()) && "APPROVED".equals(current.status())) return current;
        if (!"PROPOSED".equals(current.status())) throw new IllegalStateException("Only a proposed intervention can be approved");
        checkVersion(current, ifMatch);
        Instant now = Instant.now(clock);
        if (repository.approve(interventionId, current.version(), idempotencyKey, now) != 1) {
            throw new PreconditionFailedException("Intervention version no longer matches");
        }
        Intervention updated = get(interventionId);
        appendEvent(updated, "INTERVENTION_APPROVED", now);
        refreshProjection();
        return updated;
    }

    @Transactional
    public Intervention commit(String interventionId, String ifMatch, String idempotencyKey, Instant dueAt) {
        require(idempotencyKey, "Idempotency-Key is required");
        Intervention current = get(interventionId);
        if (idempotencyKey.equals(current.commitIdempotencyKey()) && "COMMITTED".equals(current.status())) return current;
        if (!"APPROVED".equals(current.status())) throw new IllegalStateException("Only an approved intervention can be committed");
        checkVersion(current, ifMatch);
        Instant now = Instant.now(clock);
        String assignmentId = "assign-" + UUID.randomUUID().toString().replace("-", "");
        String practiceSetId = "ps-int-" + UUID.randomUUID().toString().replace("-", "");
        if (repository.commit(interventionId, current.version(), assignmentId, idempotencyKey, now) != 1) {
            throw new PreconditionFailedException("Intervention version no longer matches");
        }
        repository.insertPracticeSet(practiceSetId, current, now);
        repository.insertAssignment(new InterventionAssignment(assignmentId, interventionId, practiceSetId,
                current.studentId(), current.courseId(), current.classId(), current.knowledgePointId(),
                "PENDING_STUDENT", dueAt, now, current.demoRunId(), current.demoCaseId(), current.correlationId(),
                current.sourceVersion()));
        Intervention updated = get(interventionId);
        appendEvent(updated, "INTERVENTION_COMMITTED", now);
        refreshProjection();
        return updated;
    }

    @Transactional(readOnly = true)
    public Intervention get(String interventionId) {
        return repository.findById(interventionId)
                .orElseThrow(() -> new InterventionNotFoundException(interventionId));
    }

    @Transactional(readOnly = true)
    public InterventionAssignment assignment(String interventionId) {
        return repository.findAssignment(interventionId).orElse(null);
    }

    private void checkVersion(Intervention current, String ifMatch) {
        if (ifMatch == null || ifMatch.isBlank()) throw new PreconditionFailedException("If-Match is required");
        String normalized = ifMatch.trim();
        if (normalized.startsWith("\"") && normalized.endsWith("\"")) normalized = normalized.substring(1, normalized.length() - 1);
        try {
            if (Integer.parseInt(normalized) != current.version()) throw new PreconditionFailedException("Intervention version no longer matches");
        } catch (NumberFormatException exception) {
            throw new PreconditionFailedException("If-Match must contain the intervention version");
        }
    }

    private void appendEvent(Intervention intervention, String eventType, Instant occurredAt) {
        if (outbox != null) {
            outbox.append("Intervention", intervention.interventionId(), eventType, occurredAt, SOURCE_VERSION,
                    intervention.demoRunId() == null ? "MANUAL_CAPTURE" : "LIVE_DEMO", intervention.demoRunId(),
                    intervention.demoCaseId(), intervention.correlationId());
        }
    }

    private void refreshProjection() {
        if (analyticsProjectionService != null) analyticsProjectionService.refresh();
    }

    private void require(String value, String message) {
        if (value == null || value.isBlank()) throw new IllegalArgumentException(message);
    }
}
