package io.github.hypecycle.chatpipeline.global;

import lombok.Getter;

@Getter
public enum ErrorCode {

    INVALID_FORMAT_CHANNEL_ID("채널 id 형식이 잘못되었습니다. 다시 입력해주세요."),
    CLOSE_LIVE("채널이 방송중이 아닙니다."),
    INVALID_CHANNEL_ID("존재하지 않는 채널입니다.")
    ;

    private String message;

    ErrorCode(String message) {
        this.message = message;
    }
}
