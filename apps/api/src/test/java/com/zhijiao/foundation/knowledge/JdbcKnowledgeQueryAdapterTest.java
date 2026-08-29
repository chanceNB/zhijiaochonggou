package com.zhijiao.foundation.knowledge;

import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class JdbcKnowledgeQueryAdapterTest {

    @Test
    void queriesThroughEmbeddingPortAndUsesLexicalFallbackWhenDimensionsDiffer() {
        KnowledgeRepository repository = mock(KnowledgeRepository.class);
        EmbeddingPort embedding = mock(EmbeddingPort.class);
        when(embedding.embed("BFS queue")).thenReturn(List.of(1.0, 0.0));
        when(embedding.dimension()).thenReturn(2);
        when(repository.findChunks("course-1", "kp-1")).thenReturn(List.of(
                new KnowledgeRepository.StoredChunk("chunk-1", "doc-1", "Graph notes",
                        "BFS queue traversal", List.of(1.0, 0.0, 0.0), List.of())));

        JdbcKnowledgeQueryAdapter adapter = new JdbcKnowledgeQueryAdapter(repository, embedding);

        List<KnowledgeSearchResult> results = adapter.search("course-1", "kp-1", "BFS queue", 4);

        assertThat(results).hasSize(1);
        assertThat(results.get(0).score()).isEqualTo(0.25);
    }
}
