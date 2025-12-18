package io.github.hypecycle.chatpipeline.connector.chzzk.websocket;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.github.hypecycle.chatpipeline.buffer.ChatBuffer;
import io.github.hypecycle.chatpipeline.connector.chzzk.dto.request.ChzzkAuthRequest;
import io.github.hypecycle.chatpipeline.connector.chzzk.dto.response.ChzzkResponseMessage;
import io.github.hypecycle.chatpipeline.connector.chzzk.dto.response.ChzzkResponseMessage.Body;
import io.github.hypecycle.chatpipeline.connector.chzzk.mapper.ChzzkMessageMapper;
import io.github.hypecycle.chatpipeline.domain.ChatMessage;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class ChzzkMessageHandler {

    private final ChzzkMessageMapper chzzkMessageMapper;
    private final ObjectMapper objectMapper;
    private final ChatBuffer chatBuffer;


    public Optional<String> handleMessage(String message) {
        try {
            ChzzkResponseMessage response = objectMapper.readValue(message, ChzzkResponseMessage.class);

            return switch (response.cmd()) {
                case CONNECT_ACK -> {
                    log.info(">>> 치지직 웹소켓 서버 접속 승인 완료");
                    yield Optional.empty();
                }
                case PING -> {
                    log.info("<<< 서버 Ping 수신 (cmd: 0)");
                    yield Optional.of(createPongPacket());
                }
                case PONG -> {
                    log.info("<<< [수신] 서버 Pong(cmd: 10000) - 내 핑에 대답함");
                    yield Optional.empty();
                }
                case CHAT, DONATION -> {
                    if (response.bdy().isArray()) {
                        for (JsonNode node : response.bdy()) {
                            Body bodyDto = objectMapper.treeToValue(node, Body.class);
                            ChatMessage chatMessage = chzzkMessageMapper.parse(bodyDto);
                            chatBuffer.produce(chatMessage);
                            log.info(">>> {}", chatMessage);
                        }
                    }
                    yield Optional.empty();
                }
            };
        } catch (Exception e) {
            log.error("메시지 파싱 실패: {}", message, e);
            return Optional.empty();
        }
    }

    public String createAuthPacket(String chatChannelId, String accessToken) {
        try {
            ChzzkAuthRequest.AuthRequestBody body = new ChzzkAuthRequest.AuthRequestBody(
                null, 2001, "Google Chrome/142.0.0.0", accessToken, "4.9.3", "ko",
                "macOS/10.15.7", "Asia/Seoul", "READ"
            );
            ChzzkAuthRequest request = new ChzzkAuthRequest(
                "3", 100, "game", chatChannelId, 1, body
            );
            return this.objectMapper.writeValueAsString(request);
        } catch (JsonProcessingException e) {
            throw new RuntimeException("JSON 변환 실패", e);
        }
    }

    public String createPongPacket() {
        return "{\"cmd\": 10000, \"ver\": 2, \"svcid\": \"game\", \"bdy\": {}}";
    }

}
