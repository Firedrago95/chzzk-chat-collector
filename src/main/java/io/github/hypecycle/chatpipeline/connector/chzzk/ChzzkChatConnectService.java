package io.github.hypecycle.chatpipeline.connector.chzzk;

import io.github.hypecycle.chatpipeline.global.ChzzkPipelineException;
import java.util.concurrent.TimeUnit;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class ChzzkChatConnectService implements CommandLineRunner {

    private final ChannelIdReader channelIdReader;
    private final ChzzkApiClient chzzkApiClient;
    private final ObjectProvider<ChzzkWebsocketClient> websocketClientProvider;

    @Override
    public void run(String... args) throws Exception {
        while (!Thread.currentThread().isInterrupted()) {
            try {
                String channelId = channelIdReader.readChannelId();
                String chatChannelId = chzzkApiClient.getChatChannelId(channelId);
                String accessToken = chzzkApiClient.getAccessToken(chatChannelId);

                ChzzkWebsocketClient socketClient = websocketClientProvider.getObject(chatChannelId,
                    accessToken);
                socketClient.connectBlocking();
                break;

            } catch (ChzzkPipelineException e) {
                log.error(e.getMessage());
                TimeUnit.SECONDS.sleep(3);
            }
        }
    }
}
