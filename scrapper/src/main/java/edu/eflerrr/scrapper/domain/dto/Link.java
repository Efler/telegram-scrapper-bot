package edu.eflerrr.scrapper.domain.dto;

import java.net.URI;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.ToString;

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
        this.checkedAt = OffsetDateTime.MIN;
        this.updatedAt = OffsetDateTime.now().withOffsetSameInstant(ZoneOffset.UTC);
    }

    public Link(URI url, OffsetDateTime createdAt) {
        this.id = null;
        this.url = url;
        this.createdAt = createdAt;
        this.checkedAt = OffsetDateTime.MIN;
        this.updatedAt = createdAt;
    }

    public Link(Long id, URI url, OffsetDateTime createdAt) {
        this.id = id;
        this.url = url;
        this.createdAt = createdAt;
        this.checkedAt = OffsetDateTime.MIN;
        this.updatedAt = createdAt;
    }

}
