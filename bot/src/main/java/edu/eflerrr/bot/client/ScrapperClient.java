package edu.eflerrr.bot.client;

import edu.eflerrr.bot.client.dto.request.LinkRequest;
import edu.eflerrr.bot.client.dto.response.LinkResponse;
import edu.eflerrr.bot.client.dto.response.ListLinksResponse;
import edu.eflerrr.bot.client.dto.response.ScrapperErrorResponse;
import java.net.URI;
import org.springframework.http.HttpMethod;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

@SuppressWarnings("MemberName")
public class ScrapperClient {
    private static final String TG_CHAT_ID_HEADER = "Tg-Chat-Id";
    private static final String TG_CHAT_ENDPOINT = "/tg-chat";
    private static final String LINKS_ENDPOINT = "/links";
    private final WebClient webClient;
    public final String defaultApiUrl = "localhost:8080";

    public ScrapperClient(String baseApiUrl) {
        this.webClient = WebClient.create(
            baseApiUrl == null || baseApiUrl.isEmpty()
                ? defaultApiUrl
                : baseApiUrl);
    }

    public void registerTgChat(Long id) {
        var botResponse = webClient.post()
            .uri(TG_CHAT_ENDPOINT + "/" + id)
            .exchangeToMono(response -> {
                if (!response.statusCode().is2xxSuccessful()) {
                    return response.bodyToMono(ScrapperErrorResponse.class);
                }
                return Mono.empty();
            })
            .block();
        if (botResponse != null) {
            throw new RuntimeException(
                "Error occurred during registerTgChat in ScrapperClient! ErrorResponse: " + botResponse
            );
        }
    }

    public void deleteTgChat(Long id) {
        var botResponse = webClient.delete()
            .uri(TG_CHAT_ENDPOINT + "/" + id)
            .exchangeToMono(response -> {
                if (!response.statusCode().is2xxSuccessful()) {
                    return response.bodyToMono(ScrapperErrorResponse.class);
                }
                return Mono.empty();
            })
            .block();
        if (botResponse != null) {
            throw new RuntimeException(
                "Error occurred during deleteTgChat in ScrapperClient! ErrorResponse: " + botResponse
            );
        }
    }

    public LinkResponse trackLink(Long id, URI url) {
        return webClient.post()
            .uri(LINKS_ENDPOINT)
            .header(TG_CHAT_ID_HEADER, id.toString())
            .bodyValue(new LinkRequest(url))
            .exchangeToMono(response -> {
                if (response.statusCode().is2xxSuccessful()) {
                    return response.bodyToMono(LinkResponse.class);
                }
                var errorResponse = response.bodyToMono(ScrapperErrorResponse.class);
                return Mono.error(new RuntimeException(
                    "Error occurred during trackLink in ScrapperClient! ErrorResponse: " + errorResponse
                ));
            })
            .block();
    }

    public LinkResponse untrackLink(Long id, URI url) {
        return webClient.method(HttpMethod.DELETE)
            .uri(LINKS_ENDPOINT)
            .header(TG_CHAT_ID_HEADER, id.toString())
            .bodyValue(new LinkRequest(url))
            .exchangeToMono(response -> {
                if (response.statusCode().is2xxSuccessful()) {
                    return response.bodyToMono(LinkResponse.class);
                }
                var errorResponse = response.bodyToMono(ScrapperErrorResponse.class);
                return Mono.error(new RuntimeException(
                    "Error occurred during untrackLink in ScrapperClient! ErrorResponse: " + errorResponse
                ));
            })
            .block();
    }

    public ListLinksResponse listLinks(Long id) {
        return webClient.post()
            .uri(LINKS_ENDPOINT)
            .header(TG_CHAT_ID_HEADER, id.toString())
            .exchangeToMono(response -> {
                if (response.statusCode().is2xxSuccessful()) {
                    return response.bodyToMono(ListLinksResponse.class);
                }
                var errorResponse = response.bodyToMono(ScrapperErrorResponse.class);
                return Mono.error(new RuntimeException(
                    "Error occurred during listLinks in ScrapperClient! ErrorResponse: " + errorResponse
                ));
            })
            .block();
    }
}
