package com.zhijiao.foundation.teacher;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.Instant;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.List;
import java.util.Optional;

@Repository
public class AnalysisRecommendationRepository {
    private final JdbcTemplate jdbcTemplate;
    private final ObjectMapper objectMapper;

    public AnalysisRecommendationRepository(JdbcTemplate jdbcTemplate, ObjectMapper objectMapper) {
        this.jdbcTemplate = jdbcTemplate;
        this.objectMapper = objectMapper;
    }

    public Optional<AnalysisRecommendation> findById(String recommendationId) {
        return jdbcTemplate.query("""
                select recommendation_id, student_id, course_id, class_id, knowledge_point_id,
                       demo_run_id, demo_case_id, correlation_id, analysis_summary, evidence_refs, source, capture_mode,
                       status, generated_at, captured_at, source_version
                from app.analysis_recommendations where recommendation_id = ?
                """, (rs, rowNum) -> map(rs), recommendationId).stream().findFirst();
    }

    public Optional<AnalysisRecommendation> findByIdempotencyKey(String idempotencyKey) {
        return jdbcTemplate.query("""
                select recommendation_id, student_id, course_id, class_id, knowledge_point_id,
                       demo_run_id, demo_case_id, correlation_id, analysis_summary, evidence_refs, source, capture_mode,
                       status, generated_at, captured_at, source_version
                from app.analysis_recommendations where idempotency_key = ?
                """, (rs, rowNum) -> map(rs), idempotencyKey).stream().findFirst();
    }

    public void insert(AnalysisRecommendation recommendation, String idempotencyKey) {
        jdbcTemplate.update("""
                insert into app.analysis_recommendations
                    (recommendation_id, student_id, course_id, class_id, knowledge_point_id,
                     demo_run_id, demo_case_id, correlation_id, analysis_summary, evidence_refs, source, capture_mode,
                     status, generated_at, captured_at, source_version, idempotency_key)
                values (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)
                """, recommendation.recommendationId(), recommendation.studentId(), recommendation.courseId(),
                recommendation.classId(), recommendation.knowledgePointId(), recommendation.demoRunId(),
                recommendation.demoCaseId(), recommendation.correlationId(), recommendation.analysisSummary(),
                write(recommendation.evidenceRefs()), recommendation.source(), recommendation.captureMode(), recommendation.status(),
                timestamp(recommendation.generatedAt()), timestamp(recommendation.capturedAt()),
                recommendation.sourceVersion(), idempotencyKey);
        for (AnalysisRecommendation.Candidate candidate : recommendation.candidates()) {
            jdbcTemplate.update("""
                    insert into app.analysis_recommendation_candidates
                        (recommendation_id, candidate_index, strategy_code, title, rationale,
                         action_description, source_snapshot)
                    values (?, ?, ?, ?, ?, ?, ?)
                    """, recommendation.recommendationId(), candidate.candidateIndex(), candidate.strategyCode(),
                    candidate.title(), candidate.rationale(), candidate.actionDescription(), candidate.sourceSnapshot());
        }
    }

    public Optional<DemoContext> findDemoContext(String studentId, String courseId, String demoRunId) {
        String sql = demoRunId == null || demoRunId.isBlank() ? """
                select demo_run_id, demo_case_id, correlation_id, class_id
                from app.demo_runs where student_id = ? and course_id = ? and status = 'ACTIVE'
                order by created_at desc limit 1
                """ : """
                select demo_run_id, demo_case_id, correlation_id, class_id
                from app.demo_runs where demo_run_id = ? and student_id = ? and course_id = ? and status = 'ACTIVE'
                """;
        Object[] args = demoRunId == null || demoRunId.isBlank()
                ? new Object[]{studentId, courseId} : new Object[]{demoRunId, studentId, courseId};
        return jdbcTemplate.query(sql, (rs, rowNum) -> new DemoContext(rs.getString("demo_run_id"),
                rs.getString("demo_case_id"), rs.getString("correlation_id"), rs.getString("class_id")), args)
                .stream().findFirst();
    }

    private AnalysisRecommendation map(ResultSet rs) throws SQLException {
        String recommendationId = rs.getString("recommendation_id");
        List<AnalysisRecommendation.Candidate> candidates = jdbcTemplate.query("""
                select candidate_index, strategy_code, title, rationale, action_description, source_snapshot
                from app.analysis_recommendation_candidates where recommendation_id = ? order by candidate_index
                """, (candidateRs, rowNum) -> new AnalysisRecommendation.Candidate(
                candidateRs.getInt("candidate_index"), candidateRs.getString("strategy_code"),
                candidateRs.getString("title"), candidateRs.getString("rationale"),
                candidateRs.getString("action_description"), candidateRs.getString("source_snapshot")), recommendationId);
        return new AnalysisRecommendation(recommendationId, rs.getString("student_id"), rs.getString("course_id"),
                rs.getString("class_id"), rs.getString("knowledge_point_id"), rs.getString("demo_run_id"),
                rs.getString("demo_case_id"), rs.getString("correlation_id"), rs.getString("analysis_summary"),
                readEvidenceRefs(rs.getString("evidence_refs")), candidates, rs.getString("source"), rs.getString("capture_mode"), rs.getString("status"),
                instant(rs.getObject("generated_at")), instant(rs.getObject("captured_at")),
                rs.getString("source_version"));
    }

    public String snapshot(AnalysisRecommendationCapture.Candidate candidate) {
        try {
            return objectMapper.writeValueAsString(candidate);
        } catch (JsonProcessingException exception) {
            throw new IllegalStateException("Cannot serialize recommendation snapshot", exception);
        }
    }

    private String write(Object value) {
        try {
            return objectMapper.writeValueAsString(value);
        } catch (JsonProcessingException exception) {
            throw new IllegalStateException("Cannot serialize recommendation data", exception);
        }
    }

    private List<String> readEvidenceRefs(String value) {
        if (value == null || value.isBlank()) return List.of();
        try {
            return objectMapper.readValue(value, new TypeReference<>() {
            });
        } catch (JsonProcessingException exception) {
            throw new IllegalStateException("Cannot deserialize recommendation evidence", exception);
        }
    }

    private OffsetDateTime timestamp(Instant instant) {
        return instant.atOffset(ZoneOffset.UTC);
    }

    private Instant instant(Object value) {
        if (value instanceof OffsetDateTime dateTime) return dateTime.toInstant();
        if (value instanceof java.sql.Timestamp timestamp) return timestamp.toInstant();
        if (value instanceof Instant instant) return instant;
        throw new IllegalArgumentException("Unsupported timestamp value: " + value);
    }

    public record DemoContext(String demoRunId, String demoCaseId, String correlationId, String classId) {
    }
}
