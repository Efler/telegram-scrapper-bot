package edu.eflerrr.bot.controller.dto.request;

import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.v3.oas.annotations.media.Schema;
import java.net.URI;
import java.util.ArrayList;
import java.util.List;
import lombok.EqualsAndHashCode;
import lombok.Setter;
import lombok.ToString;

/**
 * LinkUpdate
 */

@Setter
@EqualsAndHashCode
@ToString
public class LinkUpdate {

    @JsonProperty("id")
    private Long id;

    @JsonProperty("url")
    private URI url;

    @JsonProperty("description")
    private String description;

    @JsonProperty("tgChatIds")
    private List<Long> tgChatIds = null;

    public LinkUpdate id(Long id) {
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

    public LinkUpdate url(URI url) {
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

    public LinkUpdate description(String description) {
        this.description = description;
        return this;
    }

    /**
     * Get description
     *
     * @return description
     */

    @Schema(name = "description", example = "Новое уведомление")
    public String getDescription() {
        return description;
    }

    public LinkUpdate tgChatIds(List<Long> tgChatIds) {
        this.tgChatIds = tgChatIds;
        return this;
    }

    public LinkUpdate addTgChatIdsItem(Long tgChatIdsItem) {
        if (this.tgChatIds == null) {
            this.tgChatIds = new ArrayList<>();
        }
        this.tgChatIds.add(tgChatIdsItem);
        return this;
    }

    /**
     * Get tgChatIds
     *
     * @return tgChatIds
     */

    @Schema(name = "tgChatIds", example = "[1, 2, 3]")
    public List<Long> getTgChatIds() {
        return tgChatIds;
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
