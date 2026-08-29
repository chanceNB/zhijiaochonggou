package com.zhijiao.foundation.student.coach;

public record LlmRequest(String systemPrompt, String userPrompt, boolean structuredOutput) {
}
