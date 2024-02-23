package edu.eflerrr.client.response.impl;

import com.fasterxml.jackson.annotation.JsonProperty;
import edu.eflerrr.client.response.HttpClientResponse;
import java.time.OffsetDateTime;

public record GithubClientResponse(
    Long id,
    String name,
    @JsonProperty("updated_at")
    OffsetDateTime lastUpdate,
    @JsonProperty("pushed_at")
    OffsetDateTime pushUpdate
) implements HttpClientResponse {

    @Override
    public String toString() {
        return String.format(
            "GithubClientResponse{ id=%d, name=%s, lastUpdate=%s, pushUpdate=%s }%n",
            id, name, lastUpdate.toString(), pushUpdate.toString()
        );
    }

}
