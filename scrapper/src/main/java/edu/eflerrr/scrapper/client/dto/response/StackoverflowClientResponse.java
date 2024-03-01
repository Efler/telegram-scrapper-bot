package edu.eflerrr.scrapper.client.dto.response;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.time.OffsetDateTime;
import java.util.ArrayList;
import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.Setter;
import lombok.ToString;

@Setter
@AllArgsConstructor
@EqualsAndHashCode
@ToString
public class StackoverflowClientResponse {
    private OffsetDateTime lastUpdate;
    @JsonProperty("items")
    private ArrayList<Event> events;

    public OffsetDateTime lastUpdate() {
        return lastUpdate;
    }

    public ArrayList<Event> events() {
        return events;
    }

    public record Event(
        @JsonProperty("creation_date")
        OffsetDateTime time,
        @JsonProperty("timeline_type")
        String type
    ) {
    }

}
