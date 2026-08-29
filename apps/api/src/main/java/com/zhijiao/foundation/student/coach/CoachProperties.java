package com.zhijiao.foundation.student.coach;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "app.coach")
public class CoachProperties {
    private final Llm llm = new Llm();
    private final Rag rag = new Rag();

    public Llm getLlm() { return llm; }
    public Rag getRag() { return rag; }

    public static class Llm {
        private String provider = "openai-compatible";
        private String baseUrl = "https://api.openai.com/v1";
        private String apiKey = "";
        private String model = "gpt-4o-mini";
        private int timeoutSeconds = 30;
        private int maxDiagnosticRetries = 1;
        private String promptVersion = "coach-prompt-v1";

        public String getProvider() { return provider; }
        public void setProvider(String provider) { this.provider = provider; }
        public String getBaseUrl() { return baseUrl; }
        public void setBaseUrl(String baseUrl) { this.baseUrl = baseUrl; }
        public String getApiKey() { return apiKey; }
        public void setApiKey(String apiKey) { this.apiKey = apiKey; }
        public String getModel() { return model; }
        public void setModel(String model) { this.model = model; }
        public int getTimeoutSeconds() { return timeoutSeconds; }
        public void setTimeoutSeconds(int timeoutSeconds) { this.timeoutSeconds = timeoutSeconds; }
        public int getMaxDiagnosticRetries() { return maxDiagnosticRetries; }
        public void setMaxDiagnosticRetries(int maxDiagnosticRetries) { this.maxDiagnosticRetries = maxDiagnosticRetries; }
        public String getPromptVersion() { return promptVersion; }
        public void setPromptVersion(String promptVersion) { this.promptVersion = promptVersion; }
    }

    public static class Rag {
        private int topK = 4;
        public int getTopK() { return topK; }
        public void setTopK(int topK) { this.topK = topK; }
    }
}
