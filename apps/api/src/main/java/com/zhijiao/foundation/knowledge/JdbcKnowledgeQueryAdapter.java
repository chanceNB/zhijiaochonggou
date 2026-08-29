package com.zhijiao.foundation.knowledge;

import org.springframework.stereotype.Component;

import java.util.Comparator;
import java.util.List;

@Component
public class JdbcKnowledgeQueryAdapter implements KnowledgeQueryPort {
    private final KnowledgeRepository repository;
    private final EmbeddingPort embedding;

    public JdbcKnowledgeQueryAdapter(KnowledgeRepository repository, EmbeddingPort embedding) {
        this.repository = repository;
        this.embedding = embedding;
    }

    @Override
    public List<KnowledgeSearchResult> search(String courseId, String knowledgePointId, String query, int topK) {
        if (courseId == null || courseId.isBlank() || query == null || query.isBlank()) {
            return List.of();
        }
        try {
            List<Double> queryVector = validateVector(embedding.embed(query), embedding.dimension());
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
        double cosine = queryVector.size() == chunk.embedding().size()
                ? cosine(queryVector, chunk.embedding()) : 0.0;
        String lowerQuery = query.toLowerCase();
        String lowerContent = chunk.content().toLowerCase();
        double lexical = lowerContent.contains(lowerQuery) ? 1.0 : 0.0;
        return Math.min(1.0, 0.75 * Math.max(0.0, cosine) + 0.25 * lexical);
    }

    private double cosine(List<Double> left, List<Double> right) {
        int size = left.size();
        double dot = 0.0, leftNorm = 0.0, rightNorm = 0.0;
        for (int index = 0; index < size; index++) {
            dot += left.get(index) * right.get(index);
            leftNorm += left.get(index) * left.get(index);
            rightNorm += right.get(index) * right.get(index);
        }
        return leftNorm == 0.0 || rightNorm == 0.0 ? 0.0 : dot / Math.sqrt(leftNorm * rightNorm);
    }

    private List<Double> validateVector(List<Double> vector, int configuredDimension) {
        if (vector == null || vector.isEmpty()) {
            throw new EmbeddingUnavailableException("Embedding provider returned an empty vector");
        }
        if (configuredDimension > 0 && vector.size() != configuredDimension) {
            throw new EmbeddingDimensionMismatchException(
                    "Embedding dimension " + vector.size() + " does not match configured dimension " + configuredDimension);
        }
        if (vector.stream().anyMatch(value -> value == null || !Double.isFinite(value))) {
            throw new EmbeddingUnavailableException("Embedding provider returned a non-finite vector");
        }
        return List.copyOf(vector);
    }

    private String excerpt(String content) {
        return content.length() <= 320 ? content : content.substring(0, 320);
    }
}
