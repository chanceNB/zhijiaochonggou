package com.zhijiao.foundation.student.coach;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.zhijiao.foundation.knowledge.Citation;
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
import java.util.UUID;

@Repository
public class CoachRepository {
    private final JdbcTemplate jdbcTemplate;
    private final ObjectMapper objectMapper;

    public CoachRepository(JdbcTemplate jdbcTemplate, ObjectMapper objectMapper) {
        this.jdbcTemplate = jdbcTemplate;
        this.objectMapper = objectMapper;
    }

    public void insertSession(CoachSession session, String idempotencyKey) {
        jdbcTemplate.update("""
                insert into app.coach_sessions
                    (session_id, student_id, course_id, knowledge_point_id, mode, status, rag_status,
                     mastery, confidence, forgetting_risk, weakness_score, reason_codes,
                     learning_model_version, source_version, idempotency_key, created_at, updated_at)
                values (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)
                """, session.sessionId(), session.studentId(), session.courseId(), session.knowledgePointId(),
                session.mode(), session.status(), session.ragStatus().name(), session.mastery(), session.confidence(),
                session.forgettingRisk(), session.weaknessScore(), session.reasonCodes(), session.learningModelVersion(),
                session.sourceVersion(), idempotencyKey, timestamp(session.createdAt()), timestamp(session.updatedAt()));
    }

    public Optional<CoachSession> findSession(String sessionId) {
        List<CoachSession> rows = jdbcTemplate.query("""
                select session_id, student_id, course_id, knowledge_point_id, mode, status, rag_status,
                       mastery, confidence, forgetting_risk, weakness_score, reason_codes,
                       learning_model_version, source_version, created_at, updated_at
                from app.coach_sessions where session_id = ?
                """, (rs, rowNum) -> mapSession(rs), sessionId);
        return rows.stream().findFirst();
    }

    public Optional<CoachSession> findSessionByIdempotencyKey(String studentId, String key) {
        if (key == null || key.isBlank()) return Optional.empty();
        List<CoachSession> rows = jdbcTemplate.query("""
                select session_id, student_id, course_id, knowledge_point_id, mode, status, rag_status,
                       mastery, confidence, forgetting_risk, weakness_score, reason_codes,
                       learning_model_version, source_version, created_at, updated_at
                from app.coach_sessions where student_id = ? and idempotency_key = ?
                """, (rs, rowNum) -> mapSession(rs), studentId, key);
        return rows.stream().findFirst();
    }

    public void insertMessage(CoachMessage message) {
        jdbcTemplate.update("""
                insert into app.coach_messages
                    (message_id, session_id, message_type, content, model_provider, model_version,
                     prompt_version, rag_status, created_at)
                values (?, ?, ?, ?, ?, ?, ?, ?, ?)
                """, message.messageId(), message.sessionId(), message.messageType(), message.content(),
                message.modelProvider(), message.modelVersion(), message.promptVersion(), message.ragStatus().name(),
                timestamp(message.createdAt()));
    }

    public List<CoachMessage> findMessages(String sessionId) {
        return jdbcTemplate.query("""
                select message_id, session_id, message_type, content, model_provider, model_version,
                       prompt_version, rag_status, created_at
                from app.coach_messages where session_id = ?
                order by created_at, message_id
                """, (rs, rowNum) -> {
            CoachMessage base = mapMessage(rs);
            return new CoachMessage(base.messageId(), base.sessionId(), base.messageType(), base.content(),
                    base.modelProvider(), base.modelVersion(), base.promptVersion(), base.ragStatus(),
                    base.createdAt(), findCitations(sessionId, base.messageId(), null));
        }, sessionId);
    }

    public String saveDiagnosticSet(String practiceSetId, String sessionId, String knowledgePointId,
                                    List<DiagnosticQuestion> questions, RagStatus ragStatus,
                                    LlmResponse response, String sourceVersion) {
        Instant generatedAt = Instant.now();
        for (DiagnosticQuestion question : questions) {
            jdbcTemplate.update("""
                    insert into app.coach_diagnostic_questions
                        (practice_set_id, question_id, session_id, knowledge_point_id, question_type, stem,
                         options, correct_answer, explanation, diagnostic_target, difficulty, model_provider,
                         model_version, prompt_version, source_version, generated_at)
                    values (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)
                    """, practiceSetId, question.questionId(), sessionId, knowledgePointId, question.questionType(),
                    question.stem(), write(question.options()), question.correctAnswer(), question.explanation(),
                    write(question.diagnosticTarget()), question.difficulty(), response.provider(), response.modelVersion(),
                    response.promptVersion(), sourceVersion, timestamp(generatedAt));
            insertCitations(sessionId, null, practiceSetId, question.questionId(), question.citations(), generatedAt);
        }
        return practiceSetId;
    }

    public List<DiagnosticQuestion> findDiagnosticQuestions(String sessionId) {
        return jdbcTemplate.query("""
                select practice_set_id, question_id, knowledge_point_id, question_type, stem, options,
                       correct_answer, explanation, diagnostic_target, difficulty, model_provider,
                       model_version, prompt_version
                from app.coach_diagnostic_questions where session_id = ?
                order by generated_at, question_id
                """, (rs, rowNum) -> {
            String practiceSetId = rs.getString("practice_set_id");
            String questionId = rs.getString("question_id");
            return new DiagnosticQuestion(questionId, rs.getString("knowledge_point_id"),
                    rs.getString("question_type"), rs.getString("stem"), read(rs.getString("options"),
                    new TypeReference<>() {}), rs.getString("correct_answer"), rs.getString("explanation"),
                    read(rs.getString("diagnostic_target"), DiagnosticTarget.class), rs.getDouble("difficulty"),
                    findCitations(sessionId, null, practiceSetId, questionId), rs.getString("model_provider"),
                    rs.getString("model_version"), rs.getString("prompt_version"));
        }, sessionId);
    }

    private void insertCitations(String sessionId, String messageId, String practiceSetId, String questionId,
                                 List<Citation> citations, Instant createdAt) {
        if (citations == null) return;
        for (Citation citation : citations) {
            jdbcTemplate.update("""
                    insert into app.coach_citations
                        (citation_id, session_id, message_id, practice_set_id, question_id, document_id,
                         chunk_id, title, excerpt, score, created_at)
                    values (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)
                    """, "citation-" + UUID.randomUUID().toString().replace("-", ""), sessionId, messageId,
                    practiceSetId, questionId, citation.documentId(), citation.chunkId(), citation.title(),
                    citation.excerpt(), citation.score(), timestamp(createdAt));
        }
    }

    public void insertMessageCitations(CoachMessage message) {
        insertCitations(message.sessionId(), message.messageId(), null, null, message.citations(), message.createdAt());
    }

    private List<Citation> findCitations(String sessionId, String messageId, String practiceSetId) {
        return findCitations(sessionId, messageId, practiceSetId, null);
    }

    private List<Citation> findCitations(String sessionId, String messageId, String practiceSetId, String questionId) {
        StringBuilder sql = new StringBuilder("""
                select document_id, chunk_id, title, excerpt, score
                from app.coach_citations where session_id = ?
                """);
        List<Object> args = new ArrayList<>();
        args.add(sessionId);
        if (messageId != null) { sql.append(" and message_id = ?"); args.add(messageId); }
        if (practiceSetId != null) { sql.append(" and practice_set_id = ?"); args.add(practiceSetId); }
        if (questionId != null) { sql.append(" and question_id = ?"); args.add(questionId); }
        return jdbcTemplate.query(sql.toString(), (rs, rowNum) -> new Citation(rs.getString("document_id"),
                rs.getString("chunk_id"), rs.getString("title"), rs.getString("excerpt"), rs.getDouble("score")),
                args.toArray());
    }

    private CoachSession mapSession(ResultSet rs) throws SQLException {
        return new CoachSession(rs.getString("session_id"), rs.getString("student_id"), rs.getString("course_id"),
                rs.getString("knowledge_point_id"), rs.getString("mode"), rs.getString("status"),
                RagStatus.valueOf(rs.getString("rag_status")), rs.getDouble("mastery"), rs.getDouble("confidence"),
                rs.getDouble("forgetting_risk"), rs.getDouble("weakness_score"), rs.getString("reason_codes"),
                rs.getString("learning_model_version"), rs.getString("source_version"),
                toInstant(rs.getObject("created_at")), toInstant(rs.getObject("updated_at")));
    }

    private CoachMessage mapMessage(ResultSet rs) throws SQLException {
        return new CoachMessage(rs.getString("message_id"), rs.getString("session_id"), rs.getString("message_type"),
                rs.getString("content"), rs.getString("model_provider"), rs.getString("model_version"),
                rs.getString("prompt_version"), RagStatus.valueOf(rs.getString("rag_status")),
                toInstant(rs.getObject("created_at")), List.of());
    }

    private String write(Object value) {
        try { return objectMapper.writeValueAsString(value); }
        catch (JsonProcessingException exception) { throw new IllegalStateException("Cannot serialize coach data", exception); }
    }

    private <T> T read(String value, Class<T> type) {
        try { return objectMapper.readValue(value, type); }
        catch (JsonProcessingException exception) { throw new IllegalStateException("Cannot deserialize coach data", exception); }
    }

    private <T> T read(String value, TypeReference<T> type) {
        try { return objectMapper.readValue(value, type); }
        catch (JsonProcessingException exception) { throw new IllegalStateException("Cannot deserialize coach data", exception); }
    }

    private OffsetDateTime timestamp(Instant instant) { return instant.atOffset(ZoneOffset.UTC); }
    private Instant toInstant(Object value) {
        if (value instanceof OffsetDateTime dateTime) return dateTime.toInstant();
        if (value instanceof java.sql.Timestamp timestamp) return timestamp.toInstant();
        if (value instanceof Instant instant) return instant;
        throw new IllegalArgumentException("Unsupported timestamp value: " + value);
    }
}
