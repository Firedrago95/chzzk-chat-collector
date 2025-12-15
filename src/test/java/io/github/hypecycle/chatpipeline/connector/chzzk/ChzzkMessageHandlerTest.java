package io.github.hypecycle.chatpipeline.connector.chzzk;

import static org.assertj.core.api.Assertions.assertThat;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.github.hypecycle.chatpipeline.buffer.ChatBuffer;
import io.github.hypecycle.chatpipeline.connector.chzzk.dto.response.ChzzkCommand;
import io.github.hypecycle.chatpipeline.connector.chzzk.dto.response.ChzzkResponseMessage;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class ChzzkMessageHandlerTest {

    private final ObjectMapper objectMapper = new ObjectMapper();
    private final ChzzkMessageMapper messageMapper = new ChzzkMessageMapper(objectMapper);

    private ChzzkMessageHandler messageHandler;
    private ChatBuffer chatBuffer;

    @BeforeEach
    void setUp() {
        chatBuffer = new ChatBuffer();
        messageHandler = new ChzzkMessageHandler(messageMapper, objectMapper, chatBuffer);
    }

    @Test
    void PING_메시지를_받으면_PONG_메시지를_반환한다() throws Exception {
        // given
        String pingMessage = "{\"cmd\":0}";
        ChzzkResponseMessage pingResponse = new ChzzkResponseMessage(
            ChzzkCommand.PING, null, null, null);

        // when
        Optional<String> result = messageHandler.handleMessage(pingMessage);

        // then
        assertThat(result).isPresent();
        assertThat(result.get()).contains("\"cmd\": 10000");
    }

    @Test
    void CHAT_메시지를_받으면_메시지_파서를_호출한다() throws Exception {
        // given
        String chatMessageJson = """
            {
              "cmd": 93101,
              "bdy": [
                {
                  "msg": "test message",
                  "profile": "{\\"userIdHash\\": \\"123124\\", \\"nickname\\": \\"test\\"}",
                  "msgTypeCode": 1,
                  "msgTime": 1234567890,
                  "extras": "{}"
                }
              ],
              "tid": "1",
              "cid": "test_cid"
            }
            """;

        // when
        messageHandler.handleMessage(chatMessageJson);

        // then
        assertThat(chatBuffer.poll().message()).isEqualTo("test message");
    }
}
