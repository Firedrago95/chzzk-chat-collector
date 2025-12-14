package io.github.hypecycle.chatpipeline.connector.chzzk;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

import io.github.hypecycle.chatpipeline.global.ChzzkPipelineException;
import io.github.hypecycle.chatpipeline.global.ErrorCode;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.beans.factory.ObjectProvider;

@ExtendWith(MockitoExtension.class)
class ChzzkChatConnectServiceTest {

    @InjectMocks
    private ChzzkChatConnectService connectService;

    @Mock
    private ChannelIdReader channelIdReader;
    @Mock
    private ChzzkApiClient chzzkApiClient;
    @Mock
    private ObjectProvider<ChzzkWebsocketClient> websocketClientProvider;
    @Mock
    private ChzzkWebsocketClient websocketClient;

    @DisplayName("정상적인 경우, 웹소켓 연결에 성공한다")
    @Test
    void 정상적인_경우_웹소켓_연결에_성공한다() throws Exception {
        // given
        given(channelIdReader.readChannelId()).willReturn("channel1");
        given(chzzkApiClient.getChatChannelId("channel1")).willReturn("chatChannel1");
        given(chzzkApiClient.getAccessToken("chatChannel1")).willReturn("token1");
        given(websocketClientProvider.getObject("chatChannel1", "token1")).willReturn(
            websocketClient);

        // when
        // run 메서드는 무한 루프이므로, 별도의 스레드에서 실행하고 인터럽트하여 종료시킨다.
        Thread serviceThread = new Thread(() -> {
            try {
                connectService.run();
            } catch (Exception e) {
                // 테스트를 위해 예상된 예외 처리
            }
        });
        serviceThread.start();

        // 서비스 스레드가 시작하고 connectBlocking()까지 실행되도록 잠시 대기
        Thread.sleep(100);

        // 서비스 스레드를 인터럽트하여 무한 루프에서 벗어나도록 시도
        serviceThread.interrupt();

        // 서비스 스레드가 종료될 때까지 잠시 대기
        serviceThread.join(2000); // 최대 2초 대기

        // Then
        verify(channelIdReader).readChannelId();
        verify(chzzkApiClient).getChatChannelId("channel1");
        verify(chzzkApiClient).getAccessToken("chatChannel1");
        verify(websocketClientProvider).getObject("chatChannel1", "token1");
        verify(websocketClient).connectBlocking();
    }

    @DisplayName("연결 실패 시, 3초 후 재시도한다")
    @Test
    void 연결_실패_시_3초_후_재시도한다() throws Exception {
        // given
        given(channelIdReader.readChannelId()).willReturn("channel1");
        given(chzzkApiClient.getChatChannelId("channel1")).willThrow(
                new ChzzkPipelineException(ErrorCode.INVALID_CHANNEL_ID))
            .willReturn("chatChannel1"); // 첫 호출은 실패, 두 번째는 성공
        given(chzzkApiClient.getAccessToken("chatChannel1")).willReturn("token1");
        given(websocketClientProvider.getObject("chatChannel1", "token1")).willReturn(
            websocketClient);

        // when
        doThrow(new RuntimeException("Test finished")).when(websocketClient).connectBlocking();

        try {
            connectService.run();
        } catch (RuntimeException e) {
            assertThat(e.getMessage()).isEqualTo("Test finished");
        }

        // then
        // getChatChannelId가 2번 호출되었는지 확인 (실패 1번, 성공 1번)
        verify(chzzkApiClient, times(2)).getChatChannelId("channel1");
        verify(websocketClient).connectBlocking(); // 최종적으로 연결 성공
    }
}
