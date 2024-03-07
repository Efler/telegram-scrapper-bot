package edu.eflerrr.scrapper.client;

import edu.eflerrr.scrapper.client.customizer.GithubWebClientCustomizer;
import edu.eflerrr.scrapper.client.dto.response.GithubClientResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

@Component
public class GithubClient {
    private final WebClient webClient;

    @Autowired
    public GithubClient(WebClient.Builder webClientBuilder, GithubWebClientCustomizer customizer) {
        customizer.customize(webClientBuilder);
        this.webClient = webClientBuilder.build();
    }

    public GithubClientResponse fetchResponse(String repository) {
        var githubResponse = webClient.get()
            .uri(String.format("/repos%s", repository))
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
            || githubResponse.lastUpdate() == null
            || githubResponse.pushUpdate() == null
            || githubResponse.name() == null
            || githubResponse.id() == null) {
            throw new RuntimeException(
                "Error occurred during fetchResponse in GithubClient! Message: empty response/data"
            );
        } else {
            return githubResponse;
        }
    }

}
