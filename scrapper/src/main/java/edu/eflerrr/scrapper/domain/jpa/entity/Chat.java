package edu.eflerrr.scrapper.domain.jpa.entity;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.JoinTable;
import jakarta.persistence.ManyToMany;
import jakarta.persistence.Table;
import java.time.OffsetDateTime;
import java.util.HashSet;
import java.util.Set;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;

@Data
@Entity
@Table(name = "Chat")
public class Chat {

    @Id
    private Long id;

    @Column(name = "username")
    private String username;

    @Column(name = "created_at")
    private OffsetDateTime createdAt;

    @ToString.Exclude
    @EqualsAndHashCode.Exclude()
    @ManyToMany(fetch = FetchType.LAZY,
                cascade = {
                    CascadeType.PERSIST,
                    CascadeType.MERGE
                }
    )
    @JoinTable(name = "tracking",
               joinColumns = @JoinColumn(name = "chat_id"),
               inverseJoinColumns = @JoinColumn(name = "link_id")
    )
    private Set<Link> links = new HashSet<>();

    public void addLink(Link link) {
        link.getChats().add(this);
        links.add(link);
    }

    public void deleteLink(Link link) {
        link.getChats().remove(this);
        links.remove(link);
    }

}
