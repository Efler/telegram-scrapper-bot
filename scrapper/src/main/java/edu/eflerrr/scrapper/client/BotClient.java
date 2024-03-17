package edu.eflerrr.scrapper.client;

import edu.eflerrr.scrapper.client.dto.request.SendUpdateRequest;
import edu.eflerrr.scrapper.client.dto.response.BotErrorResponse;
import edu.eflerrr.scrapper.exception.InvalidDataException;
import java.net.URI;
import java.util.List;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;
import static org.springframework.http.HttpStatus.BAD_REQUEST;

public class BotClient {
    private final WebClient webClient;
    public final String defaultApiUrl = "http://localhost:8090";

    public BotClient(String baseApiUrl) {
        this.webClient = WebClient.create(
            baseApiUrl == null || baseApiUrl.isEmpty()
                ? defaultApiUrl
                : baseApiUrl);
    }

    public void sendUpdate(Long id, URI url, String description, List<Long> tgChatIds) {
        var request = new SendUpdateRequest(id, url, description, tgChatIds);
        var botResponse = webClient.post()
            .uri("/updates")
            .bodyValue(request)
            .exchangeToMono(response -> {
                if (!response.statusCode().is2xxSuccessful()) {
                    return response.bodyToMono(BotErrorResponse.class);
                }
                return Mono.empty();
            })
            .block();
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
