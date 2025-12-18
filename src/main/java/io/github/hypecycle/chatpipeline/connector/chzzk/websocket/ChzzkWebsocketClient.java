package io.github.hypecycle.chatpipeline.connector.chzzk.websocket;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import lombok.extern.slf4j.Slf4j;
import org.java_websocket.client.WebSocketClient;
import org.java_websocket.handshake.ServerHandshake;

@Slf4j
public class ChzzkWebsocketClient extends WebSocketClient {

    private final String chatChannelId;
    private final String accessToken;
    private final ChzzkMessageHandler messageHandler;

    private ScheduledExecutorService pingScheduler;

    public ChzzkWebsocketClient(
        ChzzkMessageHandler messageHandler,
        String chatChannelId,
        String accessToken
    ) throws URISyntaxException {
        super(new URI("wss://kr-ss1.chat.naver.com/chat"));
        this.messageHandler = messageHandler;
        this.chatChannelId = chatChannelId;
        this.accessToken = accessToken;
    }

    @Override
    public void onOpen(ServerHandshake handshakeData) {
        this.pingScheduler = Executors.newSingleThreadScheduledExecutor();
        log.info(">>> Websocket 연결 성공! 인증 패킷 전송 시작...");

        String authPacket = messageHandler.createAuthPacket(chatChannelId, accessToken);
        send(authPacket);

        pingScheduler.scheduleAtFixedRate(this::sendActivePing, 20, 20, TimeUnit.SECONDS);
    }

    private void sendActivePing() {
        String pingPacket = "{\"cmd\": 0, \"ver\": 2}";
        try {
            send(pingPacket);
        } catch (Exception e) {
            log.error("Heartbeat 전송 실패", e);
        }
    }

    @Override
    public void onMessage(String message) {
        messageHandler.handleMessage(message)
            .ifPresent(this::send);
    }

    @Override
    public void onClose(int code, String reason, boolean remote) {
        log.info(">>> 연결 끊김: {}", reason);
        if (pingScheduler != null && !pingScheduler.isShutdown()) {
            pingScheduler.shutdown();
        }
    }

    @Override
    public void onError(Exception ex) {
        log.error("Websocket 오류 발생", ex);
    }
}
