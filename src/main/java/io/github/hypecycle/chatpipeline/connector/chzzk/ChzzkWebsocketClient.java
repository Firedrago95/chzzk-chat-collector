package io.github.hypecycle.chatpipeline.connector.chzzk;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.github.hypecycle.chatpipeline.connector.chzzk.dto.request.ChzzkAuthRequest;
import io.github.hypecycle.chatpipeline.connector.chzzk.dto.response.ChzzkCommand;
import io.github.hypecycle.chatpipeline.connector.chzzk.dto.response.ChzzkResponseMessage;
import io.github.hypecycle.chatpipeline.domain.ChatMessage;
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
    private final ChzzkMessageMapper chzzkMessageMapper;

    private ScheduledExecutorService pingScheduler;

    public ChzzkWebsocketClient(URI serverUri, String chatChannelId, String accessToken,
            ChzzkMessageMapper chzzkMessageMapper) {
        super(serverUri);
        this.chatChannelId = chatChannelId;
        this.accessToken = accessToken;
        this.chzzkMessageMapper = chzzkMessageMapper;
    }

    @Override
    public void onOpen(ServerHandshake handshakeData) {
        System.out.println(">>> Websocket 연결 성공! 인증 패킷 전송 시작...");

        // 1. 인증 패킷 전송
        String authPacket = createAuthPacket(chatChannelId, accessToken);
        if (authPacket != null) {
            this.send(authPacket);
        }

        // 2. 스케줄러 시작 (20초마다 능동적으로 생존 신고)
        pingScheduler = Executors.newSingleThreadScheduledExecutor();
        pingScheduler.scheduleAtFixedRate(this::sendActivePing, 20, 20, TimeUnit.SECONDS);
    }

    @Override
    public void onMessage(String message) {
        try {
            ChzzkResponseMessage response = objectMapper.readValue(message,
                    ChzzkResponseMessage.class);

            switch (response.cmd()) {
                case PING:
                    System.out.println("<<< 서버 Ping 수신 (cmd: 0)");
                    sendPong(); // "응 살아있어" (Pong) 대답
                    break;

                case PONG: // [서버 -> 나] ㅇㅇ 너 살아있네 (내 핑에 대한 대답)
                    System.out.println("<<< [수신] 서버 Pong(cmd: 10000) - 내 핑에 대답함");
                    break;

                case CHAT: // 채팅 메시지
                    for (ChzzkResponseMessage.Body chatItem : response.bdy()) {
                        ChatMessage chatMessage = chzzkMessageMapper.parse(chatItem);
                        System.out.println(chatMessage);
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
        // 스케줄러 종료 (자원 정리)
        if (pingScheduler != null && !pingScheduler.isShutdown()) {
            pingScheduler.shutdown();
        }
    }

    @Override
    public void onError(Exception ex) {
        ex.printStackTrace();
    }

    // 서버가 물어봤을 때 대답하는 용도 (Reactive)
    private void sendPong() {
        String pongPacket = "{"
                + "\"cmd\": 10000, "
                + "\"ver\": 2, "
                + "\"svcid\": \"game\", "
                + "\"bdy\": {}"
                + "}";
        this.send(pongPacket);
    }

    // 내가 먼저 찌르는 용도 (Proactive)
    private void sendActivePing() {
        String pingPacket = "{"
                + "\"cmd\": 0, "
                + "\"ver\": 2"
                + "}";
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
