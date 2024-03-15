package edu.eflerrr.scrapper.client;

import com.fasterxml.jackson.databind.JsonNode;
import edu.eflerrr.scrapper.client.customizer.GithubWebClientCustomizer;
import edu.eflerrr.scrapper.client.dto.response.GithubBranchResponse;
import edu.eflerrr.scrapper.client.dto.response.GithubClientResponse;
import java.time.OffsetDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Stream;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

@Component
@SuppressWarnings("MultipleStringLiterals")
public class GithubClient {
    private final WebClient webClient;

    @Autowired
    public GithubClient(WebClient.Builder webClientBuilder, GithubWebClientCustomizer customizer) {
        customizer.customize(webClientBuilder);
        this.webClient = webClientBuilder.build();
    }

    private GithubBranchResponse getBranchData(String username, String repository, String branchName) {
        var branchJsonNode = webClient.get()
            .uri(String.format("/repos/%s/%s/branches/%s", username, repository, branchName))
            .exchangeToMono(response -> {
                HttpStatus status = (HttpStatus) response.statusCode();
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
            })
            .block();
        if (branchJsonNode == null) {
            throw new RuntimeException(
                "Error occurred during getBranchData in GithubClient! Message: empty response/data"
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

    private List<String> getBranchNames(String username, String repository) {
        var branchesJsonNodeArr = webClient.get()
            .uri(String.format("/repos/%s/%s/branches", username, repository))
            .exchangeToMono(response -> {
                HttpStatus status = (HttpStatus) response.statusCode();
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
            })
            .block();
        if (branchesJsonNodeArr == null) {
            throw new RuntimeException(
                "Error occurred during getBranchNames in GithubClient! Message: empty response/data"
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

    public GithubClientResponse fetchResponse(String username, String repository) {
        var githubResponse = webClient.get()
            .uri(String.format("/repos/%s/%s", username, repository))
            .exchangeToMono(response -> {
                HttpStatus status = (HttpStatus) response.statusCode();
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
            })
            .block();
        if (githubResponse == null
            || githubResponse.getLastUpdate() == null
            || githubResponse.getPushUpdate() == null
            || githubResponse.getName() == null
            || githubResponse.getId() == null) {
            throw new RuntimeException(
                "Error occurred during fetchResponse in GithubClient! Message: empty response/data"
            );
        } else {
            var branchNames = getBranchNames(username, repository);
            var branchDataList = new ArrayList<GithubBranchResponse>();
            for (var branchName : branchNames) {
                branchDataList.add(getBranchData(username, repository, branchName));
            }
            githubResponse.getBranches().addAll(branchDataList);
            return githubResponse;
        }
    }

}
