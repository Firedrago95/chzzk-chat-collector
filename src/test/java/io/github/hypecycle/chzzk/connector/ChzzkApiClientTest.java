package io.github.hypecycle.chzzk.connector;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.requestTo;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withSuccess;

import io.github.hypecycle.chzzk.global.ChzzkPipelineException;
import io.github.hypecycle.chzzk.global.ErrorCode;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;
import org.springframework.test.web.client.MockRestServiceServer;
import org.springframework.web.client.RestClient;

class ChzzkApiClientTest {

    private ChzzkApiClient apiClient;
    private MockRestServiceServer mockServer;

    @BeforeEach
    void setUp() {
        // 실제 빌더 생성
        RestClient.Builder builder = RestClient.builder();

        // 가짜 서버를 빌더에 부착 (네트워크 차단 및 응답 조작)
        mockServer = MockRestServiceServer.bindTo(builder).build();

        // 실제 객체 생성 (Mock 객체가 아님)
        apiClient = new ChzzkApiClient(builder);
    }

    @Test
    void 유효한_채널ID를_입력하면_채팅채널ID를_반환한다() {
        //given
        String channelId = "valid_channel_id";
        String expectedChatChannelId = "chat_12345";

        // 네이버 API 응답 흉내 (DTO 구조에 맞춰야 함)
        String fakeResponse = """
            {
                "code": 200,
                "content": {
                    "chatChannelId": "%s",
                    "status": "OPEN"
                }
            }
            """.formatted(expectedChatChannelId);

        mockServer.expect(requestTo("https://api.chzzk.naver.com/service/v2/channels/" + channelId + "/live-detail"))
                .andRespond(withSuccess(fakeResponse, MediaType.APPLICATION_JSON));

        //when
        String result = apiClient.getChatChannelId(channelId);

        //then
        assertThat(result).isEqualTo(expectedChatChannelId);
    }

    @Test
    void 방송이_종료된_채널이라면_예외가_발생한다() {
        //given
        String channelId = "closed_channel_id";

        String fakeResponse = """
            {
                "code": 200,
                "content": {
                    "chatChannelId": "any_id",
                    "status": "CLOSE"
                }
            }
            """;

        mockServer.expect(requestTo("https://api.chzzk.naver.com/service/v2/channels/" + channelId + "/live-detail"))
                .andRespond(withSuccess(fakeResponse, MediaType.APPLICATION_JSON));

        //when & then
        assertThatThrownBy(() -> apiClient.getChatChannelId(channelId))
                .isInstanceOf(ChzzkPipelineException.class)
                .hasMessageContaining(ErrorCode.CLOSE_LIVE.getMessage());
    }

    @Test
    void 채팅채널ID로_접근토큰을_요청하면_토큰값을_반환한다() {
        //given
        String chatChannelId = "chat_12345";
        String expectedAccessToken = "access_token_value";

        String fakeResponse = """
            {
                "code": 200,
                "content": {
                    "accessToken": "%s"
                }
            }
            """.formatted(expectedAccessToken);

        mockServer.expect(requestTo("https://comm-api.game.naver.com/nng_main/v1/chats/access-token?channelId="
                        + chatChannelId + "&chatType=STREAMING"))
                .andRespond(withSuccess(fakeResponse, MediaType.APPLICATION_JSON));

        //when
        String result = apiClient.getAccessToken(chatChannelId);

        //then
        assertThat(result).isEqualTo(expectedAccessToken);
    }
}
