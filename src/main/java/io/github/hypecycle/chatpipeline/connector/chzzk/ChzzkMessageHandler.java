package io.github.hypecycle.chatpipeline.connector.chzzk;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.github.hypecycle.chatpipeline.connector.chzzk.dto.request.ChzzkAuthRequest;
import io.github.hypecycle.chatpipeline.connector.chzzk.dto.response.ChzzkResponseMessage;
import io.github.hypecycle.chatpipeline.domain.ChatMessage;
import java.util.Optional;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.java_websocket.client.WebSocketClient;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class ChzzkMessageHandler {

    private final ChzzkMessageMapper chzzkMessageMapper;
    private final ObjectMapper objectMapper;

    public void handleOpen(WebSocketClient client, String chatChannelId, String accessToken,
        ScheduledExecutorService pingScheduler) {
        log.info(">>> Websocket 연결 성공! 인증 패킷 전송 시작...");

        String authPacket = createAuthPacket(chatChannelId, accessToken);
        client.send(authPacket);

        pingScheduler.scheduleAtFixedRate(() -> sendActivePing(client), 20, 20, TimeUnit.SECONDS);
    }

    public Optional<String> handleMessage(String message) {
        try {
            ChzzkResponseMessage response = objectMapper.readValue(message,
                ChzzkResponseMessage.class);

            return switch (response.cmd()) {
                case PING -> {
                    log.info("<<< 서버 Ping 수신 (cmd: 0)");
                    yield Optional.of(createPongPacket());
                }
                case PONG -> {
                    log.info("<<< [수신] 서버 Pong(cmd: 10000) - 내 핑에 대답함");
                    yield Optional.empty();
                }
                case CHAT -> {
                    for (ChzzkResponseMessage.Body chatItem : response.bdy()) {
                        ChatMessage chatMessage = chzzkMessageMapper.parse(chatItem);
                        log.info(chatMessage.toString());
                    }
                    yield Optional.empty();
                }
                default -> Optional.empty();
            };
        } catch (Exception e) {
            log.error("메시지 파싱 실패: {}", message, e);
            return Optional.empty();
        }
    }

    public void handleClose(String reason, ScheduledExecutorService pingScheduler) {
        log.info(">>> 연결 끊김: {}", reason);
        if (pingScheduler != null && !pingScheduler.isShutdown()) {
            pingScheduler.shutdown();
        }
    }

    public void handleError(Exception ex) {
        log.error("Websocket 오류 발생", ex);
    }

    private void sendActivePing(WebSocketClient client) {
        String pingPacket = "{\"cmd\": 0, \"ver\": 2}";
        try {
            client.send(pingPacket);
        } catch (Exception e) {
            log.error("Heartbeat 전송 실패", e);
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
