package edu.eflerrr.scrapper.client.dto.response;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.time.OffsetDateTime;

public record GithubClientResponse(
    Long id,
    String name,
    @JsonProperty("updated_at")
    OffsetDateTime lastUpdate,
    @JsonProperty("pushed_at")
    OffsetDateTime pushUpdate
) {
}
