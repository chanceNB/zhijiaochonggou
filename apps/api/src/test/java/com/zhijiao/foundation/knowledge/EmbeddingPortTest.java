package com.zhijiao.foundation.knowledge;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class EmbeddingPortTest {

    @Test
    void deterministicAdapterIsRepeatableAndReportsItsConfiguredDimension() {
        EmbeddingProperties properties = new EmbeddingProperties();
        properties.setDimension(16);
        DeterministicEmbeddingAdapter adapter = new DeterministicEmbeddingAdapter(properties);

        assertThat(adapter.embed("BFS 使用队列")).isEqualTo(adapter.embed("BFS 使用队列"));
        assertThat(adapter.embed("BFS 使用队列")).hasSize(16);
        assertThat(adapter.dimension()).isEqualTo(16);
    }
}
