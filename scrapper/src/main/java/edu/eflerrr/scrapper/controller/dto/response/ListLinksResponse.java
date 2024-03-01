package edu.eflerrr.scrapper.controller.dto.response;

import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.v3.oas.annotations.media.Schema;
import java.util.ArrayList;
import java.util.List;
import lombok.EqualsAndHashCode;
import lombok.Setter;
import lombok.ToString;

/**
 * ListLinksResponse
 */

@Setter
@EqualsAndHashCode
@ToString
public class ListLinksResponse {

    @JsonProperty("links")
    private List<LinkResponse> links = null;

    @JsonProperty("size")
    private Integer size;

    public ListLinksResponse links(List<LinkResponse> links) {
        this.links = links;
        return this;
    }

    public ListLinksResponse addLinksItem(LinkResponse linksItem) {
        if (this.links == null) {
            this.links = new ArrayList<>();
        }
        this.links.add(linksItem);
        return this;
    }

    /**
     * Get links
     *
     * @return links
     */

    @Schema(name = "links", example = "[{\"id\":1,\"url\":\"https://example.com\"}]")
    public List<LinkResponse> getLinks() {
        return links;
    }

    public ListLinksResponse size(Integer size) {
        this.size = size;
        return this;
    }

    /**
     * Get size
     *
     * @return size
     */

    @Schema(name = "size", example = "1")
    public Integer getSize() {
        return size;
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
