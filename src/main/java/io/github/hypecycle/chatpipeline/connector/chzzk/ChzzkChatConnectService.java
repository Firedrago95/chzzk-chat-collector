package io.github.hypecycle.chatpipeline.connector.chzzk;

import io.github.hypecycle.chatpipeline.global.ChzzkPipelineException;
import java.net.URI;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class ChzzkChatConnectService implements CommandLineRunner {

    private final ChannelIdReader channelIdReader;
    private final ChzzkApiClient chzzkApiClient;

    @Override
    public void run(String... args) throws Exception {
        try {
            String channelId = channelIdReader.readChannelId();
            String chatChannelId = chzzkApiClient.getChatChannelId(channelId);
            String accessToken = chzzkApiClient.getAccessToken(chatChannelId);

            URI socketUri = new URI("wss://kr-ss1.chat.naver.com/chat");
            ChzzkWebsocketClient socketClient = new ChzzkWebsocketClient(socketUri, chatChannelId, accessToken);

            socketClient.connect(); // 비동기 연결 시작

        } catch (ChzzkPipelineException e) {
            System.out.println(e.getMessage());
            run(args);
        }
    }

}
