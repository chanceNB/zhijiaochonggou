package com.zhijiao.foundation.student.learning;

import com.zhijiao.foundation.analytics.DomainEventOutboxRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.Instant;
import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.List;
import java.util.Optional;

@Repository
public class LearningStateRepository {
    private final JdbcTemplate jdbcTemplate;
    private final DomainEventOutboxRepository outbox;

    @Autowired
    public LearningStateRepository(JdbcTemplate jdbcTemplate, DomainEventOutboxRepository outbox) {
        this.jdbcTemplate = jdbcTemplate;
        this.outbox = outbox;
    }

    public LearningStateRepository(JdbcTemplate jdbcTemplate) {
        this(jdbcTemplate, null);
    }

    public Optional<BaselineContext> findBaseline(String baselineVersion) {
        List<BaselineContext> rows = jdbcTemplate.query("""
                select baseline_version, reference_date, source_version, data_origin
                from app.baseline_metadata where baseline_version = ?
                """, (rs, rowNum) -> new BaselineContext(
                rs.getString("baseline_version"),
                rs.getDate("reference_date").toLocalDate(),
                rs.getString("source_version"),
                rs.getString("data_origin")), baselineVersion);
        return rows.stream().findFirst();
    }

    public List<PracticeObservation> findPracticeObservations(String baselineVersion) {
        return jdbcTemplate.query("""
                select p.attempt_id, p.student_id, p.course_id, p.class_id,
                       p.knowledge_point_id, p.question_id, p.correct,
                       p.response_time_ms, p.attempt_index, p.attempt_time,
                       q.item_difficulty
                from app.practice_attempts p
                left join app.question_items q on q.question_id = p.question_id
                left join app.practice_questions pq on pq.practice_set_id = p.practice_set_id
                    and pq.question_id = p.question_id
                where p.baseline_version = ?
                  and p.data_origin = 'BASELINE_SIMULATED'
                  and p.demo_run_id is null
                order by p.student_id, p.attempt_time, p.attempt_id
                """, (rs, rowNum) -> new PracticeObservation(
                rs.getString("attempt_id"),
                rs.getString("student_id"),
                rs.getString("course_id"),
                rs.getString("class_id"),
                rs.getString("knowledge_point_id"),
                rs.getString("question_id"),
                rs.getBoolean("correct"),
                rs.getInt("response_time_ms"),
                rs.getInt("attempt_index"),
                toInstant(rs.getObject("attempt_time")),
                rs.getBigDecimal("item_difficulty").doubleValue()), baselineVersion);
    }

    public List<PracticeObservation> findPracticeObservationsForScope(String baselineVersion, String studentId,
                                                                       String courseId, String demoRunId) {
        return jdbcTemplate.query("""
                select p.attempt_id, p.student_id, p.course_id, p.class_id,
                       p.knowledge_point_id, p.question_id, p.correct,
                       p.response_time_ms, p.attempt_index, p.attempt_time,
                       coalesce(q.item_difficulty, pq.difficulty) as item_difficulty
                from app.practice_attempts p
                left join app.question_items q on q.question_id = p.question_id
                left join app.practice_questions pq on pq.practice_set_id = p.practice_set_id
                    and pq.question_id = p.question_id
                where p.student_id = ? and p.course_id = ?
                  and ((p.baseline_version = ? and p.data_origin = 'BASELINE_SIMULATED' and p.demo_run_id is null)
                       or (p.data_origin = 'LIVE_DEMO' and p.demo_run_id = ?))
                order by p.attempt_time, p.attempt_id
                """, (rs, rowNum) -> new PracticeObservation(
                rs.getString("attempt_id"), rs.getString("student_id"), rs.getString("course_id"),
                rs.getString("class_id"), rs.getString("knowledge_point_id"), rs.getString("question_id"),
                rs.getBoolean("correct"), rs.getInt("response_time_ms"), rs.getInt("attempt_index"),
                toInstant(rs.getObject("attempt_time")), rs.getBigDecimal("item_difficulty").doubleValue()),
                studentId, courseId, baselineVersion, demoRunId);
    }

    public List<StudentKnowledgeState> findStatesForScope(String studentId, String courseId) {
        return findStates(studentId, courseId);
    }

    public void replaceScopedDerived(String baselineVersion, String sourceVersion, String studentId, String courseId,
                                     String demoRunId, String demoCaseId, String correlationId,
                                     StudentAbilityState ability, List<StudentKnowledgeState> states,
                                     List<WeakKnowledgePointCandidate> candidates) {
        List<StudentKnowledgeState> previousStates = findStatesForScope(studentId, courseId);
        StudentAbilityState previousAbility = findAbility(studentId, courseId).orElse(null);
        Instant capturedAt = Instant.now();
        for (StudentKnowledgeState previous : previousStates) {
            insertHistory(previous, previousAbility, baselineVersion, "BASELINE_SIMULATED", null, null, null, capturedAt);
        }
        jdbcTemplate.update("delete from app.weak_knowledge_point_candidates where student_id = ? and course_id = ?", studentId, courseId);
        jdbcTemplate.update("delete from app.student_learning_abilities where student_id = ? and course_id = ?", studentId, courseId);
        jdbcTemplate.update("delete from app.learning_snapshots where student_id = ? and course_id = ?", studentId, courseId);

        jdbcTemplate.update("""
                insert into app.student_learning_abilities
                    (student_id, course_id, theta, theta_uncertainty, ability_model_version,
                     baseline_version, data_origin, source_version, computed_at)
                values (?, ?, ?, ?, ?, ?, 'LIVE_DEMO', ?, ?)
                """, ability.studentId(), ability.courseId(), ability.theta(), ability.thetaUncertainty(), ability.abilityModelVersion(),
                baselineVersion, sourceVersion, timestamp(ability.computedAt()));
        for (StudentKnowledgeState state : states) {
            jdbcTemplate.update("""
                    insert into app.learning_snapshots
                    (student_id, knowledge_point_id, course_id, class_id, mastery, confidence,
                         forgetting_risk, misconception_code, snapshot_time, baseline_version,
                         data_origin, source_version, evidence_count, last_evidence_at,
                         mastery_model_version, ability_model_version, forgetting_model_version,
                         confidence_model_version, computed_at)
                    values (?, ?, ?, ?, ?, ?, ?, null, ?, ?, 'LIVE_DEMO', ?, ?, ?, ?, ?, ?, ?, ?)
                    """, state.studentId(), state.knowledgePointId(), state.courseId(), state.classId(), state.masteryProbability(),
                    state.confidence(), state.forgettingRisk(), timestamp(state.computedAt()), baselineVersion, sourceVersion,
                    state.evidenceCount(), timestamp(state.lastEvidenceAt()), state.masteryModelVersion(), state.abilityModelVersion(),
                    state.forgettingModelVersion(), state.confidenceModelVersion(), timestamp(state.computedAt()));
        }
        for (WeakKnowledgePointCandidate candidate : candidates) {
            jdbcTemplate.update("""
                    insert into app.weak_knowledge_point_candidates
                    (student_id, course_id, knowledge_point_id, weakness_score, confidence,
                         evidence_count, rank_position, reason_codes, model_version,
                         baseline_version, data_origin, source_version, computed_at)
                    values (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, 'LIVE_DEMO', ?, ?)
                    """, candidate.studentId(), candidate.courseId(), candidate.knowledgePointId(), candidate.weaknessScore(),
                    candidate.confidence(), candidate.evidenceCount(), candidate.rankPosition(), String.join(",", candidate.reasonCodes()),
                    candidate.modelVersion(), baselineVersion, sourceVersion, timestamp(findComputedAt(candidate, states)));
        }
        for (StudentKnowledgeState state : states) {
            insertHistory(state, ability, baselineVersion, "LIVE_DEMO", demoRunId, demoCaseId, correlationId, capturedAt);
        }
        if (outbox != null) {
            outbox.append("LearningState", studentId + ":" + courseId, "LEARNING_STATE_RECOMPUTED",
                    ability.computedAt(), sourceVersion, "LIVE_DEMO", demoRunId, demoCaseId, correlationId);
        }
    }

    private void insertHistory(StudentKnowledgeState state, StudentAbilityState ability, String baselineVersion,
                               String dataOrigin, String demoRunId, String demoCaseId, String correlationId,
                               Instant capturedAt) {
        jdbcTemplate.update("""
                insert into app.learning_snapshot_history
                    (history_id, student_id, course_id, class_id, knowledge_point_id, mastery, confidence,
                     forgetting_risk, evidence_count, last_evidence_at, mastery_model_version, ability_model_version,
                     forgetting_model_version, confidence_model_version, theta, theta_uncertainty, computed_at,
                     data_origin, baseline_version, source_version, demo_run_id, demo_case_id, correlation_id, captured_at)
                values (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)
                """, "history-" + java.util.UUID.randomUUID().toString().replace("-", ""), state.studentId(), state.courseId(),
                state.classId(), state.knowledgePointId(), state.masteryProbability(), state.confidence(), state.forgettingRisk(),
                state.evidenceCount(), timestamp(state.lastEvidenceAt()), state.masteryModelVersion(), state.abilityModelVersion(),
                state.forgettingModelVersion(), state.confidenceModelVersion(), ability == null ? null : ability.theta(),
                ability == null ? null : ability.thetaUncertainty(), timestamp(state.computedAt()), dataOrigin, baselineVersion,
                state.sourceVersion(), demoRunId, demoCaseId, correlationId, timestamp(capturedAt));
    }

    private Instant findComputedAt(WeakKnowledgePointCandidate candidate, List<StudentKnowledgeState> states) {
        return states.stream().filter(state -> state.studentId().equals(candidate.studentId())
                        && state.courseId().equals(candidate.courseId())
                        && state.knowledgePointId().equals(candidate.knowledgePointId()))
                .map(StudentKnowledgeState::computedAt).findFirst().orElseThrow();
    }

    public List<KnowledgePointRef> findKnowledgePoints(String courseId) {
        return jdbcTemplate.query("""
                select knowledge_point_id, name
                from app.knowledge_points where course_id = ? order by sort_order, knowledge_point_id
                """, (rs, rowNum) -> new KnowledgePointRef(
                rs.getString("knowledge_point_id"), rs.getString("name")), courseId);
    }

    public void replaceDerived(String baselineVersion,
                               String sourceVersion,
                               List<StudentAbilityState> abilities,
                               List<StudentKnowledgeState> states,
                               List<WeakKnowledgePointCandidate> candidates) {
        jdbcTemplate.update("delete from app.weak_knowledge_point_candidates where baseline_version = ?", baselineVersion);
        jdbcTemplate.update("delete from app.student_learning_abilities where baseline_version = ?", baselineVersion);
        jdbcTemplate.update("delete from app.learning_snapshots where baseline_version = ?", baselineVersion);

        jdbcTemplate.batchUpdate("""
                insert into app.student_learning_abilities
                    (student_id, course_id, theta, theta_uncertainty, ability_model_version,
                     baseline_version, data_origin, source_version, computed_at)
                values (?, ?, ?, ?, ?, ?, 'BASELINE_SIMULATED', ?, ?)
                """, abilities, 100, (ps, ability) -> {
            ps.setString(1, ability.studentId());
            ps.setString(2, ability.courseId());
            ps.setDouble(3, ability.theta());
            ps.setDouble(4, ability.thetaUncertainty());
            ps.setString(5, ability.abilityModelVersion());
            ps.setString(6, baselineVersion);
            ps.setString(7, ability.sourceVersion());
            ps.setObject(8, timestamp(ability.computedAt()));
        });

        jdbcTemplate.batchUpdate("""
                insert into app.learning_snapshots
                    (student_id, knowledge_point_id, course_id, class_id, mastery, confidence,
                     forgetting_risk, misconception_code, snapshot_time, baseline_version,
                     data_origin, source_version, evidence_count, last_evidence_at,
                     mastery_model_version, ability_model_version, forgetting_model_version,
                     confidence_model_version, computed_at)
                values (?, ?, ?, ?, ?, ?, ?, null, ?, ?, 'BASELINE_SIMULATED', ?, ?, ?, ?, ?, ?, ?, ?)
                """, states, 100, (ps, state) -> {
            ps.setString(1, state.studentId());
            ps.setString(2, state.knowledgePointId());
            ps.setString(3, state.courseId());
            ps.setString(4, state.classId());
            ps.setDouble(5, state.masteryProbability());
            ps.setDouble(6, state.confidence());
            ps.setDouble(7, state.forgettingRisk());
            ps.setObject(8, timestamp(state.computedAt()));
            ps.setString(9, baselineVersion);
            ps.setString(10, state.sourceVersion());
            ps.setInt(11, state.evidenceCount());
            ps.setObject(12, timestamp(state.lastEvidenceAt()));
            ps.setString(13, state.masteryModelVersion());
            ps.setString(14, state.abilityModelVersion());
            ps.setString(15, state.forgettingModelVersion());
            ps.setString(16, state.confidenceModelVersion());
            ps.setObject(17, timestamp(state.computedAt()));
        });

        jdbcTemplate.batchUpdate("""
                insert into app.weak_knowledge_point_candidates
                    (student_id, course_id, knowledge_point_id, weakness_score, confidence,
                     evidence_count, rank_position, reason_codes, model_version,
                     baseline_version, data_origin, source_version, computed_at)
                values (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, 'BASELINE_SIMULATED', ?, ?)
                """, candidates, 100, (ps, candidate) -> {
            ps.setString(1, candidate.studentId());
            ps.setString(2, candidate.courseId());
            ps.setString(3, candidate.knowledgePointId());
            ps.setDouble(4, candidate.weaknessScore());
            ps.setDouble(5, candidate.confidence());
            ps.setInt(6, candidate.evidenceCount());
            ps.setInt(7, candidate.rankPosition());
            ps.setString(8, String.join(",", candidate.reasonCodes()));
            ps.setString(9, candidate.modelVersion());
            ps.setString(10, baselineVersion);
            ps.setString(11, sourceVersion);
            ps.setObject(12, timestamp(findComputedAt(candidate, states)));
                });
        if (outbox != null) {
            outbox.append("LearningState", baselineVersion, "LEARNING_STATE_RECOMPUTED", Instant.now(),
                    sourceVersion, "BASELINE_SIMULATED", null, null, null);
        }
    }

    public Optional<StudentKnowledgeState> findState(String studentId, String courseId, String knowledgePointId) {
        List<StudentKnowledgeState> rows = jdbcTemplate.query("""
                select s.student_id, s.course_id, s.class_id, s.knowledge_point_id, k.name,
                       s.mastery, s.confidence, s.forgetting_risk, s.evidence_count,
                       s.last_evidence_at, s.mastery_model_version, s.ability_model_version,
                       s.forgetting_model_version, s.confidence_model_version,
                       s.computed_at, s.source_version
                from app.learning_snapshots s
                join app.knowledge_points k on k.knowledge_point_id = s.knowledge_point_id
                where s.student_id = ? and s.course_id = ? and s.knowledge_point_id = ?
                """, (rs, rowNum) -> mapState(rs), studentId, courseId, knowledgePointId);
        return rows.stream().findFirst();
    }

    public List<StudentKnowledgeState> findStates(String studentId, String courseId) {
        return jdbcTemplate.query("""
                select s.student_id, s.course_id, s.class_id, s.knowledge_point_id, k.name,
                       s.mastery, s.confidence, s.forgetting_risk, s.evidence_count,
                       s.last_evidence_at, s.mastery_model_version, s.ability_model_version,
                       s.forgetting_model_version, s.confidence_model_version,
                       s.computed_at, s.source_version
                from app.learning_snapshots s
                join app.knowledge_points k on k.knowledge_point_id = s.knowledge_point_id
                where s.student_id = ? and s.course_id = ?
                order by s.knowledge_point_id
                """, (rs, rowNum) -> mapState(rs), studentId, courseId);
    }

    public Optional<StudentAbilityState> findAbility(String studentId, String courseId) {
        List<StudentAbilityState> rows = jdbcTemplate.query("""
                select student_id, course_id, theta, theta_uncertainty, ability_model_version,
                       computed_at, source_version
                from app.student_learning_abilities where student_id = ? and course_id = ?
                """, (rs, rowNum) -> new StudentAbilityState(
                rs.getString("student_id"), rs.getString("course_id"),
                rs.getDouble("theta"), rs.getDouble("theta_uncertainty"),
                rs.getString("ability_model_version"), toInstant(rs.getObject("computed_at")),
                rs.getString("source_version")), studentId, courseId);
        return rows.stream().findFirst();
    }

    public List<WeakKnowledgePointCandidate> findCandidates(String studentId, String courseId) {
        return jdbcTemplate.query("""
                select c.student_id, c.course_id, c.knowledge_point_id, k.name,
                       c.weakness_score, c.confidence, c.evidence_count, c.rank_position,
                       c.reason_codes, c.model_version
                from app.weak_knowledge_point_candidates c
                join app.knowledge_points k on k.knowledge_point_id = c.knowledge_point_id
                where c.student_id = ? and c.course_id = ?
                order by c.rank_position
                """, (rs, rowNum) -> new WeakKnowledgePointCandidate(
                rs.getString("student_id"), rs.getString("course_id"),
                rs.getString("knowledge_point_id"), rs.getString("name"),
                rs.getDouble("weakness_score"), rs.getDouble("confidence"),
                rs.getInt("evidence_count"), rs.getInt("rank_position"),
                List.of(rs.getString("reason_codes").split(",")), rs.getString("model_version")),
                studentId, courseId);
    }

    private StudentKnowledgeState mapState(ResultSet rs) throws SQLException {
        return new StudentKnowledgeState(
                rs.getString("student_id"), rs.getString("course_id"), rs.getString("class_id"),
                rs.getString("knowledge_point_id"), rs.getString("name"), rs.getDouble("mastery"),
                rs.getDouble("confidence"), rs.getDouble("forgetting_risk"), rs.getInt("evidence_count"),
                toInstant(rs.getObject("last_evidence_at")), rs.getString("mastery_model_version"),
                rs.getString("ability_model_version"), rs.getString("forgetting_model_version"),
                rs.getString("confidence_model_version"), toInstant(rs.getObject("computed_at")),
                rs.getString("source_version"));
    }

    private OffsetDateTime timestamp(Instant instant) {
        return instant.atOffset(ZoneOffset.UTC);
    }

    private static Instant toInstant(Object value) {
        if (value instanceof OffsetDateTime offsetDateTime) {
            return offsetDateTime.toInstant();
        }
        if (value instanceof java.sql.Timestamp timestamp) {
            return timestamp.toInstant();
        }
        if (value instanceof Instant instant) {
            return instant;
        }
        throw new IllegalArgumentException("Unsupported timestamp value: " + value);
    }

    public record BaselineContext(String baselineVersion, LocalDate referenceDate,
                                  String sourceVersion, String dataOrigin) {
    }

    public record KnowledgePointRef(String knowledgePointId, String name) {
    }
}
