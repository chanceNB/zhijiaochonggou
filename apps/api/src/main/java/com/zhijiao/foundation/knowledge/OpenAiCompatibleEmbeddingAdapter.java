package com.zhijiao.foundation.knowledge;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.http.MediaType;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.stereotype.Component;
import org.springframework.web.client.ResourceAccessException;
import org.springframework.web.client.RestClient;

import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Component
@ConditionalOnProperty(name = "app.coach.rag.embedding.provider", havingValue = "openai-compatible")
public class OpenAiCompatibleEmbeddingAdapter implements EmbeddingPort {
    private final RestClient restClient;
    private final ObjectMapper objectMapper;
    private final EmbeddingProperties properties;

    public OpenAiCompatibleEmbeddingAdapter(ObjectMapper objectMapper, EmbeddingProperties properties) {
        this(buildRestClient(properties), objectMapper, properties);
    }

    OpenAiCompatibleEmbeddingAdapter(RestClient restClient, ObjectMapper objectMapper, EmbeddingProperties properties) {
        this.restClient = restClient;
        this.objectMapper = objectMapper;
        this.properties = properties;
        validateConfiguration();
    }

    @Override
    public List<Double> embed(String text) {
        if (text == null || text.isBlank()) {
            throw new IllegalArgumentException("Embedding input must not be blank");
        }
        if (properties.getApiKey() == null || properties.getApiKey().isBlank()) {
            throw new EmbeddingUnavailableException("Embedding credentials are not configured");
        }
        try {
            String payload = restClient.post().uri("/embeddings")
                    .contentType(MediaType.APPLICATION_JSON)
                    .header("Authorization", "Bearer " + properties.getApiKey())
                    .body(Map.of("model", properties.getModel(), "input", text))
                    .retrieve()
                    .body(String.class);
            JsonNode embeddingNode = objectMapper.readTree(payload).path("data").path(0).path("embedding");
            if (!embeddingNode.isArray() || embeddingNode.isEmpty()) {
                throw new EmbeddingUnavailableException("Embedding response did not contain a vector");
            }
            List<Double> vector = new ArrayList<>(embeddingNode.size());
            for (JsonNode value : embeddingNode) {
                if (!value.isNumber() || !Double.isFinite(value.asDouble())) {
                    throw new EmbeddingUnavailableException("Embedding response contained a non-finite value");
                }
                vector.add(value.asDouble());
            }
            validateDimension(vector.size());
            return List.copyOf(vector);
        } catch (ResourceAccessException exception) {
            throw new EmbeddingUnavailableException("Embedding request failed", exception);
        } catch (EmbeddingUnavailableException exception) {
            throw exception;
        } catch (Exception exception) {
            throw new EmbeddingUnavailableException("Embedding response could not be parsed", exception);
        }
    }

    @Override
    public int dimension() {
        return properties.getDimension() > 0 ? properties.getDimension() : -1;
    }

    private void validateConfiguration() {
        if (properties.getBaseUrl() == null || properties.getBaseUrl().isBlank()) {
            throw new IllegalArgumentException("Embedding base URL must not be blank");
        }
        if (properties.getModel() == null || properties.getModel().isBlank()) {
            throw new IllegalArgumentException("Embedding model must not be blank");
        }
        if (properties.getDimension() < 0) {
            throw new IllegalArgumentException("Embedding dimension must be zero or positive");
        }
        if (properties.getTimeoutSeconds() < 1) {
            throw new IllegalArgumentException("Embedding timeout must be at least one second");
        }
    }

    private void validateDimension(int actualDimension) {
        int expectedDimension = properties.getDimension();
        if (expectedDimension > 0 && actualDimension != expectedDimension) {
            throw new EmbeddingDimensionMismatchException(
                    "Embedding dimension " + actualDimension + " does not match configured dimension " + expectedDimension);
        }
    }

    private static RestClient buildRestClient(EmbeddingProperties properties) {
        SimpleClientHttpRequestFactory requestFactory = new SimpleClientHttpRequestFactory();
        requestFactory.setConnectTimeout(Duration.ofSeconds(Math.max(1, properties.getTimeoutSeconds())));
        requestFactory.setReadTimeout(Duration.ofSeconds(Math.max(1, properties.getTimeoutSeconds())));
        return RestClient.builder().requestFactory(requestFactory).baseUrl(properties.getBaseUrl()).build();
    }
}
