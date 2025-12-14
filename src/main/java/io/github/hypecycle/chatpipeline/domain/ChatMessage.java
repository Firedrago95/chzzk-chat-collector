package io.github.hypecycle.chatpipeline.domain;

import java.time.LocalDateTime;
import java.util.Map;

public record ChatMessage(
        Platform platform,
        MessageType messageType,
        Author author,
        String message,
        LocalDateTime time,
        Map<String, Object> headers
) {
    public boolean hasHeader(String key) {
        return headers.containsKey(key);
    }
}
