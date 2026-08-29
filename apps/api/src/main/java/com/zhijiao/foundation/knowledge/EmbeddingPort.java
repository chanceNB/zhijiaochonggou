package com.zhijiao.foundation.knowledge;

import java.util.List;

/** Semantic embedding boundary shared by ingestion and retrieval. */
public interface EmbeddingPort {
    List<Double> embed(String text);

    /** Returns the configured dimension, or -1 when the provider dimension is dynamic. */
    default int dimension() {
        return -1;
    }
}
