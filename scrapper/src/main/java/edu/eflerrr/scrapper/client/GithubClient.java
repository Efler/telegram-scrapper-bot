package edu.eflerrr.scrapper.client;

import com.fasterxml.jackson.databind.JsonNode;
import edu.eflerrr.scrapper.client.dto.response.GithubBranchResponse;
import edu.eflerrr.scrapper.client.dto.response.GithubClientResponse;
import edu.eflerrr.scrapper.exception.retry.RetryableRequestException;
import java.time.OffsetDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.stream.Stream;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.retry.support.RetryTemplate;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

@RequiredArgsConstructor
@SuppressWarnings({"MultipleStringLiterals", "ReturnCount"})
public class GithubClient {

    private final WebClient webClient;
    private final RetryTemplate retryTemplate;
    private final Set<Integer> retryStatusCodes;

    private GithubBranchResponse getBranchDataWithRetry(String username, String repository, String branchName) {
        var branchJsonNode = (JsonNode) retryTemplate.execute(
            context -> getBranchData(username, repository, branchName).block()
        );
        if (branchJsonNode == null) {
            throw new RuntimeException(
                "Error occurred during getBranchDataWithRetry in GithubClient! Message: empty response/data"
            );
        } else {
            return new GithubBranchResponse(
                branchJsonNode.get("name").asText(),
                OffsetDateTime.parse(
                    branchJsonNode.get("commit").get("commit").get("committer").get("date").asText(),
                    DateTimeFormatter.ISO_OFFSET_DATE_TIME
                )
            );
        }
    }

    private Mono<JsonNode> getBranchData(String username, String repository, String branchName) {
        return webClient.get()
            .uri(String.format("/repos/%s/%s/branches/%s", username, repository, branchName))
            .exchangeToMono(response -> {
                HttpStatus status = (HttpStatus) response.statusCode();
                if (retryStatusCodes.contains(status.value())) {
                    return Mono.error(
                        new RetryableRequestException("Retryable status code: " + status.value())
                    );
                }
                if (status.is2xxSuccessful()) {
                    return response.bodyToMono(JsonNode.class);
                } else if (status.is4xxClientError()) {
                    return Mono.error(new RuntimeException(
                        "Client error during getBranchData in GithubClient! Message: "
                            + status.value() + " "
                            + status.getReasonPhrase()));
                } else if (status.is5xxServerError()) {
                    return Mono.error(new RuntimeException(
                        "Server error during getBranchData in GithubClient! Message: "
                            + status.value() + " "
                            + status.getReasonPhrase()));
                } else {
                    return Mono.error(new RuntimeException(
                        "Unexpected status code during getBranchData in GithubClient! Message: "
                            + status.value() + " "
                            + status.getReasonPhrase()));
                }
            });
    }

    private List<String> getBranchNamesWithRetry(String username, String repository) {
        var branchesJsonNodeArr = (JsonNode[]) retryTemplate.execute(
            context -> getBranchNames(username, repository).block()
        );
        if (branchesJsonNodeArr == null) {
            throw new RuntimeException(
                "Error occurred during getBranchNamesWithRetry in GithubClient! Message: empty response/data"
            );
        } else {
            return new ArrayList<>(
                Stream.of(branchesJsonNodeArr)
                    .map((x) -> x.get("name"))
                    .map(JsonNode::asText)
                    .toList()
            );
        }
    }

    private Mono<JsonNode[]> getBranchNames(String username, String repository) {
        return webClient.get()
            .uri(String.format("/repos/%s/%s/branches", username, repository))
            .exchangeToMono(response -> {
                HttpStatus status = (HttpStatus) response.statusCode();
                if (retryStatusCodes.contains(status.value())) {
                    return Mono.error(
                        new RetryableRequestException("Retryable status code: " + status.value())
                    );
                }
                if (status.is2xxSuccessful()) {
                    return response.bodyToMono(JsonNode[].class);
                } else if (status.is4xxClientError()) {
                    return Mono.error(new RuntimeException(
                        "Client error during getBranchNames in GithubClient! Message: "
                            + status.value() + " "
                            + status.getReasonPhrase()));
                } else if (status.is5xxServerError()) {
                    return Mono.error(new RuntimeException(
                        "Server error during getBranchNames in GithubClient! Message: "
                            + status.value() + " "
                            + status.getReasonPhrase()));
                } else {
                    return Mono.error(new RuntimeException(
                        "Unexpected status code during getBranchNames in GithubClient! Message: "
                            + status.value() + " "
                            + status.getReasonPhrase()));
                }
            });
    }

    private GithubClientResponse fetchResponseWithRetry(String username, String repository) {
        return retryTemplate.execute(
            context -> getRepositoryData(username, repository).block()
        );
    }

    private Mono<GithubClientResponse> getRepositoryData(String username, String repository) {
        return webClient.get()
            .uri(String.format("/repos/%s/%s", username, repository))
            .exchangeToMono(response -> {
                HttpStatus status = (HttpStatus) response.statusCode();
                if (retryStatusCodes.contains(status.value())) {
                    return Mono.error(
                        new RetryableRequestException("Retryable status code: " + status.value())
                    );
                }
                if (status.is2xxSuccessful()) {
                    return response.bodyToMono(GithubClientResponse.class);
                } else if (status.is4xxClientError()) {
                    return Mono.error(new RuntimeException(
                        "Client error during fetchResponse in GithubClient! Message: "
                            + status.value() + " "
                            + status.getReasonPhrase()));
                } else if (status.is5xxServerError()) {
                    return Mono.error(new RuntimeException(
                        "Server error during fetchResponse in GithubClient! Message: "
                            + status.value() + " "
                            + status.getReasonPhrase()));
                } else {
                    return Mono.error(new RuntimeException(
                        "Unexpected status code during fetchResponse in GithubClient! Message: "
                            + status.value() + " "
                            + status.getReasonPhrase()));
                }
            });
    }

    public GithubClientResponse fetchResponse(String username, String repository) {
        var githubResponse = fetchResponseWithRetry(username, repository);

        if (githubResponse == null
            || githubResponse.getLastUpdate() == null
            || githubResponse.getPushUpdate() == null
            || githubResponse.getName() == null
            || githubResponse.getId() == null) {
            throw new RuntimeException(
                "Error occurred during fetchResponse in GithubClient! Message: empty response/data"
            );
        } else {
            var branchNames = getBranchNamesWithRetry(username, repository);
            var branchDataList = new ArrayList<GithubBranchResponse>();
            for (var branchName : branchNames) {
                branchDataList.add(getBranchDataWithRetry(username, repository, branchName));
            }
            githubResponse.getBranches().addAll(branchDataList);
            return githubResponse;
        }
    }

}
