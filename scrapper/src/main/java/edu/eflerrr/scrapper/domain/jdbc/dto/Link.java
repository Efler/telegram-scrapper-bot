package edu.eflerrr.scrapper.domain.jdbc.dto;

import java.net.URI;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.ToString;
import static edu.eflerrr.scrapper.configuration.TimeConstants.MIN_DATE_TIME;

@Getter
@EqualsAndHashCode
@ToString
@AllArgsConstructor
public class Link {

    private Long id;
    private URI url;
    private OffsetDateTime createdAt;
    private OffsetDateTime checkedAt;
    private OffsetDateTime updatedAt;

    public Link(URI url) {
        this.id = null;
        this.url = url;
        this.createdAt = OffsetDateTime.now().withOffsetSameInstant(ZoneOffset.UTC);
        this.checkedAt = MIN_DATE_TIME;
        this.updatedAt = OffsetDateTime.now().withOffsetSameInstant(ZoneOffset.UTC);
    }

    public Link(URI url, OffsetDateTime createdAt) {
        this.id = null;
        this.url = url;
        this.createdAt = createdAt;
        this.checkedAt = MIN_DATE_TIME;
        this.updatedAt = createdAt;
    }

    public Link(Long id, URI url, OffsetDateTime createdAt) {
        this.id = id;
        this.url = url;
        this.createdAt = createdAt;
        this.checkedAt = MIN_DATE_TIME;
        this.updatedAt = createdAt;
    }

}
