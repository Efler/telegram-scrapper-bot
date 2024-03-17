package edu.eflerrr.scrapper.domain.jdbc.dto;

import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.ToString;

@Getter
@EqualsAndHashCode
@ToString
@AllArgsConstructor
public class Tracking {

    private Long id;
    private Long chatId;
    private Long linkId;

    public Tracking(Long chatId, Long linkId) {
        this.id = null;
        this.chatId = chatId;
        this.linkId = linkId;
    }

}
