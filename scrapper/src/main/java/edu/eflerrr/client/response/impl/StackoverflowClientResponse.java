package edu.eflerrr.client.response.impl;

import com.fasterxml.jackson.annotation.JsonProperty;
import edu.eflerrr.client.response.HttpClientResponse;
import java.time.OffsetDateTime;
import java.util.ArrayList;
import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.Setter;

@Setter
@AllArgsConstructor
@EqualsAndHashCode
public class StackoverflowClientResponse implements HttpClientResponse {
    private OffsetDateTime lastUpdate;
    @JsonProperty("items")
    private ArrayList<Event> events;

    @Override
    public OffsetDateTime lastUpdate() {
        return lastUpdate;
    }

    public ArrayList<Event> events() {
        return events;
    }

    @Override
    public String toString() {
        var builder = new StringBuilder();
        builder.append(String.format(
            "StackoverflowClientResponse{ lastUpdate=%s, events=%n",
            lastUpdate.toString()
        ));
        for (var event : events) {
            builder.append(String.format(
                "  Event{ time=%s, type=%s }%n",
                event.time.toString(), event.type
            ));
        }
        builder.append("}");
        return builder.toString();
    }

    public record Event(
        @JsonProperty("creation_date")
        OffsetDateTime time,
        @JsonProperty("timeline_type")
        String type
    ) {
    }

}
