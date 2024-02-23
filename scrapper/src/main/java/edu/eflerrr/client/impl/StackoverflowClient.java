package edu.eflerrr.client.impl;

import edu.eflerrr.client.HttpClient;
import edu.eflerrr.client.response.impl.StackoverflowClientResponse;
import org.springframework.http.HttpStatus;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

public class StackoverflowClient implements HttpClient {
    private final WebClient webClient;
    public final String defaultApiUrl = "https://api.stackexchange.com";

    public StackoverflowClient(String baseApiUrl) {
        var apiUrl = baseApiUrl == null || baseApiUrl.isEmpty()
            ? defaultApiUrl
            : baseApiUrl;
        this.webClient = WebClient.create(apiUrl);
    }

    @Override
    public StackoverflowClientResponse fetchResponse(String question) {
        var stackoverflowResponse = webClient.get()
            .uri(String.format(
                    "/questions%s/timeline?order=desc&sort=creation_date&site=stackoverflow",
                    question
                )
            )
            .exchangeToMono(response -> {
                HttpStatus status = (HttpStatus) response.statusCode();
                if (status.is2xxSuccessful()) {
                    return response.bodyToMono(StackoverflowClientResponse.class);
                } else if (status.is4xxClientError()) {
                    return Mono.error(new RuntimeException(
                        "Client error during fetchResponse in StackoverflowClient! Message: "
                            + status.value() + " "
                            + status.getReasonPhrase()));
                } else if (status.is5xxServerError()) {
                    return Mono.error(new RuntimeException(
                        "Server error during fetchResponse in StackoverflowClient! Message: "
                            + status.value() + " "
                            + status.getReasonPhrase()));
                } else {
                    return Mono.error(new RuntimeException(
                        "Unexpected status code during fetchResponse in StackoverflowClient! Message: "
                            + status.value() + " "
                            + status.getReasonPhrase()));
                }
            })
            .block();
        if (stackoverflowResponse != null
            && stackoverflowResponse.events() != null
            && !stackoverflowResponse.events().isEmpty()
        ) {
            boolean isAllEventsHaveData = stackoverflowResponse.events().stream()
                .allMatch(event -> event.time() != null && event.type() != null);
            if (isAllEventsHaveData) {
                stackoverflowResponse.setLastUpdate(stackoverflowResponse.events().getFirst().time());
                return stackoverflowResponse;
            }
        }
        throw new RuntimeException(
            "Error occurred during fetchResponse in StackoverflowClient! Message: empty response/data"
        );
    }

}
