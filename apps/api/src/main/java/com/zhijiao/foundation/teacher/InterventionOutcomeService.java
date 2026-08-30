package com.zhijiao.foundation.teacher;

import com.zhijiao.foundation.student.learning.StudentKnowledgeState;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Clock;
import java.time.Instant;
import java.util.Optional;
import java.util.UUID;

@Service
public class InterventionOutcomeService {
    private static final String SOURCE_VERSION = "t08-outcome-v1";

    private final InterventionOutcomeRepository repository;
    private final Clock clock;

    public InterventionOutcomeService(InterventionOutcomeRepository repository, Clock clock) {
        this.repository = repository;
        this.clock = clock == null ? Clock.systemUTC() : clock;
    }

    @Transactional
    public InterventionOutcome record(Intervention intervention, InterventionAssignment assignment,
                                      StudentKnowledgeState before, Double weaknessScoreBefore,
                                      StudentKnowledgeState after, TransferValidation validation,
                                      double practiceAccuracy, Instant completedAt) {
        InterventionOutcome existing = repository.findByInterventionId(intervention.interventionId()).orElse(null);
        if (existing != null) return existing;
        double actualLift = after.masteryProbability() - before.masteryProbability();
        InterventionOutcome outcome = new InterventionOutcome(
                "outcome-" + UUID.randomUUID().toString().replace("-", ""), intervention.interventionId(),
                assignment.assignmentId(), assignment.practiceSetId(), intervention.studentId(), intervention.courseId(),
                intervention.classId(), intervention.knowledgePointId(), intervention.predictedLift(), intervention.predictionLow(),
                intervention.predictionHigh(), before.masteryProbability(), before.confidence(), before.forgettingRisk(),
                weaknessScoreBefore, before.evidenceCount(), after.masteryProbability(), after.confidence(), after.forgettingRisk(),
                after.evidenceCount(), actualLift, actualLift - intervention.predictedLift(), validation.result(), practiceAccuracy,
                "LIVE_DEMO", intervention.demoRunId(), intervention.demoCaseId(), intervention.correlationId(), SOURCE_VERSION,
                completedAt == null ? Instant.now(clock) : completedAt);
        repository.insertTransferValidation(outcome, validation);
        repository.insert(outcome);
        return repository.findByInterventionId(intervention.interventionId()).orElse(outcome);
    }

    @Transactional(readOnly = true)
    public InterventionOutcome getByIntervention(String interventionId) {
        return repository.findByInterventionId(interventionId)
                .orElseThrow(() -> new InterventionOutcomeNotFoundException(interventionId));
    }

    @Transactional(readOnly = true)
    public Optional<InterventionOutcome> findByIntervention(String interventionId) {
        return repository.findByInterventionId(interventionId);
    }
}
