package edu.eflerrr.scrapper.domain.dto;

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
    private String repositoryOwner;
    private String repositoryName;
    private String branchName;
    private OffsetDateTime lastCommitTime;

    public Branch(
        String repositoryOwner, String repositoryName,
        String branchName, OffsetDateTime lastCommitTime
    ) {
        this.id = null;
        this.repositoryOwner = repositoryOwner;
        this.repositoryName = repositoryName;
        this.branchName = branchName;
        this.lastCommitTime = lastCommitTime;
    }

}
