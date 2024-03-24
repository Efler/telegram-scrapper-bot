package edu.eflerrr.scrapper.domain.jdbc.dto;

import java.time.OffsetDateTime;
import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.ToString;

@Getter
@EqualsAndHashCode
@ToString
@AllArgsConstructor
public class Branch {

    private Long id;
    private Long linkId;
    private String repositoryOwner;
    private String repositoryName;
    private String branchName;
    private OffsetDateTime lastCommitTime;

    public Branch(
        Long linkId, String repositoryOwner, String repositoryName,
        String branchName, OffsetDateTime lastCommitTime
    ) {
        this.id = null;
        this.linkId = linkId;
        this.repositoryOwner = repositoryOwner;
        this.repositoryName = repositoryName;
        this.branchName = branchName;
        this.lastCommitTime = lastCommitTime;
    }

}
