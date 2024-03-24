package edu.eflerrr.scrapper.domain.jpa.entity;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.Data;

@Data
@Entity
@Table(name = "Tracking")
public class Tracking {
    @Id
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY,
               cascade = {
                   CascadeType.PERSIST,
                   CascadeType.MERGE
               })
    @JoinColumn(name = "chat_id")
    private Chat chat;

    @ManyToOne(fetch = FetchType.LAZY,
               cascade = {
                   CascadeType.PERSIST,
                   CascadeType.MERGE
               })
    @JoinColumn(name = "link_id")
    private Link link;

}
