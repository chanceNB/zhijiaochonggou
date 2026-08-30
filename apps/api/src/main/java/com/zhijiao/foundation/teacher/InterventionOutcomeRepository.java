package com.zhijiao.foundation.teacher;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.Instant;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.Optional;

@Repository
public class InterventionOutcomeRepository {
    private final JdbcTemplate jdbcTemplate;

    public InterventionOutcomeRepository(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    public Optional<InterventionOutcome> findByInterventionId(String interventionId) {
        return jdbcTemplate.query(selectSql() + " where intervention_id = ?", (rs, rowNum) -> map(rs), interventionId)
                .stream().findFirst();
    }

    public void insertTransferValidation(InterventionOutcome outcome, TransferValidation validation) {
        jdbcTemplate.update("""
                insert into app.transfer_validations
                    (transfer_validation_id, intervention_id, assignment_id, practice_set_id, student_id, course_id,
                     class_id, knowledge_point_id, result, attempt_count, correct_count, evaluated_at, data_origin,
                     demo_run_id, demo_case_id, correlation_id, source_version)
                values (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)
                """, "transfer-" + outcome.outcomeId(), outcome.interventionId(), outcome.assignmentId(), outcome.practiceSetId(),
                outcome.studentId(), outcome.courseId(), outcome.classId(), outcome.knowledgePointId(), validation.result(),
                validation.attemptCount(), validation.correctCount(), timestamp(outcome.completedAt()), outcome.dataOrigin(),
                outcome.demoRunId(), outcome.demoCaseId(), outcome.correlationId(), outcome.sourceVersion());
    }

    public void insert(InterventionOutcome outcome) {
        jdbcTemplate.update("""
                insert into app.intervention_outcomes
                    (outcome_id, intervention_id, assignment_id, practice_set_id, student_id, course_id, class_id,
                     knowledge_point_id, predicted_lift, prediction_low, prediction_high, mastery_before,
                     confidence_before, forgetting_risk_before, weakness_score_before, evidence_count_before,
                     mastery_after, confidence_after, forgetting_risk_after, evidence_count_after, actual_lift,
                     prediction_deviation, transfer_validation, practice_accuracy_after, completed_at, data_origin,
                     demo_run_id, demo_case_id, correlation_id, source_version)
                values (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)
                """, outcome.outcomeId(), outcome.interventionId(), outcome.assignmentId(), outcome.practiceSetId(),
                outcome.studentId(), outcome.courseId(), outcome.classId(), outcome.knowledgePointId(), outcome.predictedLift(),
                outcome.predictionLow(), outcome.predictionHigh(), outcome.masteryBefore(), outcome.confidenceBefore(),
                outcome.forgettingRiskBefore(), outcome.weaknessScoreBefore(), outcome.evidenceCountBefore(), outcome.masteryAfter(),
                outcome.confidenceAfter(), outcome.forgettingRiskAfter(), outcome.evidenceCountAfter(), outcome.actualLift(),
                outcome.predictionDeviation(), outcome.transferValidation(), outcome.practiceAccuracyAfter(), timestamp(outcome.completedAt()),
                outcome.dataOrigin(), outcome.demoRunId(), outcome.demoCaseId(), outcome.correlationId(), outcome.sourceVersion());
    }

    private String selectSql() {
        return """
                select outcome_id, intervention_id, assignment_id, practice_set_id, student_id, course_id, class_id,
                       knowledge_point_id, predicted_lift, prediction_low, prediction_high, mastery_before,
                       confidence_before, forgetting_risk_before, weakness_score_before, evidence_count_before,
                       mastery_after, confidence_after, forgetting_risk_after, evidence_count_after, actual_lift,
                       prediction_deviation, transfer_validation, practice_accuracy_after, completed_at, data_origin,
                       demo_run_id, demo_case_id, correlation_id, source_version
                from app.intervention_outcomes
                """;
    }

    private InterventionOutcome map(ResultSet rs) throws SQLException {
        double weakness = rs.getDouble("weakness_score_before");
        return new InterventionOutcome(rs.getString("outcome_id"), rs.getString("intervention_id"),
                rs.getString("assignment_id"), rs.getString("practice_set_id"), rs.getString("student_id"),
                rs.getString("course_id"), rs.getString("class_id"), rs.getString("knowledge_point_id"),
                rs.getDouble("predicted_lift"), rs.getDouble("prediction_low"), rs.getDouble("prediction_high"),
                rs.getDouble("mastery_before"), rs.getDouble("confidence_before"), rs.getDouble("forgetting_risk_before"),
                rs.wasNull() ? null : weakness, rs.getInt("evidence_count_before"), rs.getDouble("mastery_after"),
                rs.getDouble("confidence_after"), rs.getDouble("forgetting_risk_after"), rs.getInt("evidence_count_after"),
                rs.getDouble("actual_lift"), rs.getDouble("prediction_deviation"), rs.getString("transfer_validation"),
                rs.getDouble("practice_accuracy_after"), rs.getString("data_origin"), rs.getString("demo_run_id"),
                rs.getString("demo_case_id"), rs.getString("correlation_id"), rs.getString("source_version"),
                instant(rs.getObject("completed_at")));
    }

    private Object timestamp(Instant value) {
        return value.atOffset(ZoneOffset.UTC);
    }

    private Instant instant(Object value) {
        if (value instanceof OffsetDateTime dateTime) return dateTime.toInstant();
        if (value instanceof java.sql.Timestamp timestamp) return timestamp.toInstant();
        if (value instanceof Instant instant) return instant;
        throw new IllegalArgumentException("Unsupported timestamp value: " + value);
    }
}
