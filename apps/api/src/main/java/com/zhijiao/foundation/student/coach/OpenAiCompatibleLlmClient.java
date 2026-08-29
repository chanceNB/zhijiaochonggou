package com.zhijiao.foundation.student.coach;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.web.client.ResourceAccessException;
import org.springframework.web.client.RestClient;

import java.time.Duration;
import java.util.List;
import java.util.Map;

@Component
@ConditionalOnProperty(name = "app.coach.llm.provider", havingValue = "openai-compatible", matchIfMissing = true)
public class OpenAiCompatibleLlmClient implements LlmPort {
    private final RestClient restClient;
    private final ObjectMapper objectMapper;
    private final CoachProperties.Llm properties;

    public OpenAiCompatibleLlmClient(ObjectMapper objectMapper, CoachProperties properties) {
        this.properties = properties.getLlm();
        SimpleClientHttpRequestFactory requestFactory = new SimpleClientHttpRequestFactory();
        Duration timeout = Duration.ofSeconds(Math.max(1, this.properties.getTimeoutSeconds()));
        requestFactory.setConnectTimeout(timeout);
        requestFactory.setReadTimeout(timeout);
        this.restClient = RestClient.builder()
                .requestFactory(requestFactory)
                .baseUrl(this.properties.getBaseUrl())
                .build();
        this.objectMapper = objectMapper;
    }

    @Override
    public LlmResponse complete(LlmRequest request) {
        if (properties.getApiKey() == null || properties.getApiKey().isBlank()) {
            throw new LlmUnavailableException("LLM credentials are not configured");
        }
        Map<String, Object> body = Map.of(
                "model", properties.getModel(),
                "messages", List.of(
                        Map.of("role", "system", "content", request.systemPrompt()),
                        Map.of("role", "user", "content", request.userPrompt())),
                "temperature", 0.0,
                "response_format", request.structuredOutput()
                        ? Map.of("type", "json_object") : Map.of("type", "text"));
        try {
            String payload = restClient.post().uri("/chat/completions")
                    .contentType(MediaType.APPLICATION_JSON)
                    .header("Authorization", "Bearer " + properties.getApiKey())
                    .body(body)
                    .retrieve()
                    .body(String.class);
            JsonNode root = objectMapper.readTree(payload);
            String content = root.path("choices").path(0).path("message").path("content").asText(null);
            if (content == null || content.isBlank()) {
                throw new LlmUnavailableException("LLM response did not contain content");
            }
            return new LlmResponse(content, properties.getProvider(), properties.getModel(), properties.getPromptVersion());
        } catch (ResourceAccessException exception) {
            if (exception.getCause() instanceof java.net.SocketTimeoutException) {
                throw new LlmTimeoutException("LLM request timed out", exception);
            }
            throw new LlmUnavailableException("LLM request failed", exception);
        } catch (LlmUnavailableException exception) {
            throw exception;
        } catch (Exception exception) {
            throw new LlmUnavailableException("LLM response could not be parsed", exception);
        }
    }
}
