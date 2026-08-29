package com.zhijiao.foundation.knowledge;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.Instant;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

@Repository
public class KnowledgeRepository {
    private final JdbcTemplate jdbcTemplate;

    public KnowledgeRepository(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    public void insertDocument(KnowledgeDocument document, String content) {
        jdbcTemplate.update("""
                insert into app.knowledge_documents
                    (document_id, course_id, title, source, content, status, data_origin,
                     source_version, created_at, indexed_at)
                values (?, ?, ?, ?, ?, ?, ?, ?, ?, ?)
                """, document.documentId(), document.courseId(), document.title(), document.source(), content,
                document.status(), document.dataOrigin(), document.sourceVersion(), timestamp(document.createdAt()),
                document.indexedAt() == null ? null : timestamp(document.indexedAt()));
    }

    public void insertChunks(List<KnowledgeChunk> chunks) {
        jdbcTemplate.batchUpdate("""
                insert into app.knowledge_chunks
                    (chunk_id, document_id, course_id, knowledge_point_id, title, content,
                     chunk_index, embedding, metadata, source_version, created_at)
                values (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)
                """, chunks, 100, (ps, chunk) -> {
            ps.setString(1, chunk.chunkId());
            ps.setString(2, chunk.documentId());
            ps.setString(3, chunk.courseId());
            ps.setString(4, chunk.knowledgePointId());
            ps.setString(5, chunk.title());
            ps.setString(6, chunk.content());
            ps.setInt(7, chunk.chunkIndex());
            ps.setString(8, encodeDoubles(chunk.embedding()));
            ps.setString(9, String.join("\u001f", chunk.metadata()));
            ps.setString(10, chunk.sourceVersion());
            ps.setObject(11, timestamp(chunk.createdAt()));
        });
    }

    public Optional<KnowledgeDocument> findDocument(String documentId) {
        List<KnowledgeDocument> rows = jdbcTemplate.query("""
                select document_id, course_id, title, source, status, data_origin,
                       source_version, created_at, indexed_at
                from app.knowledge_documents where document_id = ?
                """, (rs, rowNum) -> mapDocument(rs), documentId);
        return rows.stream().findFirst();
    }

    public List<KnowledgeDocument> findDocuments(String courseId) {
        return jdbcTemplate.query("""
                select document_id, course_id, title, source, status, data_origin,
                       source_version, created_at, indexed_at
                from app.knowledge_documents where course_id = ? order by created_at, document_id
                """, (rs, rowNum) -> mapDocument(rs), courseId);
    }

    public int countChunks(String documentId) {
        return jdbcTemplate.queryForObject("select count(*) from app.knowledge_chunks where document_id = ?",
                Integer.class, documentId);
    }

    public List<StoredChunk> findChunks(String courseId, String knowledgePointId) {
        String sql = knowledgePointId == null || knowledgePointId.isBlank() ? """
                select chunk_id, document_id, title, content, embedding, metadata
                from app.knowledge_chunks
                where course_id = ?
                order by document_id, chunk_index
                """ : """
                select chunk_id, document_id, title, content, embedding, metadata
                from app.knowledge_chunks
                where course_id = ? and (knowledge_point_id = ? or knowledge_point_id is null)
                order by document_id, chunk_index
                """;
        return knowledgePointId == null || knowledgePointId.isBlank()
                ? jdbcTemplate.query(sql, (rs, rowNum) -> mapChunk(rs), courseId)
                : jdbcTemplate.query(sql, (rs, rowNum) -> mapChunk(rs), courseId, knowledgePointId);
    }

    private KnowledgeDocument mapDocument(ResultSet rs) throws SQLException {
        return new KnowledgeDocument(rs.getString("document_id"), rs.getString("course_id"),
                rs.getString("title"), rs.getString("source"), rs.getString("status"),
                rs.getString("data_origin"), rs.getString("source_version"),
                toInstant(rs.getObject("created_at")), toNullableInstant(rs.getObject("indexed_at")));
    }

    private StoredChunk mapChunk(ResultSet rs) throws SQLException {
        String metadata = rs.getString("metadata");
        return new StoredChunk(rs.getString("chunk_id"), rs.getString("document_id"),
                rs.getString("title"), rs.getString("content"), decodeDoubles(rs.getString("embedding")),
                metadata == null || metadata.isBlank() ? List.of() : Arrays.asList(metadata.split("\u001f", -1)));
    }

    private String encodeDoubles(List<Double> values) {
        return values.stream().map(String::valueOf).collect(java.util.stream.Collectors.joining(","));
    }

    private List<Double> decodeDoubles(String encoded) {
        if (encoded == null || encoded.isBlank()) {
            return List.of();
        }
        return Arrays.stream(encoded.split(",")).map(Double::valueOf).toList();
    }

    private OffsetDateTime timestamp(Instant instant) {
        return instant.atOffset(ZoneOffset.UTC);
    }

    private Instant toInstant(Object value) {
        if (value instanceof OffsetDateTime dateTime) return dateTime.toInstant();
        if (value instanceof java.sql.Timestamp timestamp) return timestamp.toInstant();
        if (value instanceof Instant instant) return instant;
        throw new IllegalArgumentException("Unsupported timestamp value: " + value);
    }

    private Instant toNullableInstant(Object value) {
        return value == null ? null : toInstant(value);
    }

    public record StoredChunk(String chunkId, String documentId, String title, String content,
                              List<Double> embedding, List<String> metadata) {
    }
}
