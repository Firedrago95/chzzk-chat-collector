package io.github.hypecycle.chzzk.connector;

import io.github.hypecycle.chzzk.connector.dto.response.ChannelInfoResponse;
import io.github.hypecycle.chzzk.connector.dto.response.ChatAccessResponse;
import io.github.hypecycle.chzzk.global.ChzzkPipelineException;
import io.github.hypecycle.chzzk.global.ErrorCode;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

@Component
public class ChzzkApiClient {

    private final RestClient restClient;

    public ChzzkApiClient(RestClient.Builder builder) {
        this.restClient = builder.build();
    }

    public String getChatChannelId(String channelId) {
        ChannelInfoResponse response = restClient
                .get()
                .uri("https://api.chzzk.naver.com/service/v2/channels/"+ channelId +"/live-detail")
                .accept(MediaType.APPLICATION_JSON)
                .retrieve()
                .body(ChannelInfoResponse.class);

        if (response == null) {
            throw new ChzzkPipelineException(ErrorCode.INVALID_CHANNEL_ID);
        }

        if ("CLOSE".equals(response.content().status())) {
            throw new ChzzkPipelineException(ErrorCode.CLOSE_LIVE);
        }

        String chatChannelId = response.content().chatChannelId();
        System.out.println("chatChannelID = " + chatChannelId);
        return chatChannelId;
    }

    public String getAccessToken(String chatChannelId) {
        ChatAccessResponse chatAccessResponse = restClient
                .get()
                .uri("https://comm-api.game.naver.com/nng_main/v1/chats/access-token?channelId="
                        + chatChannelId + "&chatType=STREAMING")
                .accept(MediaType.APPLICATION_JSON)
                .retrieve()
                .body(ChatAccessResponse.class);

        if (chatAccessResponse == null) {
            throw new ChzzkPipelineException(ErrorCode.INVALID_CHANNEL_ID);
        }

        String accessToken = chatAccessResponse.content().accessToken();
        System.out.println("accessToken = " + accessToken);
        return accessToken;
    }
}
