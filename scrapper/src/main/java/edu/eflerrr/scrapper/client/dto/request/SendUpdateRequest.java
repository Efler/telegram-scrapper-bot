package edu.eflerrr.scrapper.client.dto.request;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.net.URI;
import java.util.List;

public record SendUpdateRequest(
    @JsonProperty("id")
    Long id,
    @JsonProperty("url")
    URI url,
    @JsonProperty("description")
    String description,
    @JsonProperty("tgChatIds")
    List<Long> tgChatIds

) {
}
