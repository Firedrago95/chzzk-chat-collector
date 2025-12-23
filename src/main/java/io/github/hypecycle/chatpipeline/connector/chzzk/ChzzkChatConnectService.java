package io.github.hypecycle.chatpipeline.connector.chzzk;

import io.github.hypecycle.chatpipeline.connector.chzzk.api.ChannelIdReader;
import io.github.hypecycle.chatpipeline.connector.chzzk.api.ChzzkApiClient;
import io.github.hypecycle.chatpipeline.connector.chzzk.websocket.ChzzkWebsocketClient;
import io.github.hypecycle.chatpipeline.connector.chzzk.websocket.ChzzkWebsocketClientFactory;
import io.github.hypecycle.chatpipeline.global.ChzzkPipelineException;
import java.util.concurrent.TimeUnit;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

@Slf4j
@Order(1)
@Component
@RequiredArgsConstructor
public class ChzzkChatConnectService implements CommandLineRunner {

    private final ChannelIdReader channelIdReader;
    private final ChzzkApiClient chzzkApiClient;
    private final ChzzkWebsocketClientFactory chzzkWebsocketClientFactory;

    @Override
    public void run(String... args) throws Exception {
        while (!Thread.currentThread().isInterrupted()) {
            try {
                String channelId = channelIdReader.readChannelId();
                String chatChannelId = chzzkApiClient.getChatChannelId(channelId);
                String accessToken = chzzkApiClient.getAccessToken(chatChannelId);

                ChzzkWebsocketClient socketClient = chzzkWebsocketClientFactory.create(chatChannelId, accessToken);
                socketClient.connectBlocking();
                break;

            } catch (ChzzkPipelineException e) {
                log.error(e.getMessage());
                TimeUnit.SECONDS.sleep(2);
            }
        }
    }
}

