package com.zhijiao.foundation.knowledge;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Clock;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Service
public class KnowledgeIngestionService {
    private final KnowledgeRepository repository;
    private final DeterministicEmbedding embedding;
    private final Clock clock;

    public KnowledgeIngestionService(KnowledgeRepository repository, Clock clock) {
        this.repository = repository;
        this.embedding = new DeterministicEmbedding(32);
        this.clock = clock;
    }

    @Transactional
    public KnowledgeDocument ingestText(String courseId, String title, String source, String content,
                                        String knowledgePointId, String sourceVersion, String dataOrigin) {
        if (courseId == null || courseId.isBlank() || title == null || title.isBlank()
                || content == null || content.isBlank()) {
            throw new IllegalArgumentException("courseId, title, and content are required");
        }
        Instant now = Instant.now(clock);
        String documentId = "doc-" + UUID.randomUUID().toString().replace("-", "");
        KnowledgeDocument document = new KnowledgeDocument(documentId, courseId, title,
                source == null || source.isBlank() ? title : source, "INDEXED",
                dataOrigin == null || dataOrigin.isBlank() ? "PRODUCTION" : dataOrigin,
                sourceVersion == null || sourceVersion.isBlank() ? "knowledge-v1" : sourceVersion,
                now, now);
        repository.insertDocument(document, content);
        repository.insertChunks(chunks(document, content, knowledgePointId, now));
        return document;
    }

    private List<KnowledgeChunk> chunks(KnowledgeDocument document, String content,
                                        String knowledgePointId, Instant now) {
        List<KnowledgeChunk> chunks = new ArrayList<>();
        int chunkSize = 800;
        int index = 0;
        for (int start = 0; start < content.length(); start += chunkSize) {
            int end = Math.min(content.length(), start + chunkSize);
            String piece = content.substring(start, end).trim();
            if (piece.isBlank()) continue;
            chunks.add(new KnowledgeChunk(
                    document.documentId() + "-chunk-" + index, document.documentId(), document.courseId(),
                    knowledgePointId, document.title(), piece, index, embedding.embed(piece),
                    List.of("courseId=" + document.courseId(), "knowledgePointId=" + String.valueOf(knowledgePointId)),
                    document.sourceVersion(), now));
            index++;
        }
        if (chunks.isEmpty()) {
            throw new IllegalArgumentException("Document content produced no chunks");
        }
        return chunks;
    }
}
