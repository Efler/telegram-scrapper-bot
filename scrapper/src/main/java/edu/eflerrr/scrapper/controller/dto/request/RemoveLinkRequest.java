package edu.eflerrr.scrapper.controller.dto.request;

import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.v3.oas.annotations.media.Schema;
import java.net.URI;
import lombok.EqualsAndHashCode;
import lombok.Setter;
import lombok.ToString;

/**
 * RemoveLinkRequest
 */

@Setter
@EqualsAndHashCode
@ToString
public class RemoveLinkRequest {

    @JsonProperty("link")
    private URI link;

    public RemoveLinkRequest link(URI link) {
        this.link = link;
        return this;
    }

    /**
     * Get link
     *
     * @return link
     */

    @Schema(name = "link", example = "https://example.com")
    public URI getLink() {
        return link;
    }

    /**
     * Convert the given object to string with each line indented by 4 spaces
     * (except the first line).
     */
    private String toIndentedString(Object o) {
        if (o == null) {
            return "null";
        }
        return o.toString().replace("\n", "\n    ");
    }
}
