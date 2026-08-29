package com.zhijiao.foundation.knowledge;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.test.web.client.MockRestServiceServer;
import org.springframework.web.client.RestClient;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.content;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.header;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.method;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.requestTo;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withSuccess;

class OpenAiCompatibleEmbeddingAdapterTest {

    @Test
    void mapsOpenAiCompatibleEmbeddingRequestAndResponse() {
        EmbeddingProperties properties = properties(3);
        RestClient.Builder builder = RestClient.builder()
                .baseUrl("http://embedding.test/v1")
                .requestFactory(new SimpleClientHttpRequestFactory());
        MockRestServiceServer server = MockRestServiceServer.bindTo(builder).build();
        server.expect(requestTo("http://embedding.test/v1/embeddings"))
                .andExpect(method(HttpMethod.POST))
                .andExpect(header("Authorization", "Bearer test-key"))
                .andExpect(content().json("{\"model\":\"text-embedding-test\",\"input\":\"BFS queue\"}"))
                .andRespond(withSuccess("{\"data\":[{\"embedding\":[0.1,0.2,0.3]}]}",
                        MediaType.APPLICATION_JSON));

        OpenAiCompatibleEmbeddingAdapter adapter = new OpenAiCompatibleEmbeddingAdapter(
                builder.build(), new ObjectMapper(), properties);

        assertThat(adapter.embed("BFS queue")).containsExactly(0.1, 0.2, 0.3);
        assertThat(adapter.dimension()).isEqualTo(3);
        server.verify();
    }

    @Test
    void rejectsProviderVectorWithConfiguredDimensionMismatch() {
        EmbeddingProperties properties = properties(3);
        RestClient.Builder builder = RestClient.builder()
                .baseUrl("http://embedding.test/v1")
                .requestFactory(new SimpleClientHttpRequestFactory());
        MockRestServiceServer server = MockRestServiceServer.bindTo(builder).build();
        server.expect(requestTo("http://embedding.test/v1/embeddings"))
                .andRespond(withSuccess("{\"data\":[{\"embedding\":[0.1,0.2]}]}",
                        MediaType.APPLICATION_JSON));
        OpenAiCompatibleEmbeddingAdapter adapter = new OpenAiCompatibleEmbeddingAdapter(
                builder.build(), new ObjectMapper(), properties);

        assertThatThrownBy(() -> adapter.embed("BFS queue"))
                .isInstanceOf(EmbeddingDimensionMismatchException.class);
        server.verify();
    }

    @Test
    void failsWithoutCredentialBeforeCallingProvider() {
        EmbeddingProperties properties = properties(3);
        properties.setApiKey("");
        RestClient.Builder builder = RestClient.builder()
                .baseUrl("http://embedding.test/v1")
                .requestFactory(new SimpleClientHttpRequestFactory());
        MockRestServiceServer server = MockRestServiceServer.bindTo(builder).build();
        OpenAiCompatibleEmbeddingAdapter adapter = new OpenAiCompatibleEmbeddingAdapter(
                builder.build(), new ObjectMapper(), properties);

        assertThatThrownBy(() -> adapter.embed("BFS queue"))
                .isInstanceOf(EmbeddingUnavailableException.class)
                .hasMessageContaining("credentials");
        server.verify();
    }

    private EmbeddingProperties properties(int dimension) {
        EmbeddingProperties properties = new EmbeddingProperties();
        properties.setBaseUrl("http://embedding.test/v1");
        properties.setApiKey("test-key");
        properties.setModel("text-embedding-test");
        properties.setDimension(dimension);
        return properties;
    }
}
