package com.zhijiao.foundation.knowledge;

import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class KnowledgeIngestionTest {

    @Test
    void deterministicEmbeddingAndLexicalSearchProduceStableCitationInput() {
        DeterministicEmbedding embedding = new DeterministicEmbedding(16);
        KnowledgeSearchResult result = new KnowledgeSearchResult(
                "chunk-1", "doc-1", "图遍历讲义", "BFS 使用队列维护待访问顶点。", 0.91,
                List.of("knowledgePointId=kp-graph-bfs-dfs"));

        assertThat(embedding.embed("BFS 使用队列")).isEqualTo(embedding.embed("BFS 使用队列"));
        assertThat(result.toCitation().chunkId()).isEqualTo("chunk-1");
        assertThat(result.toCitation().title()).isEqualTo("图遍历讲义");
    }
}
