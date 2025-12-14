package io.github.hypecycle.chatpipeline.connector.chzzk.dto.response;

import lombok.Getter;

@Getter
public enum ChzzkCommand {
    PING(0),
    PONG(10000),
    CHAT(93101),
    DONATION(93102),
    ;

    private final int num;

    ChzzkCommand(int num) {
        this.num = num;
    }
}
