package io.github.hypecycle.chzzk.connector;

import io.github.hypecycle.chzzk.global.ChzzkPipelineException;
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
        } catch (ChzzkPipelineException e) {
            System.out.println(e.getMessage());
            run(args);
        }
    }

}
