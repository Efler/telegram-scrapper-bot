package edu.eflerrr.scrapper.client;

import edu.eflerrr.scrapper.client.dto.request.SendUpdateRequest;
import edu.eflerrr.scrapper.client.dto.response.BotErrorResponse;
import edu.eflerrr.scrapper.exception.InvalidDataException;
import edu.eflerrr.scrapper.exception.retry.RetryableRequestException;
import edu.eflerrr.scrapper.service.UpdateSender;
import java.net.URI;
import java.util.List;
import java.util.Set;
import org.springframework.retry.support.RetryTemplate;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;
import static org.springframework.http.HttpStatus.BAD_REQUEST;

public class BotClient implements UpdateSender {
    private final WebClient webClient;
    private final RetryTemplate retryTemplate;
    private final Set<Integer> retryStatusCodes;
    public final String defaultApiUrl = "http://localhost:8090";

    public BotClient(
        String baseApiUrl, RetryTemplate retryTemplate, Set<Integer> retryStatusCodes
    ) {
        this.webClient = WebClient.create(
            baseApiUrl == null || baseApiUrl.isEmpty()
                ? defaultApiUrl
                : baseApiUrl);
        this.retryTemplate = retryTemplate;
        this.retryStatusCodes = retryStatusCodes;
    }

    private BotErrorResponse sendUpdateWithRetry(SendUpdateRequest request) {
        return retryTemplate.execute(
            context -> webClientRequest(request).block()
        );
    }

    private Mono<BotErrorResponse> webClientRequest(SendUpdateRequest request) {
        return webClient.post()
            .uri("/updates")
            .bodyValue(request)
            .exchangeToMono(response -> {
                if (retryStatusCodes.contains(response.statusCode().value())) {
                    return Mono.error(
                        new RetryableRequestException("Retryable status code: " + response.statusCode().value())
                    );
                }
                if (!response.statusCode().is2xxSuccessful()) {
                    return response.bodyToMono(BotErrorResponse.class);
                }
                return Mono.empty();
            });
    }

    @Override
    public void sendUpdate(Long id, URI url, String description, List<Long> tgChatIds) {
        var botResponse = sendUpdateWithRetry(
            new SendUpdateRequest(id, url, description, tgChatIds)
        );

        if (botResponse != null) {
            if (Integer.parseInt(botResponse.code()) == BAD_REQUEST.value()) {
                throw new InvalidDataException(botResponse.description());
            }
            throw new RuntimeException(
                "Error occurred during sendUpdate in BotClient! ErrorResponse: " + botResponse
            );
        }
    }
}
