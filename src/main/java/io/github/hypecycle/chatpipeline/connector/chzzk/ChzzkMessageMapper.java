package io.github.hypecycle.chatpipeline.connector.chzzk;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.github.hypecycle.chatpipeline.connector.chzzk.dto.response.ChzzkResponseMessage;
import io.github.hypecycle.chatpipeline.domain.Author;
import io.github.hypecycle.chatpipeline.domain.ChatMessage;
import io.github.hypecycle.chatpipeline.domain.MessageType;
import io.github.hypecycle.chatpipeline.domain.Platform;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.HashMap;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class ChzzkMessageMapper {

    private final ObjectMapper objectMapper;

    public ChatMessage parse(ChzzkResponseMessage.Body content) {
        try {
            Author author = toAuthor(content.profile());
            MessageType messageType = toMessageType(content.msgTypeCode());
            Map<String, Object> headers = extractHeaders(content.extras(), messageType);

            return new ChatMessage(
                Platform.CHZZK,
                messageType,
                author,
                content.msg(),
                toLocalDateTime(content.msgTime()),
                headers
            );

        } catch (JsonProcessingException e) {
            throw new RuntimeException("Failed to parse Chzzk message body: " + e.getMessage(), e);
        }
    }

    private Author toAuthor(String profileJson) throws JsonProcessingException {
        JsonNode profile = objectMapper.readTree(profileJson);
        return new Author(
            profile.get("userIdHash").asText(),
            profile.get("nickname").asText()
        );
    }

    private MessageType toMessageType(int msgTypeCode) {
        return msgTypeCode == 10 ? MessageType.DONATION : MessageType.NORMAL;
    }

    private LocalDateTime toLocalDateTime(long epochMillis) {
        return LocalDateTime.ofInstant(Instant.ofEpochMilli(epochMillis),
            ZoneId.systemDefault());
    }

    private Map<String, Object> extractHeaders(String extrasJson, MessageType messageType)
        throws JsonProcessingException {
        Map<String, Object> headers = new HashMap<>();
        JsonNode extras = objectMapper.readTree(extrasJson);

        if (extras.has("osType")) {
            headers.put("osType", extras.get("osType").asText());
        }
        if (extras.has("chatType")) {
            headers.put("chatType", extras.get("chatType").asText());
        }

        if (messageType == MessageType.DONATION) {
            extractDonationHeaders(extras, headers);
        }

        return headers;
    }

    private void extractDonationHeaders(JsonNode extras, Map<String, Object> headers) {
        if (extras.has("payAmount")) {
            headers.put("payAmount", extras.get("payAmount").asLong());
        }
        if (extras.has("isAnonymous")) {
            headers.put("isAnonymous", extras.get("isAnonymous").asBoolean());
        }
    }
}
