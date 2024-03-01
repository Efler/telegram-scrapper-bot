package edu.eflerrr.bot.client.dto.response;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.List;

public record ScrapperErrorResponse(
    @JsonProperty("description")
    String description,
    @JsonProperty("code")
    String code,
    @JsonProperty("exceptionName")
    String exceptionName,
    @JsonProperty("exceptionMessage")
    String exceptionMessage,
    @JsonProperty("stacktrace")
    List<String> stacktrace
) {
}
