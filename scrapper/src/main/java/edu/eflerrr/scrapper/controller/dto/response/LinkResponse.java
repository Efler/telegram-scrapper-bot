package edu.eflerrr.scrapper.controller.dto.response;

import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.v3.oas.annotations.media.Schema;
import java.net.URI;
import lombok.EqualsAndHashCode;
import lombok.Setter;
import lombok.ToString;

/**
 * LinkResponse
 */

@Setter
@EqualsAndHashCode
@ToString
public class LinkResponse {

    @JsonProperty("id")
    private Long id;

    @JsonProperty("url")
    private URI url;

    public LinkResponse id(Long id) {
        this.id = id;
        return this;
    }

    /**
     * Get id
     *
     * @return id
     */

    @Schema(name = "id", example = "1")
    public Long getId() {
        return id;
    }

    public LinkResponse url(URI url) {
        this.url = url;
        return this;
    }

    /**
     * Get url
     *
     * @return url
     */

    @Schema(name = "url", example = "https://example.com")
    public URI getUrl() {
        return url;
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

