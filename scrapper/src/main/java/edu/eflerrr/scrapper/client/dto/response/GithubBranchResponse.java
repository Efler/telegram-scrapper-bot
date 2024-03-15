package edu.eflerrr.scrapper.client.dto.response;

import java.time.OffsetDateTime;

public record GithubBranchResponse(
    String name,
    OffsetDateTime lastCommitTime
) {
}
