package io.github.hypecycle.chatpipeline.connector.chzzk;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import lombok.extern.slf4j.Slf4j;
import org.java_websocket.client.WebSocketClient;
import org.java_websocket.handshake.ServerHandshake;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@Scope("prototype")
public class ChzzkWebsocketClient extends WebSocketClient {

    private final String chatChannelId;
    private final String accessToken;
    private final ChzzkMessageHandler messageHandler;

    private ScheduledExecutorService pingScheduler;

    public ChzzkWebsocketClient(
        ChzzkMessageHandler messageHandler,
        @Value("${chzzk.websocket.url}") String websocketUrl,
        String chatChannelId,
        String accessToken) throws URISyntaxException {
        super(new URI(websocketUrl));
        this.messageHandler = messageHandler;
        this.chatChannelId = chatChannelId;
        this.accessToken = accessToken;
    }

    @Override
    public void onOpen(ServerHandshake handshakeData) {
        this.pingScheduler = Executors.newSingleThreadScheduledExecutor();
        messageHandler.handleOpen(this, chatChannelId, accessToken, pingScheduler);
    }

    @Override
    public void onMessage(String message) {
        messageHandler.handleMessage(message)
            .ifPresent(this::send);
    }

    @Override
    public void onClose(int code, String reason, boolean remote) {
        messageHandler.handleClose(reason, pingScheduler);
    }

    @Override
    public void onError(Exception ex) {
        messageHandler.handleError(ex);
    }
}
