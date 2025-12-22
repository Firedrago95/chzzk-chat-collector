package io.github.hypecycle.chatpipeline.analyzer.domain;

public record EmotionResult(
        String sentiment,
        double score,
        String summary
) {}
