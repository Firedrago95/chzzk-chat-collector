package io.github.hypecycle.chatpipeline.connector.chzzk;

import io.github.hypecycle.chatpipeline.global.ChzzkPipelineException;
import io.github.hypecycle.chatpipeline.global.ErrorCode;
import java.util.Scanner;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public class ChannelIdReader {

    private static final String INPUT_MESSAGE = "접속하고 싶은 채널 id를 입력해주세요\n "
            + "ex) https://chzzk.naver.com/live/{채널 id}\n";
    private static final String CHANNEL_ID_REGEX = "^[a-fA-F0-9]{32}$";

    private final Scanner scanner;

    public String readChannelId() {
        System.out.println(INPUT_MESSAGE);
        String channelId = scanner.next();
        validateChannelId(channelId);

        return channelId;
    }

    private void validateChannelId(String channelId) {
        if (!channelId.matches(CHANNEL_ID_REGEX)) {
            throw new ChzzkPipelineException(ErrorCode.INVALID_FORMAT_CHANNEL_ID);
        }
    }

}
