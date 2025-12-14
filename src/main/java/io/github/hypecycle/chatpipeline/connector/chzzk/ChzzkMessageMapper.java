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
import java.util.Collections;
import org.springframework.stereotype.Component;

@Component
public class ChzzkMessageMapper {

    private final ObjectMapper objectMapper = new ObjectMapper();

    public ChatMessage parse(ChzzkResponseMessage.Body content) {
        try {
            JsonNode profile = objectMapper.readTree(content.profile());

            Author author = new Author(
                    profile.get("userIdHash").asText(),
                    profile.get("nickname").asText()
            );
            MessageType messageType = content.msgTypeCode() == 10 ? MessageType.DONATION
                    : MessageType.NORMAL;

            return new ChatMessage(
                    Platform.CHZZK,
                    messageType,
                    author,
                    content.msg(),
                    LocalDateTime.ofInstant(Instant.ofEpochMilli(content.msgTime()),
                            ZoneId.systemDefault()),
                    Collections.emptyMap()
            );

        } catch (JsonProcessingException e) {
            throw new RuntimeException(e);
        }
    }
}
