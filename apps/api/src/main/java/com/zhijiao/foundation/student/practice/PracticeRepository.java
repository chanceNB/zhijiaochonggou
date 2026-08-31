package com.zhijiao.foundation.student.practice;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.zhijiao.foundation.analytics.DomainEventOutboxRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.Instant;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Repository
public class PracticeRepository {
    private final JdbcTemplate jdbcTemplate;
    private final ObjectMapper objectMapper;
    private final DomainEventOutboxRepository outbox;

    @Autowired
    public PracticeRepository(JdbcTemplate jdbcTemplate, ObjectMapper objectMapper, DomainEventOutboxRepository outbox) {
        this.jdbcTemplate = jdbcTemplate;
        this.objectMapper = objectMapper;
        this.outbox = outbox;
    }

    public PracticeRepository(JdbcTemplate jdbcTemplate, ObjectMapper objectMapper) {
        this(jdbcTemplate, objectMapper, null);
    }

    public Optional<PracticeSet> findSet(String practiceSetId) {
        return jdbcTemplate.query("""
                select practice_set_id, student_id, course_id, class_id, coach_session_id, source, status,
                       demo_run_id, demo_case_id, correlation_id, source_version, created_at, completed_at
                from app.practice_sets where practice_set_id = ?
                """, (rs, rowNum) -> mapSet(rs), practiceSetId).stream().findFirst();
    }

    public int questionCount(String practiceSetId) {
        Integer count = jdbcTemplate.queryForObject(
                "select count(*) from app.practice_questions where practice_set_id = ?", Integer.class, practiceSetId);
        return count == null ? 0 : count;
    }

    public java.util.Map<String, String> validationRoles(String practiceSetId) {
        return jdbcTemplate.query("""
                select question_id, validation_role from app.practice_questions
                where practice_set_id = ?
                """, (rs, rowNum) -> java.util.Map.entry(rs.getString("question_id"), rs.getString("validation_role")),
                practiceSetId).stream().collect(java.util.stream.Collectors.toMap(java.util.Map.Entry::getKey,
                java.util.Map.Entry::getValue));
    }

    public int countTransferAttempts(String practiceSetId) {
        Integer count = jdbcTemplate.queryForObject("""
                select count(*) from app.practice_attempts a
                join app.practice_questions q on q.practice_set_id = a.practice_set_id
                    and q.question_id = a.question_id
                where a.practice_set_id = ? and q.validation_role = 'TRANSFER'
                """, Integer.class, practiceSetId);
        return count == null ? 0 : count;
    }

    public int countCorrectTransferAttempts(String practiceSetId) {
        Integer count = jdbcTemplate.queryForObject("""
                select count(*) from app.practice_attempts a
                join app.practice_questions q on q.practice_set_id = a.practice_set_id
                    and q.question_id = a.question_id
                where a.practice_set_id = ? and q.validation_role = 'TRANSFER' and a.correct = true
                """, Integer.class, practiceSetId);
        return count == null ? 0 : count;
    }

    public void bindAttemptToIntervention(String attemptId, String practiceSetId) {
        jdbcTemplate.update("""
                update app.practice_attempts a
                   set intervention_id = ia.intervention_id
                  from app.intervention_assignments ia
                 where a.attempt_id = ? and a.practice_set_id = ?
                   and ia.practice_set_id = ?
                """, attemptId, practiceSetId, practiceSetId);
    }

    public List<InternalQuestion> findQuestions(String practiceSetId) {
        return jdbcTemplate.query("""
                select practice_set_id, question_id, parent_question_id, knowledge_point_id, question_type,
                       stem, options, correct_answer, explanation, difficulty, created_at
                from app.practice_questions where practice_set_id = ? order by question_id
                """, (rs, rowNum) -> mapQuestion(rs), practiceSetId);
    }

    public Optional<InternalQuestion> findQuestion(String practiceSetId, String questionId) {
        return findQuestions(practiceSetId).stream()
                .filter(question -> question.questionId().equals(questionId)).findFirst();
    }

    public Optional<InternalQuestion> findQuestionById(String questionId) {
        return jdbcTemplate.query("""
                select practice_set_id, question_id, parent_question_id, knowledge_point_id, question_type,
                       stem, options, correct_answer, explanation, difficulty, created_at
                from app.practice_questions where question_id = ?
                """, (rs, rowNum) -> mapQuestion(rs), questionId).stream().findFirst();
    }

    public Optional<AttemptRow> findAttemptById(String attemptId) {
        return jdbcTemplate.query("""
                select attempt_id, practice_set_id, attempt_time, student_id, course_id, class_id,
                       knowledge_point_id, question_id, question_source, difficulty, correct, duration_seconds,
                       response_time_ms, attempt_index, selected_answer, data_origin, source_version,
                       baseline_version, demo_run_id, demo_case_id, correlation_id, coach_session_id
                from app.practice_attempts where attempt_id = ?
                """, (rs, rowNum) -> mapAttempt(rs), attemptId).stream().findFirst();
    }

    public Optional<AttemptRow> findAttemptByIdempotency(String practiceSetId, String questionId, String key) {
        if (key == null || key.isBlank()) return Optional.empty();
        return jdbcTemplate.query("""
                select attempt_id, practice_set_id, attempt_time, student_id, course_id, class_id,
                       knowledge_point_id, question_id, question_source, difficulty, correct, duration_seconds,
                       response_time_ms, attempt_index, selected_answer, data_origin, source_version,
                       baseline_version, demo_run_id, demo_case_id, correlation_id, coach_session_id
                from app.practice_attempts where practice_set_id = ? and question_id = ? and idempotency_key = ?
                """, (rs, rowNum) -> mapAttempt(rs), practiceSetId, questionId, key).stream().findFirst();
    }

    public int nextAttemptIndex(String studentId, String courseId, String knowledgePointId) {
        Integer max = jdbcTemplate.queryForObject("""
                select coalesce(max(attempt_index), 0) from app.practice_attempts
                where student_id = ? and course_id = ? and knowledge_point_id = ?
                """, Integer.class, studentId, courseId, knowledgePointId);
        return (max == null ? 0 : max) + 1;
    }

    public AttemptRow insertAttempt(AttemptRow attempt) {
        int inserted = jdbcTemplate.update("""
                insert into app.practice_attempts
                    (attempt_id, practice_set_id, attempt_time, student_id, course_id, class_id,
                     knowledge_point_id, question_id, question_source, difficulty, correct, duration_seconds,
                     response_time_ms, attempt_index, selected_answer, data_origin, source_version,
                     baseline_version, demo_run_id, demo_case_id, correlation_id, ingested_at, coach_session_id,
                     idempotency_key)
                values (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)
                on conflict do nothing
                """, attempt.attemptId(), attempt.practiceSetId(), timestamp(attempt.attemptTime()), attempt.studentId(),
                attempt.courseId(), attempt.classId(), attempt.knowledgePointId(), attempt.questionId(), attempt.questionSource(),
                attempt.difficulty(), attempt.correct(), attempt.durationSeconds(), attempt.responseTimeMs(), attempt.attemptIndex(),
                attempt.selectedAnswer(), attempt.dataOrigin(), attempt.sourceVersion(), attempt.baselineVersion(), attempt.demoRunId(),
                attempt.demoCaseId(), attempt.correlationId(), timestamp(attempt.attemptTime()), attempt.coachSessionId(), attempt.idempotencyKey());
        emit(inserted, "PracticeAttempt", attempt.attemptId(), "PRACTICE_ATTEMPT_RECORDED", attempt.attemptTime(),
                attempt.sourceVersion(), attempt.dataOrigin(), attempt.demoRunId(), attempt.demoCaseId(), attempt.correlationId());
        return attempt;
    }

    public List<AttemptRow> findAttempts(String practiceSetId) {
        return jdbcTemplate.query("""
                select attempt_id, practice_set_id, attempt_time, student_id, course_id, class_id,
                       knowledge_point_id, question_id, question_source, difficulty, correct, duration_seconds,
                       response_time_ms, attempt_index, selected_answer, data_origin, source_version,
                       baseline_version, demo_run_id, demo_case_id, correlation_id, coach_session_id
                from app.practice_attempts where practice_set_id = ? order by attempt_time, attempt_id
                """, (rs, rowNum) -> mapAttempt(rs), practiceSetId);
    }

    public void markCompleted(String practiceSetId, Instant completedAt) {
        int updated = jdbcTemplate.update("update app.practice_sets set status = 'COMPLETED', completed_at = ? where practice_set_id = ?",
                timestamp(completedAt), practiceSetId);
        emit(updated, "PracticeSet", practiceSetId, "PRACTICE_SET_COMPLETED", completedAt, "unknown", "LIVE_DEMO", null, null, null);
    }

    public void markAssignmentInProgress(String practiceSetId) {
        jdbcTemplate.update("""
                update app.intervention_assignments
                   set status = 'IN_PROGRESS'
                 where practice_set_id = ? and status = 'PENDING_STUDENT'
                """, practiceSetId);
    }

    public void markAssignmentCompleted(String practiceSetId) {
        jdbcTemplate.update("""
                update app.intervention_assignments
                   set status = 'COMPLETED'
                 where practice_set_id = ? and status <> 'COMPLETED'
                """, practiceSetId);
    }

    public PracticeOutcome insertOutcome(PracticeOutcomeData outcome) {
        int inserted = jdbcTemplate.update("""
                insert into app.practice_outcomes
                    (outcome_id, practice_set_id, student_id, course_id, accuracy, attempt_count,
                     data_origin, demo_run_id, demo_case_id, correlation_id, source_version, created_at)
                values (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)
                on conflict do nothing
                """, outcome.outcomeId(), outcome.practiceSetId(), outcome.studentId(), outcome.courseId(), outcome.accuracy(),
                outcome.attemptCount(), outcome.dataOrigin(), outcome.demoRunId(), outcome.demoCaseId(), outcome.correlationId(),
                outcome.sourceVersion(), timestamp(outcome.createdAt()));
        emit(inserted, "PracticeOutcome", outcome.outcomeId(), "PRACTICE_OUTCOME_RECORDED", outcome.createdAt(),
                outcome.sourceVersion(), outcome.dataOrigin(), outcome.demoRunId(), outcome.demoCaseId(), outcome.correlationId());
        if (inserted == 1) {
            return new PracticeOutcome(outcome.outcomeId(), outcome.practiceSetId(), outcome.accuracy(), outcome.attemptCount(),
                    outcome.learningStateStatus());
        }
        return findOutcome(outcome.practiceSetId()).orElse(new PracticeOutcome(outcome.outcomeId(), outcome.practiceSetId(),
                outcome.accuracy(), outcome.attemptCount(), outcome.learningStateStatus()));
    }

    public Optional<PracticeOutcome> findOutcome(String practiceSetId) {
        return jdbcTemplate.query("select outcome_id, practice_set_id, accuracy, attempt_count from app.practice_outcomes where practice_set_id = ?",
                (rs, rowNum) -> new PracticeOutcome(rs.getString("outcome_id"), rs.getString("practice_set_id"),
                        rs.getDouble("accuracy"), rs.getInt("attempt_count"), "COMPLETED"), practiceSetId).stream().findFirst();
    }

    public Optional<DemoContext> findActiveDemo(String studentId, String courseId) {
        return jdbcTemplate.query("""
                select demo_run_id, demo_case_id, correlation_id, baseline_version
                from app.demo_runs where student_id = ? and course_id = ? and status = 'ACTIVE'
                order by created_at desc limit 1
                """, (rs, rowNum) -> new DemoContext(rs.getString("demo_run_id"), rs.getString("demo_case_id"),
                rs.getString("correlation_id"), rs.getString("baseline_version")), studentId, courseId).stream().findFirst();
    }

    public void bindDemoContext(String practiceSetId, DemoContext demo) {
        jdbcTemplate.update("""
                update app.practice_sets
                   set demo_run_id = ?, demo_case_id = ?, correlation_id = ?
                 where practice_set_id = ? and demo_run_id is null
                """, demo.demoRunId(), demo.demoCaseId(), demo.correlationId(), practiceSetId);
    }

    public void insertPracticeSet(PracticeSet set, List<InternalQuestion> questions, List<String> citations) {
        jdbcTemplate.update("""
                insert into app.practice_sets
                    (practice_set_id, student_id, course_id, class_id, coach_session_id, source, status,
                     demo_run_id, demo_case_id, correlation_id, source_version, created_at)
                values (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)
                """, set.practiceSetId(), set.studentId(), set.courseId(), set.classId(), set.coachSessionId(), set.source(),
                set.status(), set.demoRunId(), set.demoCaseId(), set.correlationId(), set.sourceVersion(), timestamp(set.createdAt()));
        for (InternalQuestion question : questions) {
            jdbcTemplate.update("""
                    insert into app.practice_questions
                        (practice_set_id, question_id, parent_question_id, knowledge_point_id, question_type,
                         stem, options, correct_answer, explanation, difficulty, generation_reason,
                         validation_role, source_version, created_at)
                    values (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)
                    """, set.practiceSetId(), question.questionId(), question.parentQuestionId(), question.knowledgePointId(),
                    question.questionType(), question.stem(), write(question.options()), question.correctAnswer(), question.explanation(),
                    question.difficulty(), set.source().equals("AI_COACH_SIMILAR") ? "SIMILAR_PRACTICE" : "ACTIVE_DIAGNOSIS",
                    "DIAGNOSTIC", set.sourceVersion(), timestamp(question.createdAt()));
        }
    }

    public void insertGeneratedPracticeSet(PracticeSet set, List<GeneratedQuestion> questions) {
        jdbcTemplate.update("""
                insert into app.practice_sets
                    (practice_set_id, student_id, course_id, class_id, coach_session_id, source, status,
                     demo_run_id, demo_case_id, correlation_id, source_version, created_at)
                values (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)
                """, set.practiceSetId(), set.studentId(), set.courseId(), set.classId(), set.coachSessionId(), set.source(),
                set.status(), set.demoRunId(), set.demoCaseId(), set.correlationId(), set.sourceVersion(), timestamp(set.createdAt()));
        for (GeneratedQuestion question : questions) {
            jdbcTemplate.update("""
                    insert into app.practice_questions
                        (practice_set_id, question_id, parent_question_id, knowledge_point_id, question_type,
                         stem, options, correct_answer, explanation, diagnostic_target, difficulty,
                         validation_role, generation_reason, model_provider, model_version, prompt_version, citations,
                         source_version, created_at)
                    values (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, 'DIAGNOSTIC', 'SIMILAR_PRACTICE', ?, ?, ?, ?, ?, ?)
                    """, set.practiceSetId(), question.questionId(), question.parentQuestionId(), question.knowledgePointId(),
                    question.questionType(), question.stem(), write(question.options()), question.correctAnswer(), question.explanation(),
                    write(question.diagnosticTarget()), question.difficulty(), question.modelProvider(), question.modelVersion(),
                    question.promptVersion(), write(question.citations()), set.sourceVersion(), timestamp(set.createdAt()));
        }
    }

    public Optional<WrongBookItem> findWrongBookBySourceAttempt(String studentId, String attemptId) {
        return jdbcTemplate.query("""
                select w.wrong_item_id, w.student_id, w.course_id, w.class_id, w.question_id, w.source_attempt_id,
                       w.knowledge_point_id, w.reason, w.status, w.review_count, w.added_at, w.repaired_at,
                       w.data_origin, w.demo_run_id, w.demo_case_id, w.correlation_id, w.source_version,
                       k.name as knowledge_point_name, q.stem as question_summary,
                       cast(null as text) as reason_display_name
                from app.wrong_book_items w
                left join app.practice_attempts a on a.attempt_id = w.source_attempt_id
                left join app.practice_questions q on q.practice_set_id = a.practice_set_id
                    and q.question_id = w.question_id
                left join app.knowledge_points k on k.knowledge_point_id = w.knowledge_point_id
                where w.student_id = ? and w.source_attempt_id = ?
                """, (rs, rowNum) -> mapWrongBook(rs), studentId, attemptId).stream().findFirst();
    }

    public WrongBookItem insertWrongBook(WrongBookItem item) {
        int inserted = jdbcTemplate.update("""
                insert into app.wrong_book_items
                    (wrong_item_id, student_id, course_id, class_id, question_id, source_attempt_id,
                     knowledge_point_id, reason, status, review_count, added_at, repaired_at, data_origin,
                     demo_run_id, demo_case_id, correlation_id, source_version)
                values (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)
                on conflict do nothing
                """, item.wrongItemId(), item.studentId(), item.courseId(), item.classId(), item.questionId(), item.sourceAttemptId(),
                item.knowledgePointId(), item.reason(), item.status(), item.reviewCount(), timestamp(item.addedAt()),
                item.repairedAt() == null ? null : timestamp(item.repairedAt()), item.dataOrigin(), item.demoRunId(), item.demoCaseId(),
                item.correlationId(), item.sourceVersion());
        emit(inserted, "WrongBookItem", item.wrongItemId(), "WRONG_BOOK_ADDED", item.addedAt(), item.sourceVersion(),
                item.dataOrigin(), item.demoRunId(), item.demoCaseId(), item.correlationId());
        return item;
    }

    public List<WrongBookItem> findWrongBook(String studentId, String knowledgePointId, String status, int page, int size) {
        StringBuilder sql = new StringBuilder("""
                select w.wrong_item_id, w.student_id, w.course_id, w.class_id, w.question_id, w.source_attempt_id,
                       w.knowledge_point_id, w.reason, w.status, w.review_count, w.added_at, w.repaired_at,
                       w.data_origin, w.demo_run_id, w.demo_case_id, w.correlation_id, w.source_version,
                       k.name as knowledge_point_name, q.stem as question_summary,
                       cast(null as text) as reason_display_name
                from app.wrong_book_items w
                left join app.practice_attempts a on a.attempt_id = w.source_attempt_id
                left join app.practice_questions q on q.practice_set_id = a.practice_set_id
                    and q.question_id = w.question_id
                left join app.knowledge_points k on k.knowledge_point_id = w.knowledge_point_id
                where w.student_id = ?
                  and (
                      w.data_origin <> 'LIVE_DEMO'
                      or exists (
                          select 1 from app.demo_runs active_demo
                          where active_demo.demo_run_id = w.demo_run_id
                            and active_demo.student_id = ?
                            and active_demo.course_id = w.course_id
                            and active_demo.status = 'ACTIVE'
                      )
                  )
                """);
        List<Object> args = new ArrayList<>();
        args.add(studentId);
        args.add(studentId);
        if (knowledgePointId != null && !knowledgePointId.isBlank()) { sql.append(" and w.knowledge_point_id = ?"); args.add(knowledgePointId); }
        if (status != null && !status.isBlank()) { sql.append(" and w.status = ?"); args.add(status); }
        sql.append(" order by w.added_at desc, w.wrong_item_id limit ? offset ?");
        args.add(size); args.add(Math.max(0, page - 1) * size);
        return jdbcTemplate.query(sql.toString(), (rs, rowNum) -> mapWrongBook(rs), args.toArray());
    }

    public long countWrongBook(String studentId, String knowledgePointId, String status) {
        StringBuilder sql = new StringBuilder("""
                select count(*) from app.wrong_book_items
                where student_id = ?
                  and (
                      data_origin <> 'LIVE_DEMO'
                      or exists (
                          select 1 from app.demo_runs active_demo
                          where active_demo.demo_run_id = wrong_book_items.demo_run_id
                            and active_demo.student_id = ?
                            and active_demo.course_id = wrong_book_items.course_id
                            and active_demo.status = 'ACTIVE'
                      )
                  )
                """);
        List<Object> args = new ArrayList<>();
        args.add(studentId);
        args.add(studentId);
        if (knowledgePointId != null && !knowledgePointId.isBlank()) { sql.append(" and knowledge_point_id = ?"); args.add(knowledgePointId); }
        if (status != null && !status.isBlank()) { sql.append(" and status = ?"); args.add(status); }
        Long count = jdbcTemplate.queryForObject(sql.toString(), Long.class, args.toArray());
        return count == null ? 0 : count;
    }

    public Optional<WrongBookItem> findWrongBook(String studentId, String wrongItemId) {
        return jdbcTemplate.query("""
                select w.wrong_item_id, w.student_id, w.course_id, w.class_id, w.question_id, w.source_attempt_id,
                       w.knowledge_point_id, w.reason, w.status, w.review_count, w.added_at, w.repaired_at,
                       w.data_origin, w.demo_run_id, w.demo_case_id, w.correlation_id, w.source_version,
                       k.name as knowledge_point_name, q.stem as question_summary,
                       cast(null as text) as reason_display_name
                from app.wrong_book_items w
                left join app.practice_attempts a on a.attempt_id = w.source_attempt_id
                left join app.practice_questions q on q.practice_set_id = a.practice_set_id
                    and q.question_id = w.question_id
                left join app.knowledge_points k on k.knowledge_point_id = w.knowledge_point_id
                where w.student_id = ? and w.wrong_item_id = ?
                  and (
                      w.data_origin <> 'LIVE_DEMO'
                      or exists (
                          select 1 from app.demo_runs active_demo
                          where active_demo.demo_run_id = w.demo_run_id
                            and active_demo.student_id = ?
                            and active_demo.course_id = w.course_id
                            and active_demo.status = 'ACTIVE'
                      )
                  )
                """, (rs, rowNum) -> mapWrongBook(rs), studentId, wrongItemId, studentId).stream().findFirst();
    }

    public WrongBookItem updateWrongBookReview(WrongBookItem item, boolean correct, Instant reviewedAt) {
        String status = correct ? (item.reviewCount() + 1 >= 1 ? "LEARNING" : "TO_REVIEW") : "TO_REVIEW";
        Instant repairedAt = correct ? reviewedAt : item.repairedAt();
        int updated = jdbcTemplate.update("update app.wrong_book_items set status = ?, review_count = ?, repaired_at = ? where wrong_item_id = ?",
                status, item.reviewCount() + 1, repairedAt == null ? null : timestamp(repairedAt), item.wrongItemId());
        emit(updated, "WrongBookItem", item.wrongItemId(), "WRONG_BOOK_REVIEWED", reviewedAt, item.sourceVersion(),
                item.dataOrigin(), item.demoRunId(), item.demoCaseId(), item.correlationId());
        return new WrongBookItem(item.wrongItemId(), item.studentId(), item.courseId(), item.classId(), item.questionId(),
                item.sourceAttemptId(), item.knowledgePointId(), item.reason(), status, item.reviewCount() + 1,
                item.addedAt(), repairedAt, item.dataOrigin(), item.demoRunId(), item.demoCaseId(), item.correlationId(), item.sourceVersion(),
                item.knowledgePointName(), item.questionSummary(), item.reasonDisplayName());
    }

    private PracticeSet mapSet(ResultSet rs) throws SQLException {
        return new PracticeSet(rs.getString("practice_set_id"), rs.getString("student_id"), rs.getString("course_id"),
                rs.getString("class_id"), rs.getString("coach_session_id"), rs.getString("source"), rs.getString("status"),
                rs.getString("demo_run_id"), rs.getString("demo_case_id"), rs.getString("correlation_id"),
                rs.getString("source_version"), toInstant(rs.getObject("created_at")), nullableInstant(rs.getObject("completed_at")));
    }

    private InternalQuestion mapQuestion(ResultSet rs) throws SQLException {
        return new InternalQuestion(rs.getString("question_id"), rs.getString("practice_set_id"), "", rs.getString("parent_question_id"),
                rs.getString("knowledge_point_id"), rs.getString("question_type"), rs.getString("stem"), read(rs.getString("options")),
                rs.getString("correct_answer"), rs.getString("explanation"), rs.getDouble("difficulty"), toInstant(rs.getObject("created_at")));
    }

    private AttemptRow mapAttempt(ResultSet rs) throws SQLException {
        return new AttemptRow(rs.getString("attempt_id"), rs.getString("practice_set_id"), toInstant(rs.getObject("attempt_time")),
                rs.getString("student_id"), rs.getString("course_id"), rs.getString("class_id"), rs.getString("knowledge_point_id"),
                rs.getString("question_id"), rs.getString("question_source"), rs.getString("difficulty"), rs.getBoolean("correct"),
                rs.getInt("duration_seconds"), rs.getInt("response_time_ms"), rs.getInt("attempt_index"), rs.getString("selected_answer"),
                rs.getString("data_origin"), rs.getString("source_version"), rs.getString("baseline_version"), rs.getString("demo_run_id"),
                rs.getString("demo_case_id"), rs.getString("correlation_id"), rs.getString("coach_session_id"), null);
    }

    private WrongBookItem mapWrongBook(ResultSet rs) throws SQLException {
        return new WrongBookItem(rs.getString("wrong_item_id"), rs.getString("student_id"), rs.getString("course_id"),
                rs.getString("class_id"), rs.getString("question_id"), rs.getString("source_attempt_id"), rs.getString("knowledge_point_id"),
                rs.getString("reason"), rs.getString("status"), rs.getInt("review_count"), toInstant(rs.getObject("added_at")),
                nullableInstant(rs.getObject("repaired_at")), rs.getString("data_origin"), rs.getString("demo_run_id"),
                rs.getString("demo_case_id"), rs.getString("correlation_id"), rs.getString("source_version"),
                rs.getString("knowledge_point_name"), rs.getString("question_summary"), rs.getString("reason_display_name"));
    }

    private String write(Object value) {
        try { return objectMapper.writeValueAsString(value); }
        catch (JsonProcessingException exception) { throw new IllegalStateException("Cannot serialize practice question", exception); }
    }

    private List<QuestionOptionView> read(String value) {
        try { return objectMapper.readValue(value, new TypeReference<>() {}); }
        catch (JsonProcessingException exception) { throw new IllegalStateException("Cannot deserialize practice question", exception); }
    }

    private OffsetDateTime timestamp(Instant instant) { return instant.atOffset(ZoneOffset.UTC); }

    private void emit(int changed, String aggregateType, String aggregateId, String eventType, Instant occurredAt,
                      String sourceVersion, String dataOrigin, String demoRunId, String demoCaseId, String correlationId) {
        if (changed > 0 && outbox != null) {
            outbox.append(aggregateType, aggregateId, eventType, occurredAt, sourceVersion, dataOrigin,
                    demoRunId, demoCaseId, correlationId);
        }
    }
    private Instant toInstant(Object value) {
        if (value instanceof OffsetDateTime dateTime) return dateTime.toInstant();
        if (value instanceof java.sql.Timestamp timestamp) return timestamp.toInstant();
        if (value instanceof Instant instant) return instant;
        throw new IllegalArgumentException("Unsupported timestamp value: " + value);
    }
    private Instant nullableInstant(Object value) { return value == null ? null : toInstant(value); }

    public record AttemptRow(String attemptId, String practiceSetId, Instant attemptTime, String studentId, String courseId,
                             String classId, String knowledgePointId, String questionId, String questionSource, String difficulty,
                             boolean correct, int durationSeconds, int responseTimeMs, int attemptIndex, String selectedAnswer,
                             String dataOrigin, String sourceVersion, String baselineVersion, String demoRunId, String demoCaseId,
                             String correlationId, String coachSessionId, String idempotencyKey) {
        public static AttemptRow newLive(String attemptId, String practiceSetId, Instant attemptTime, String studentId, String courseId,
                                          String classId, String knowledgePointId, String questionId, String questionSource, String difficulty,
                                          boolean correct, int durationSeconds, int responseTimeMs, int attemptIndex, String selectedAnswer,
                                          String sourceVersion, String baselineVersion, DemoContext demo, String coachSessionId, String idempotencyKey) {
            return new AttemptRow(attemptId, practiceSetId, attemptTime, studentId, courseId, classId, knowledgePointId, questionId,
                    questionSource, difficulty, correct, durationSeconds, responseTimeMs, attemptIndex, selectedAnswer,
                    "LIVE_DEMO", sourceVersion, baselineVersion, demo.demoRunId(), demo.demoCaseId(), demo.correlationId(), coachSessionId, idempotencyKey);
        }
    }

    public record PracticeOutcomeData(String outcomeId, String practiceSetId, String studentId, String courseId,
                                      double accuracy, int attemptCount, String dataOrigin, String demoRunId, String demoCaseId,
                                      String correlationId, String sourceVersion, Instant createdAt, String learningStateStatus) {
    }

    public record DemoContext(String demoRunId, String demoCaseId, String correlationId, String baselineVersion) {
    }

    public record GeneratedQuestion(String questionId, String knowledgePointId, String questionType, String stem,
                                    List<QuestionOptionView> options, String correctAnswer, String explanation,
                                    com.zhijiao.foundation.student.coach.DiagnosticTarget diagnosticTarget,
                                    double difficulty, String parentQuestionId, String modelProvider,
                                    String modelVersion, String promptVersion, List<com.zhijiao.foundation.knowledge.Citation> citations) {
        InternalQuestion toInternal(String practiceSetId) {
            return new InternalQuestion(questionId, practiceSetId, "AI_COACH_SIMILAR", parentQuestionId, knowledgePointId,
                    questionType, stem, options, correctAnswer, explanation, difficulty, Instant.now());
        }
    }
}
