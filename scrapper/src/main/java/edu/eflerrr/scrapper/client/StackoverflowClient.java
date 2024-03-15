package edu.eflerrr.scrapper.client;

import edu.eflerrr.scrapper.client.customizer.StackoverflowWebClientCustomizer;
import edu.eflerrr.scrapper.client.dto.response.StackoverflowClientResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

@Component
public class StackoverflowClient {
    private final WebClient webClient;

    @Autowired
    public StackoverflowClient(WebClient.Builder webClientBuilder, StackoverflowWebClientCustomizer customizer) {
        customizer.customize(webClientBuilder);
        this.webClient = webClientBuilder.build();
    }

    public StackoverflowClientResponse fetchResponse(Long questionId) {
        var stackoverflowResponse = webClient.get()
            .uri(String.format(
                    "/questions/%d/timeline?order=desc&sort=creation_date&site=stackoverflow",
                    questionId
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
