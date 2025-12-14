package io.github.hypecycle.chatpipeline.connector.chzzk.dto.response;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@JsonIgnoreProperties(ignoreUnknown = true)
public record ChatAccessResponse(
        Content content
) {

    @JsonIgnoreProperties(ignoreUnknown = true)
    public record Content (
            String accessToken
    ) {}
}
