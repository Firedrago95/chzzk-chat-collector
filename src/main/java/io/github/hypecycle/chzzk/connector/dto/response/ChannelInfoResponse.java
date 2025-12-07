package io.github.hypecycle.chzzk.connector.dto.response;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@JsonIgnoreProperties(ignoreUnknown = true)
public record ChannelInfoResponse(
        ChzzkContent content
) {

    @JsonIgnoreProperties(ignoreUnknown = true)
    public record ChzzkContent(
            String status,
            String chatChannelId
    ) {}
}
