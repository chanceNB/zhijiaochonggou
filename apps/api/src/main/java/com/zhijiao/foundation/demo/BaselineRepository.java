package com.zhijiao.foundation.demo;

import com.zhijiao.foundation.analytics.DomainEventOutboxRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.Instant;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@Repository
public class BaselineRepository {
    private final JdbcTemplate jdbcTemplate;
    private final DomainEventOutboxRepository outbox;

    @Autowired
    public BaselineRepository(JdbcTemplate jdbcTemplate, DomainEventOutboxRepository outbox) {
        this.jdbcTemplate = jdbcTemplate;
        this.outbox = outbox;
    }

    public BaselineRepository(JdbcTemplate jdbcTemplate) {
        this(jdbcTemplate, null);
    }

    public Optional<String> findBaselineVersion(String baselineVersion) {
        return jdbcTemplate.query(
                "select baseline_version from app.baseline_metadata where baseline_version = ?",
                rs -> rs.next() ? Optional.of(rs.getString(1)) : Optional.empty(),
                baselineVersion);
    }

    public void insertBaselineMetadata(String baselineVersion, java.sql.Date referenceDate,
                                       String dataOrigin, String sourceVersion, Instant createdAt) {
        int inserted = jdbcTemplate.update("""
                insert into app.baseline_metadata
                    (baseline_version, reference_date, data_origin, source_version, created_at)
                select ?, ?, ?, ?, ?
                where not exists (select 1 from app.baseline_metadata where baseline_version = ?)
                """, baselineVersion, referenceDate, dataOrigin, sourceVersion, timestamp(createdAt), baselineVersion);
        emit(inserted, "BaselineMetadata", baselineVersion, "BASELINE_SEEDED", createdAt, sourceVersion, dataOrigin, null, null, null);
    }

    public void insertCourse(String courseId, String name, String baselineVersion,
                             String dataOrigin, String sourceVersion, Instant createdAt) {
        int inserted = jdbcTemplate.update("""
                insert into app.courses
                    (course_id, name, baseline_version, data_origin, source_version, created_at)
                select ?, ?, ?, ?, ?, ?
                where not exists (select 1 from app.courses where course_id = ?)
                """, courseId, name, baselineVersion, dataOrigin, sourceVersion, timestamp(createdAt), courseId);
        emit(inserted, "Course", courseId, "COURSE_PUBLISHED", createdAt, sourceVersion, dataOrigin, null, null, null);
    }

    public void insertClassroom(String classId, String courseId, String name, String baselineVersion,
                                String dataOrigin, String sourceVersion, Instant createdAt) {
        int inserted = jdbcTemplate.update("""
                insert into app.classrooms
                    (class_id, course_id, name, baseline_version, data_origin, source_version, created_at)
                select ?, ?, ?, ?, ?, ?, ?
                where not exists (select 1 from app.classrooms where class_id = ?)
                """, classId, courseId, name, baselineVersion, dataOrigin, sourceVersion, timestamp(createdAt), classId);
        emit(inserted, "Class", classId, "CLASS_PUBLISHED", createdAt, sourceVersion, dataOrigin, null, null, null);
    }

    public void insertStudent(String studentId, String classId, String courseId, String displayName,
                              String baselineVersion, String dataOrigin, String sourceVersion, Instant createdAt) {
        int inserted = jdbcTemplate.update("""
                insert into app.students
                    (student_id, class_id, course_id, display_name, baseline_version, data_origin, source_version, created_at)
                select ?, ?, ?, ?, ?, ?, ?, ?
                where not exists (select 1 from app.students where student_id = ?)
                """, studentId, classId, courseId, displayName, baselineVersion, dataOrigin, sourceVersion,
                timestamp(createdAt), studentId);
        emit(inserted, "Student", studentId, "STUDENT_PUBLISHED", createdAt, sourceVersion, dataOrigin, null, null, null);
    }

    public void insertKnowledgePoint(String knowledgePointId, String courseId, String parentId, String name,
                                     int sortOrder, String baselineVersion, String dataOrigin,
                                     String sourceVersion, Instant createdAt) {
        int inserted = jdbcTemplate.update("""
                insert into app.knowledge_points
                    (knowledge_point_id, course_id, parent_id, name, sort_order, baseline_version,
                     data_origin, source_version, created_at)
                select ?, ?, ?, ?, ?, ?, ?, ?, ?
                where not exists (select 1 from app.knowledge_points where knowledge_point_id = ?)
                """, knowledgePointId, courseId, parentId, name, sortOrder, baselineVersion,
                dataOrigin, sourceVersion, timestamp(createdAt), knowledgePointId);
        emit(inserted, "KnowledgePoint", knowledgePointId, "KNOWLEDGE_POINT_PUBLISHED", createdAt, sourceVersion, dataOrigin, null, null, null);
    }

    public void insertLearningEvent(String eventId, Instant eventTime, String studentId, String courseId,
                                    String classId, String knowledgePointId, String eventType, Boolean correct,
                                    BigDecimal masteryAfter, BigDecimal confidence, BigDecimal forgettingRisk,
                                    String misconceptionCode, String dataOrigin, String sourceVersion,
                                    String baselineVersion, String demoRunId, String demoCaseId,
                                    String correlationId, Instant ingestedAt) {
        int inserted = jdbcTemplate.update("""
                insert into app.learning_events
                    (event_id, event_time, student_id, course_id, class_id, knowledge_point_id,
                     event_type, correct, mastery_after, confidence, forgetting_risk, misconception_code,
                     data_origin, source_version, baseline_version, demo_run_id, demo_case_id,
                     correlation_id, ingested_at)
                select ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?
                where not exists (select 1 from app.learning_events where event_id = ?)
                """, eventId, timestamp(eventTime), studentId, courseId, classId, knowledgePointId, eventType,
                correct, masteryAfter, confidence, forgettingRisk, misconceptionCode, dataOrigin,
                sourceVersion, baselineVersion, demoRunId, demoCaseId, correlationId, timestamp(ingestedAt), eventId);
        emit(inserted, "LearningEvent", eventId, "LEARNING_EVENT_RECORDED", eventTime, sourceVersion, dataOrigin, demoRunId, demoCaseId, correlationId);
    }

    public int insertQuestionItem(String questionId, String courseId, String knowledgePointId,
                                  String questionSource, String difficulty, BigDecimal itemDifficulty,
                                  String baselineVersion, String dataOrigin, String sourceVersion,
                                  Instant createdAt) {
        int inserted = jdbcTemplate.update("""
                insert into app.question_items
                    (question_id, course_id, knowledge_point_id, question_source, difficulty,
                     item_difficulty, baseline_version, data_origin, source_version, created_at)
                select ?, ?, ?, ?, ?, ?, ?, ?, ?, ?
                where not exists (select 1 from app.question_items where question_id = ?)
                """, questionId, courseId, knowledgePointId, questionSource, difficulty, itemDifficulty,
                baselineVersion, dataOrigin, sourceVersion, timestamp(createdAt), questionId);
        emit(inserted, "QuestionItem", questionId, "QUESTION_PUBLISHED", createdAt, sourceVersion, dataOrigin, null, null, null);
        return inserted;
    }

    public void insertPracticeAttempt(String attemptId, Instant attemptTime, String studentId, String courseId,
                                      String classId, String knowledgePointId, String questionId,
                                      String questionSource, String difficulty, boolean correct,
                                      int durationSeconds, int responseTimeMs, int attemptIndex,
                                      String misconceptionCode, String dataOrigin,
                                      String sourceVersion, String baselineVersion, String demoRunId,
                                      String demoCaseId, String correlationId, Instant ingestedAt) {
        int inserted = jdbcTemplate.update("""
                insert into app.practice_attempts
                    (attempt_id, attempt_time, student_id, course_id, class_id, knowledge_point_id,
                     question_id, question_source, difficulty, correct, duration_seconds, response_time_ms,
                     attempt_index, misconception_code,
                     data_origin, source_version, baseline_version, demo_run_id, demo_case_id,
                     correlation_id, ingested_at)
                select ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?
                where not exists (select 1 from app.practice_attempts where attempt_id = ?)
                """, attemptId, timestamp(attemptTime), studentId, courseId, classId, knowledgePointId, questionId,
                questionSource, difficulty, correct, durationSeconds, responseTimeMs, attemptIndex, misconceptionCode, dataOrigin,
                sourceVersion, baselineVersion, demoRunId, demoCaseId, correlationId, timestamp(ingestedAt), attemptId);
        emit(inserted, "PracticeAttempt", attemptId, "PRACTICE_ATTEMPT_RECORDED", attemptTime, sourceVersion, dataOrigin, demoRunId, demoCaseId, correlationId);
    }

    public Optional<StudentSeedContext> findStudent(String studentId) {
        List<StudentSeedContext> rows = jdbcTemplate.query("""
                select student_id, class_id, course_id
                from app.students where student_id = ?
                """, (rs, rowNum) -> new StudentSeedContext(
                rs.getString("student_id"), rs.getString("class_id"), rs.getString("course_id")), studentId);
        return rows.stream().findFirst();
    }

    public void insertDemoRun(DemoRun run) {
        int inserted = jdbcTemplate.update("""
                insert into app.demo_runs
                    (demo_run_id, demo_case_id, baseline_version, student_id, course_id, class_id,
                     status, stage, correlation_id, created_at, reset_at, reset_from_demo_run_id)
                values (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)
                """, run.demoRunId(), run.demoCaseId(), run.baselineVersion(), run.studentId(), run.courseId(),
                run.classId(), run.status(), run.stage(), run.correlationId(), timestamp(run.createdAt()), timestamp(run.resetAt()),
                run.resetFromDemoRunId());
        emit(inserted, "DemoRun", run.demoRunId(), "DEMO_RUN_STARTED", run.createdAt(), run.baselineVersion(), "LIVE_DEMO", run.demoRunId(), run.demoCaseId(), run.correlationId());
    }

    public Optional<DemoRun> findDemoRun(String demoRunId) {
        List<DemoRun> rows = jdbcTemplate.query("""
                select demo_run_id, demo_case_id, baseline_version, student_id, course_id, class_id,
                       status, stage, correlation_id, created_at, reset_at, reset_from_demo_run_id
                from app.demo_runs where demo_run_id = ?
                """, (rs, rowNum) -> mapDemoRun(rs), demoRunId);
        return rows.stream().findFirst();
    }

    public void markReset(String demoRunId, Instant resetAt) {
        int updated = jdbcTemplate.update("update app.demo_runs set status = 'RESET', reset_at = ? where demo_run_id = ?",
                timestamp(resetAt), demoRunId);
        emit(updated, "DemoRun", demoRunId, "DEMO_RUN_RESET", resetAt, "baseline-ds-v1", "LIVE_DEMO", demoRunId, null, null);
    }

    public List<DemoTraceEvent> findTraceEvents(String demoRunId) {
        return jdbcTemplate.query("""
                select event_time, event_type, event_id from app.learning_events
                where demo_run_id = ?
                union all
                select attempt_time, 'PRACTICE_ATTEMPT', attempt_id from app.practice_attempts
                where demo_run_id = ?
                order by event_time
                """, (rs, rowNum) -> new DemoTraceEvent(
                rs.getString("event_type"), rs.getString("event_id"), rs.getTimestamp("event_time").toInstant()),
                demoRunId, demoRunId);
    }

    private DemoRun mapDemoRun(ResultSet rs) throws SQLException {
        return new DemoRun(
                rs.getString("demo_run_id"),
                rs.getString("demo_case_id"),
                rs.getString("baseline_version"),
                rs.getString("student_id"),
                rs.getString("course_id"),
                rs.getString("class_id"),
                rs.getString("status"),
                rs.getString("stage"),
                rs.getString("correlation_id"),
                rs.getTimestamp("created_at").toInstant(),
                rs.getTimestamp("reset_at") == null ? null : rs.getTimestamp("reset_at").toInstant(),
                rs.getString("reset_from_demo_run_id")
        );
    }

    private OffsetDateTime timestamp(Instant instant) {
        return instant == null ? null : instant.atOffset(ZoneOffset.UTC);
    }

    private void emit(int changed, String aggregateType, String aggregateId, String eventType, Instant occurredAt,
                      String sourceVersion, String dataOrigin, String demoRunId, String demoCaseId, String correlationId) {
        if (changed > 0 && outbox != null) {
            outbox.append(aggregateType, aggregateId, eventType, occurredAt, sourceVersion, dataOrigin,
                    demoRunId, demoCaseId, correlationId);
        }
    }

    public record StudentSeedContext(String studentId, String classId, String courseId) {
    }
}
