package io.github.hypecycle.chatpipeline.connector.chzzk;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import io.github.hypecycle.chatpipeline.global.ChzzkPipelineException;
import io.github.hypecycle.chatpipeline.global.ErrorCode;
import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.Scanner;
import org.junit.jupiter.api.Test;

class ChannelIdReaderTest {

    @Test
    void 유효한_채널ID를_입력하면_해당_ID를_반환한다() {
        //given
        String input = "0123456789abcdef0123456789abcdef";

        InputStream fakeInputStream = new ByteArrayInputStream(input.getBytes(StandardCharsets.UTF_8));
        Scanner fakeScanner = new Scanner(fakeInputStream);

        ChannelIdReader reader = new ChannelIdReader(fakeScanner);

        //when
        String result = reader.readChannelId();

        //then
        assertThat(result).isEqualTo(input);
    }

    @Test
    void 유효하지_않은_형식의_채널ID를_입력하면_예외가_발생한다() {
        //given
        String invalidInput = "invalid_id_short";

        InputStream fakeInputStream = new ByteArrayInputStream(invalidInput.getBytes(StandardCharsets.UTF_8));
        Scanner fakeScanner = new Scanner(fakeInputStream);

        ChannelIdReader reader = new ChannelIdReader(fakeScanner);

        //when & then
        assertThatThrownBy(() -> reader.readChannelId())
                .isInstanceOf(ChzzkPipelineException.class)
                .hasMessageContaining(ErrorCode.INVALID_FORMAT_CHANNEL_ID.getMessage());
    }
}
