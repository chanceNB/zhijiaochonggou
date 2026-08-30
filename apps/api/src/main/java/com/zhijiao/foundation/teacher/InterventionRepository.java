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
public class InterventionRepository {
    private final JdbcTemplate jdbcTemplate;

    public InterventionRepository(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    public Optional<Intervention> findById(String interventionId) {
        return jdbcTemplate.query(selectSql() + " where intervention_id = ?", (rs, rowNum) -> map(rs), interventionId)
                .stream().findFirst();
    }

    public Optional<Intervention> findByIdempotencyKey(String idempotencyKey) {
        return jdbcTemplate.query(selectSql() + " where idempotency_key = ?", (rs, rowNum) -> map(rs), idempotencyKey)
                .stream().findFirst();
    }

    public Optional<Intervention> findByRecommendationId(String recommendationId) {
        return jdbcTemplate.query(selectSql() + " where recommendation_id = ?", (rs, rowNum) -> map(rs), recommendationId)
                .stream().findFirst();
    }

    public void insert(Intervention intervention) {
        jdbcTemplate.update("""
                insert into app.interventions
                    (intervention_id, recommendation_id, student_id, course_id, class_id, knowledge_point_id,
                     strategy_code, teacher_rationale, predicted_lift, prediction_low, prediction_high, status,
                     version, assignment_id, demo_run_id, demo_case_id, correlation_id, source_version,
                     idempotency_key, created_at)
                values (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)
                """, intervention.interventionId(), intervention.recommendationId(), intervention.studentId(),
                intervention.courseId(), intervention.classId(), intervention.knowledgePointId(), intervention.strategyCode(),
                intervention.teacherRationale(), intervention.predictedLift(), intervention.predictionLow(),
                intervention.predictionHigh(), intervention.status(), intervention.version(), intervention.assignmentId(),
                intervention.demoRunId(), intervention.demoCaseId(), intervention.correlationId(), intervention.sourceVersion(),
                intervention.idempotencyKey(), timestamp(intervention.createdAt()));
    }

    public int approve(String interventionId, int expectedVersion, String idempotencyKey, Instant approvedAt) {
        return jdbcTemplate.update("""
                update app.interventions
                   set status = 'APPROVED', version = version + 1, approve_idempotency_key = ?, approved_at = ?
                 where intervention_id = ? and version = ? and status = 'PROPOSED'
                   and approve_idempotency_key is null
                """, idempotencyKey, timestamp(approvedAt), interventionId, expectedVersion);
    }

    public int commit(String interventionId, int expectedVersion, String assignmentId, String idempotencyKey,
                      Instant committedAt) {
        return jdbcTemplate.update("""
                update app.interventions
                   set status = 'COMMITTED', version = version + 1, assignment_id = ?,
                       commit_idempotency_key = ?, committed_at = ?
                 where intervention_id = ? and version = ? and status = 'APPROVED'
                   and commit_idempotency_key is null
                """, assignmentId, idempotencyKey, timestamp(committedAt), interventionId, expectedVersion);
    }

    public void insertPracticeSet(String practiceSetId, Intervention intervention, Instant createdAt) {
        jdbcTemplate.update("""
                insert into app.practice_sets
                    (practice_set_id, student_id, course_id, class_id, coach_session_id, source, status,
                     demo_run_id, demo_case_id, correlation_id, source_version, created_at)
                values (?, ?, ?, ?, null, 'TEACHER_ASSIGNMENT', 'OPEN', ?, ?, ?, ?, ?)
                """, practiceSetId, intervention.studentId(), intervention.courseId(), intervention.classId(),
                intervention.demoRunId(), intervention.demoCaseId(), intervention.correlationId(),
                intervention.sourceVersion(), timestamp(createdAt));
    }

    public void insertAssignment(InterventionAssignment assignment) {
        jdbcTemplate.update("""
                insert into app.intervention_assignments
                    (assignment_id, intervention_id, practice_set_id, student_id, course_id, class_id,
                     knowledge_point_id, status, due_at, created_at, demo_run_id, demo_case_id,
                     correlation_id, source_version)
                values (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)
                """, assignment.assignmentId(), assignment.interventionId(), assignment.practiceSetId(),
                assignment.studentId(), assignment.courseId(), assignment.classId(), assignment.knowledgePointId(),
                assignment.status(), timestamp(assignment.dueAt()), timestamp(assignment.createdAt()), assignment.demoRunId(),
                assignment.demoCaseId(), assignment.correlationId(), assignment.sourceVersion());
    }

    public Optional<InterventionAssignment> findAssignment(String interventionId) {
        return jdbcTemplate.query("""
                select assignment_id, intervention_id, practice_set_id, student_id, course_id, class_id,
                       knowledge_point_id, status, due_at, created_at, demo_run_id, demo_case_id,
                       correlation_id, source_version
                from app.intervention_assignments where intervention_id = ?
                """, (rs, rowNum) -> new InterventionAssignment(rs.getString("assignment_id"),
                rs.getString("intervention_id"), rs.getString("practice_set_id"), rs.getString("student_id"),
                rs.getString("course_id"), rs.getString("class_id"), rs.getString("knowledge_point_id"),
                rs.getString("status"), nullableInstant(rs.getObject("due_at")), instant(rs.getObject("created_at")),
                rs.getString("demo_run_id"), rs.getString("demo_case_id"), rs.getString("correlation_id"),
                rs.getString("source_version")),
                interventionId).stream().findFirst();
    }

    public Optional<InterventionAssignment> findAssignmentByPracticeSet(String practiceSetId) {
        return jdbcTemplate.query("""
                select assignment_id, intervention_id, practice_set_id, student_id, course_id, class_id,
                       knowledge_point_id, status, due_at, created_at, demo_run_id, demo_case_id,
                       correlation_id, source_version
                from app.intervention_assignments where practice_set_id = ?
                """, (rs, rowNum) -> mapAssignment(rs), practiceSetId).stream().findFirst();
    }

    public Optional<InterventionAssignment> findCurrentAssignment(String studentId, String courseId) {
        return jdbcTemplate.query("""
                select a.assignment_id, a.intervention_id, a.practice_set_id, a.student_id, a.course_id,
                       a.class_id, a.knowledge_point_id, a.status, a.due_at, a.created_at, a.demo_run_id,
                       a.demo_case_id, a.correlation_id, a.source_version
                from app.intervention_assignments a
                join app.interventions i on i.intervention_id = a.intervention_id
                where a.student_id = ? and a.course_id = ?
                  and a.status in ('PENDING_STUDENT', 'IN_PROGRESS')
                  and i.status = 'COMMITTED'
                  and (i.demo_run_id is null or exists (
                      select 1 from app.demo_runs d where d.demo_run_id = i.demo_run_id and d.status = 'ACTIVE'
                  ))
                order by a.created_at desc, a.assignment_id desc limit 1
                """, (rs, rowNum) -> mapAssignment(rs), studentId, courseId).stream().findFirst();
    }

    public void captureBeforeSnapshot(String interventionId, BeforeSnapshot snapshot) {
        jdbcTemplate.update("""
                update app.interventions
                   set mastery_before = ?, confidence_before = ?, forgetting_risk_before = ?,
                       weakness_score_before = ?, evidence_count_before = ?, before_captured_at = ?
                 where intervention_id = ? and before_captured_at is null
                """, snapshot.mastery(), snapshot.confidence(), snapshot.forgettingRisk(), snapshot.weaknessScore(),
                snapshot.evidenceCount(), timestamp(snapshot.capturedAt()), interventionId);
    }

    private String selectSql() {
        return """
                select intervention_id, recommendation_id, student_id, course_id, class_id, knowledge_point_id,
                       strategy_code, teacher_rationale, predicted_lift, prediction_low, prediction_high, status,
                       version, assignment_id, demo_run_id, demo_case_id, correlation_id, source_version,
                       idempotency_key, approve_idempotency_key, commit_idempotency_key, created_at, approved_at, committed_at
                from app.interventions
                """;
    }

    private InterventionAssignment mapAssignment(ResultSet rs) throws SQLException {
        return new InterventionAssignment(rs.getString("assignment_id"), rs.getString("intervention_id"),
                rs.getString("practice_set_id"), rs.getString("student_id"), rs.getString("course_id"),
                rs.getString("class_id"), rs.getString("knowledge_point_id"), rs.getString("status"),
                nullableInstant(rs.getObject("due_at")), instant(rs.getObject("created_at")),
                rs.getString("demo_run_id"), rs.getString("demo_case_id"), rs.getString("correlation_id"),
                rs.getString("source_version"));
    }

    private Intervention map(ResultSet rs) throws SQLException {
        return new Intervention(rs.getString("intervention_id"), rs.getString("recommendation_id"),
                rs.getString("student_id"), rs.getString("course_id"), rs.getString("class_id"),
                rs.getString("knowledge_point_id"), rs.getString("strategy_code"), rs.getString("teacher_rationale"),
                rs.getDouble("predicted_lift"), rs.getDouble("prediction_low"), rs.getDouble("prediction_high"),
                rs.getString("status"), rs.getInt("version"), rs.getString("assignment_id"), rs.getString("demo_run_id"),
                rs.getString("demo_case_id"), rs.getString("correlation_id"), rs.getString("source_version"),
                rs.getString("idempotency_key"), rs.getString("approve_idempotency_key"), rs.getString("commit_idempotency_key"),
                instant(rs.getObject("created_at")), nullableInstant(rs.getObject("approved_at")),
                nullableInstant(rs.getObject("committed_at")));
    }

    private OffsetDateTime timestamp(Instant instant) {
        return instant == null ? null : instant.atOffset(ZoneOffset.UTC);
    }

    private Instant nullableInstant(Object value) {
        return value == null ? null : instant(value);
    }

    private Instant instant(Object value) {
        if (value instanceof OffsetDateTime dateTime) return dateTime.toInstant();
        if (value instanceof java.sql.Timestamp timestamp) return timestamp.toInstant();
        if (value instanceof Instant instant) return instant;
        throw new IllegalArgumentException("Unsupported timestamp value: " + value);
    }

    public record BeforeSnapshot(double mastery, double confidence, double forgettingRisk,
                                 Double weaknessScore, int evidenceCount, Instant capturedAt) {
    }
}
