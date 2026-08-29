package com.zhijiao.foundation.knowledge;

import org.springframework.stereotype.Component;

import java.util.Comparator;
import java.util.List;

@Component
public class JdbcKnowledgeQueryAdapter implements KnowledgeQueryPort {
    private final KnowledgeRepository repository;
    private final DeterministicEmbedding embedding = new DeterministicEmbedding(32);

    public JdbcKnowledgeQueryAdapter(KnowledgeRepository repository) {
        this.repository = repository;
    }

    @Override
    public List<KnowledgeSearchResult> search(String courseId, String knowledgePointId, String query, int topK) {
        if (courseId == null || courseId.isBlank() || query == null || query.isBlank()) {
            return List.of();
        }
        try {
            List<Double> queryVector = embedding.embed(query);
            return repository.findChunks(courseId, knowledgePointId).stream()
                    .map(chunk -> new KnowledgeSearchResult(chunk.chunkId(), chunk.documentId(), chunk.title(),
                            excerpt(chunk.content()), score(query, queryVector, chunk), chunk.metadata()))
                    .filter(result -> result.score() > 0.0)
                    .sorted(Comparator.comparingDouble(KnowledgeSearchResult::score).reversed()
                            .thenComparing(KnowledgeSearchResult::chunkId))
                    .limit(Math.max(0, topK))
                    .toList();
        } catch (RuntimeException exception) {
            if (exception instanceof KnowledgeUnavailableException) throw exception;
            throw new KnowledgeUnavailableException("Knowledge search is unavailable", exception);
        }
    }

    private double score(String query, List<Double> queryVector, KnowledgeRepository.StoredChunk chunk) {
        double cosine = cosine(queryVector, chunk.embedding());
        String lowerQuery = query.toLowerCase();
        String lowerContent = chunk.content().toLowerCase();
        double lexical = lowerContent.contains(lowerQuery) ? 1.0 : 0.0;
        return Math.min(1.0, 0.75 * Math.max(0.0, cosine) + 0.25 * lexical);
    }

    private double cosine(List<Double> left, List<Double> right) {
        int size = Math.min(left.size(), right.size());
        double dot = 0.0, leftNorm = 0.0, rightNorm = 0.0;
        for (int index = 0; index < size; index++) {
            dot += left.get(index) * right.get(index);
            leftNorm += left.get(index) * left.get(index);
            rightNorm += right.get(index) * right.get(index);
        }
        return leftNorm == 0.0 || rightNorm == 0.0 ? 0.0 : dot / Math.sqrt(leftNorm * rightNorm);
    }

    private String excerpt(String content) {
        return content.length() <= 320 ? content : content.substring(0, 320);
    }
}
