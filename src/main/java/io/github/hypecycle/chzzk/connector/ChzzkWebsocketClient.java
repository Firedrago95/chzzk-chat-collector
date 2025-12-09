package io.github.hypecycle.chzzk.connector;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.github.hypecycle.chzzk.connector.dto.request.ChzzkAuthRequest;
import java.net.URI;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import org.java_websocket.client.WebSocketClient;
import org.java_websocket.handshake.ServerHandshake;

public class ChzzkWebsocketClient extends WebSocketClient {

    private final String chatChannelId;
    private final String accessToken;

    private final ObjectMapper objectMapper = new ObjectMapper();

    private ScheduledExecutorService pingScheduler;

    public ChzzkWebsocketClient(URI serverUri, String chatChannelId, String accessToken) {
        super(serverUri);
        this.chatChannelId = chatChannelId;
        this.accessToken = accessToken;
    }

    @Override
    public void onOpen(ServerHandshake handshakeData) {
        System.out.println(">>> Websocket 연결 성공! 인증 패킷 전송 시작...");

        String authPacket = createAuthPacket(chatChannelId, accessToken);
        if (authPacket != null) {
            this.send(authPacket);
        }

        pingScheduler = Executors.newSingleThreadScheduledExecutor();
        pingScheduler.scheduleAtFixedRate(this::sendActivePing, 20, 20, TimeUnit.SECONDS);
    }

    @Override
    public void onMessage(String message) {
        try {
            JsonNode node = objectMapper.readTree(message);
            int cmd = node.get("cmd").asInt();

            switch (cmd) {
                case 0:
                    System.out.println("<<< 서버 Ping 수신 (cmd: 0)");
                    sendPong();
                    break;

                case 10000:
                    System.out.println("<<< [수신] 서버 Pong(cmd: 10000) - 내 핑에 대답함");
                    break;

                case 93101:
                    JsonNode bdy = node.get("bdy");

                    if (bdy != null && bdy.isArray()) {
                        for (JsonNode chatItem : bdy) {
                            String messageType = chatItem.path("msgTypeCode").asText();
                            String content = chatItem.path("msg").asText();

                            System.out.println("[****] " + content);
                        }
                    }
                    break;

                default:
                    break;
            }
        } catch (Exception e) {
            System.err.println("메시지 파싱 실패: " + e.getMessage());
        }
    }

    @Override
    public void onClose(int code, String reason, boolean remote) {
        System.out.println(">>> 연결 끊김: " + reason);
        if (pingScheduler != null && !pingScheduler.isShutdown()) {
            pingScheduler.shutdown();
        }
    }

    @Override
    public void onError(Exception ex) {
        ex.printStackTrace();
    }

    // ---------------- Helper Methods ----------------

    private void sendPong() {
        String pongPacket = "{\"cmd\": 10000, \"ver\": 2, \"svcid\": \"game\", \"bdy\": {}}";
        this.send(pongPacket);
    }

    private void sendActivePing() {
        String pingPacket = "{\"cmd\": 0, \"ver\": 2}";
        try {
            this.send(pingPacket);
        } catch (Exception e) {
            System.err.println("Heartbeat 전송 실패");
        }
    }

    private String createAuthPacket(String chatChannelId, String accessToken) {
        try {
            ChzzkAuthRequest.AuthRequestBody body = new ChzzkAuthRequest.AuthRequestBody(
                    null,
                    2001,
                    "Google Chrome/142.0.0.0",
                    accessToken,
                    "4.9.3",
                    "ko",
                    "macOS/10.15.7",
                    "Asia/Seoul",
                    "READ"
            );
            ChzzkAuthRequest request = new ChzzkAuthRequest(
                    "3", 100, "game", chatChannelId, 1, body
            );
            return this.objectMapper.writeValueAsString(request);
        } catch (JsonProcessingException e) {
            throw new RuntimeException("JSON 변환 실패", e);
        }
    }
}
