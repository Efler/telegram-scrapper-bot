package edu.eflerrr.scrapper.domain.jpa.entity;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import java.time.OffsetDateTime;
import lombok.Data;

@Data
@Entity
@Table(name = "Branch")
public class Branch {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY,
               cascade = {
                   CascadeType.PERSIST,
                   CascadeType.MERGE
               })
    @JoinColumn(name = "link_id")
    private Link link;

    @Column(name = "repository_owner")
    private String repositoryOwner;

    @Column(name = "repository_name")
    private String repositoryName;

    @Column(name = "branch_name")
    private String branchName;

    @Column(name = "last_commit_time")
    private OffsetDateTime lastCommitTime;

}
