package io.github.hypecycle.chzzk.connector;

import io.github.hypecycle.chzzk.global.ChzzkPipelineException;
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
            System.out.println(">>> 3. 소켓 연결을 시도합니다.");

            // [수정 2] 데이터가 준비되었을 때 '직접' 생성합니다.
            // (URI는 하드코딩 혹은 설정파일에서 가져오기)
            URI socketUri = new URI("wss://kr-ss1.chat.naver.com/chat");
            ChzzkWebsocketClient socketClient = new ChzzkWebsocketClient(socketUri, chatChannelId, accessToken);

            socketClient.connect(); // 비동기 연결 시작

            // [참고] connect()는 비동기라서 바로 다음 줄로 넘어갑니다.
            // 메인 스레드가 죽지 않게 하려면 여기서 대기하거나,
            // Spring Boot Web 애플리케이션이라면 알아서 살아있습니다.
        } catch (ChzzkPipelineException e) {
            System.out.println(e.getMessage());
            run(args);
        }
    }

}
