package edu.eflerrr.scrapper.client;

import edu.eflerrr.scrapper.client.dto.response.StackoverflowClientResponse;
import edu.eflerrr.scrapper.exception.retry.RetryableRequestException;
import java.util.Set;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.retry.support.RetryTemplate;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

@RequiredArgsConstructor
@SuppressWarnings("ReturnCount")
public class StackoverflowClient {

    private final WebClient webClient;
    private final RetryTemplate retryTemplate;
    private final Set<Integer> retryStatusCodes;

    private StackoverflowClientResponse fetchResponseWithRetry(Long questionId) {
        return retryTemplate.execute(
            context -> getQuestionData(questionId).block()
        );
    }

    private Mono<StackoverflowClientResponse> getQuestionData(Long questionId) {
        return webClient.get()
            .uri(String.format(
                    "/questions/%d/timeline?order=desc&sort=creation_date&site=stackoverflow",
                    questionId
                )
            )
            .exchangeToMono(response -> {
                HttpStatus status = (HttpStatus) response.statusCode();
                if (retryStatusCodes.contains(status.value())) {
                    return Mono.error(
                        new RetryableRequestException("Retryable status code: " + status.value())
                    );
                }
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
            });
    }

    public StackoverflowClientResponse fetchResponse(Long questionId) {
        var stackoverflowResponse = fetchResponseWithRetry(questionId);

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
