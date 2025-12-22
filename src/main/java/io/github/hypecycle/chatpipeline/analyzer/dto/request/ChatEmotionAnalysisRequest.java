package io.github.hypecycle.chatpipeline.analyzer.dto.request;

import java.util.Map;

public record ChatEmotionAnalysisRequest(
    String model,
    String prompt,
    boolean stream,
    Map<String, Object> options
) {
    public static ChatEmotionAnalysisRequest from(String model, String prompt) {
        Map<String, Object> temperature = Map.of("temperature", 0.1);
        return new ChatEmotionAnalysisRequest(model, prompt, false, temperature);
    }
}
