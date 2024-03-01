package edu.eflerrr.bot.client.dto.response;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.net.URI;

public record LinkResponse(
    @JsonProperty("id")
    Long id,
    @JsonProperty("url")
    URI url
) {
}
