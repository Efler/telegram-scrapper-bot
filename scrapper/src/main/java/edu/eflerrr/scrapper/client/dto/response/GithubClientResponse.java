package edu.eflerrr.scrapper.client.dto.response;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.List;
import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.ToString;

@Getter
@ToString
@EqualsAndHashCode
@RequiredArgsConstructor
@AllArgsConstructor
public class GithubClientResponse {
    private Long id;
    private String name;
    @JsonProperty("updated_at")
    private OffsetDateTime lastUpdate;
    @JsonProperty("pushed_at")
    private OffsetDateTime pushUpdate;
    private List<GithubBranchResponse> branches = new ArrayList<>();
}
