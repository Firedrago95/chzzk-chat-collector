package io.github.hypecycle.chzzk.connector.dto.response;

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
