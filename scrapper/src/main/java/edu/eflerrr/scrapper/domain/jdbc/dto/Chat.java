package edu.eflerrr.scrapper.domain.jdbc.dto;

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
public class Chat {

    private Long id;
    private String username;
    private OffsetDateTime createdAt;

    public Chat(Long id, String username) {
        this.id = id;
        this.username = username;
        this.createdAt = OffsetDateTime.now().withOffsetSameInstant(ZoneOffset.UTC);
    }

}
