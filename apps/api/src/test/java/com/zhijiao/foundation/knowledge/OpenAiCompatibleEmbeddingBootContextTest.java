package com.zhijiao.foundation.knowledge;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@SpringBootTest(properties = {
        "app.coach.rag.embedding.provider=openai-compatible",
        "app.coach.rag.embedding.api-key=",
        "app.coach.rag.embedding.base-url=http://localhost:9999/v1"
})
class OpenAiCompatibleEmbeddingBootContextTest {
    @Autowired
    private EmbeddingPort embeddingPort;

    @Test
    void defaultProductionAdapterLoadsWithoutCredentialsAndFailsOnlyOnUse() {
        assertThat(embeddingPort).isInstanceOf(OpenAiCompatibleEmbeddingAdapter.class);
        assertThatThrownBy(() -> embeddingPort.embed("BFS queue"))
                .isInstanceOf(EmbeddingUnavailableException.class)
                .hasMessageContaining("credentials");
    }
}
