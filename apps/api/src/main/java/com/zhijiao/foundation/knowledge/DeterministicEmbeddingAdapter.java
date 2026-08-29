package com.zhijiao.foundation.knowledge;

import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;

import java.util.List;

/** Offline deterministic embedding for tests and explicitly selected local fallback mode. */
@Component
@ConditionalOnProperty(name = "app.coach.rag.embedding.provider", havingValue = "deterministic", matchIfMissing = false)
public class DeterministicEmbeddingAdapter implements EmbeddingPort {
    private static final int DEFAULT_DIMENSION = 32;
    private final DeterministicEmbedding delegate;
    private final int dimension;

    public DeterministicEmbeddingAdapter(EmbeddingProperties properties) {
        this.dimension = properties.getDimension() > 0 ? properties.getDimension() : DEFAULT_DIMENSION;
        this.delegate = new DeterministicEmbedding(dimension);
    }

    @Override
    public List<Double> embed(String text) {
        return delegate.embed(text);
    }

    @Override
    public int dimension() {
        return dimension;
    }
}
