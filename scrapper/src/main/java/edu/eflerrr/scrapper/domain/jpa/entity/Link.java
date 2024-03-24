package edu.eflerrr.scrapper.domain.jpa.entity;

import edu.eflerrr.scrapper.domain.jpa.converter.UriConverter;
import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Convert;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.ManyToMany;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import java.net.URI;
import java.time.OffsetDateTime;
import java.util.HashSet;
import java.util.Set;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;

@Data
@Entity
@Table(name = "Link")
public class Link {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "url", columnDefinition = "TEXT")
    @Convert(converter = UriConverter.class)
    private URI url;

    @Column(name = "created_at")
    private OffsetDateTime createdAt;

    @Column(name = "checked_at")
    private OffsetDateTime checkedAt;

    @Column(name = "updated_at")
    private OffsetDateTime updatedAt;

    @ToString.Exclude
    @EqualsAndHashCode.Exclude()
    @ManyToMany(mappedBy = "links",
                fetch = FetchType.LAZY,
                cascade = {
                    CascadeType.PERSIST,
                    CascadeType.MERGE
                })
    private Set<Chat> chats = new HashSet<>();

    @ToString.Exclude
    @EqualsAndHashCode.Exclude()
    @OneToMany(mappedBy = "link",
               fetch = FetchType.LAZY,
               cascade = {
                   CascadeType.PERSIST,
                   CascadeType.MERGE
               },
               orphanRemoval = true)
    private Set<Branch> branches = new HashSet<>();

    public void addBranch(Branch branch) {
        branch.setLink(this);
        branches.add(branch);
    }

}
