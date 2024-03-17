package edu.eflerrr.bot.client;

import edu.eflerrr.bot.client.dto.request.LinkRequest;
import edu.eflerrr.bot.client.dto.response.LinkResponse;
import edu.eflerrr.bot.client.dto.response.ListLinksResponse;
import edu.eflerrr.bot.client.dto.response.ScrapperErrorResponse;
import edu.eflerrr.bot.exception.DuplicateLinkPostException;
import edu.eflerrr.bot.exception.DuplicateRegistrationException;
import edu.eflerrr.bot.exception.InvalidDataException;
import edu.eflerrr.bot.exception.LinkNotFoundException;
import edu.eflerrr.bot.exception.TgChatNotExistException;
import java.net.URI;
import java.util.concurrent.atomic.AtomicInteger;
import org.springframework.http.HttpMethod;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;
import static org.springframework.http.HttpStatus.BAD_REQUEST;
import static org.springframework.http.HttpStatus.CONFLICT;
import static org.springframework.http.HttpStatus.NOT_FOUND;
import static org.springframework.http.HttpStatus.OK;

@SuppressWarnings("MemberName")
public class ScrapperClient {
    private final String TG_CHAT_ID_HEADER = "Tg-Chat-Id";
    private final String TG_CHAT_ENDPOINT = "/tg-chat";
    private final String LINKS_ENDPOINT = "/links";
    private final WebClient webClient;
    private final String trackLinkErrorMessage =
        "Error occurred during trackLink in ScrapperClient! ErrorResponse: ";
    private final String untrackLinkErrorMessage =
        "Error occurred during untrackLink in ScrapperClient! ErrorResponse: ";
    private final String listLinksErrorMessage =
        "Error occurred during listLinks in ScrapperClient! ErrorResponse: ";
    public final String defaultApiUrl = "http://localhost:8080";

    public ScrapperClient(String baseApiUrl) {
        this.webClient = WebClient.create(
            baseApiUrl == null || baseApiUrl.isEmpty()
                ? defaultApiUrl
                : baseApiUrl);
    }

    public void registerTgChat(Long id) {
        AtomicInteger statusCode = new AtomicInteger();
        var botResponse = webClient.post()
            .uri(TG_CHAT_ENDPOINT + "/" + id)
            .exchangeToMono(response -> {
                if (!response.statusCode().is2xxSuccessful()) {
                    statusCode.set(response.statusCode().value());
                    return response.bodyToMono(ScrapperErrorResponse.class);
                }
                return Mono.empty();
            })
            .block();
        if (botResponse != null) {
            if (statusCode.get() == (CONFLICT.value())) {
                throw new DuplicateRegistrationException(botResponse.description());
            }
            if (statusCode.get() == (BAD_REQUEST.value())) {
                throw new InvalidDataException(botResponse.description());
            }
            throw new RuntimeException(
                "Error occurred during registerTgChat in ScrapperClient! ErrorResponse: " + botResponse
            );
        }
    }

    public void deleteTgChat(Long id) {
        AtomicInteger statusCode = new AtomicInteger();
        var botResponse = webClient.delete()
            .uri(TG_CHAT_ENDPOINT + "/" + id)
            .exchangeToMono(response -> {
                if (!response.statusCode().is2xxSuccessful()) {
                    statusCode.set(response.statusCode().value());
                    return response.bodyToMono(ScrapperErrorResponse.class);
                }
                return Mono.empty();
            })
            .block();
        if (botResponse != null) {
            if (statusCode.get() == (NOT_FOUND.value())) {
                throw new TgChatNotExistException(botResponse.description());
            }
            if (statusCode.get() == (BAD_REQUEST.value())) {
                throw new InvalidDataException(botResponse.description());
            }
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
                if (response.statusCode().equals(OK)) {
                    return response.bodyToMono(LinkResponse.class);
                }
                return response.bodyToMono(ScrapperErrorResponse.class)
                    .flatMap(errorResponse -> {
                        String errorMessage = errorResponse.description();
                        if (response.statusCode().value() == BAD_REQUEST.value()) {
                            return Mono.error(new InvalidDataException(
                                trackLinkErrorMessage + errorMessage
                            ));
                        }
                        if (response.statusCode().value() == CONFLICT.value()) {
                            return Mono.error(new DuplicateLinkPostException(
                                trackLinkErrorMessage + errorMessage
                            ));
                        }
                        if (response.statusCode().value() == NOT_FOUND.value()) {
                            return Mono.error(new TgChatNotExistException(
                                trackLinkErrorMessage + errorMessage
                            ));
                        }
                        return Mono.error(new RuntimeException(
                            trackLinkErrorMessage + errorMessage
                        ));
                    });
            })
            .block();
    }

    public LinkResponse untrackLink(Long id, URI url) {
        return webClient.method(HttpMethod.DELETE)
            .uri(LINKS_ENDPOINT)
            .header(TG_CHAT_ID_HEADER, id.toString())
            .bodyValue(new LinkRequest(url))
            .exchangeToMono(response -> {
                if (response.statusCode().equals(OK)) {
                    return response.bodyToMono(LinkResponse.class);
                }
                return response.bodyToMono(ScrapperErrorResponse.class)
                    .flatMap(errorResponse -> {
                        String errorMessage = errorResponse.description();
                        if (response.statusCode().value() == BAD_REQUEST.value()) {
                            return Mono.error(new InvalidDataException(
                                untrackLinkErrorMessage + errorMessage
                            ));
                        }
                        if (response.statusCode().value() == NOT_FOUND.value()) {
                            if (errorMessage.equals("Чат не существует")) {
                                return Mono.error(new TgChatNotExistException(
                                    untrackLinkErrorMessage + errorMessage
                                ));
                            } else {
                                return Mono.error(new LinkNotFoundException(
                                    untrackLinkErrorMessage + errorMessage
                                ));
                            }
                        }
                        return Mono.error(new RuntimeException(
                            untrackLinkErrorMessage + errorMessage
                        ));
                    });
            })
            .block();
    }

    public ListLinksResponse listLinks(Long id) {
        return webClient.get()
            .uri(LINKS_ENDPOINT)
            .header(TG_CHAT_ID_HEADER, id.toString())
            .exchangeToMono(response -> {
                if (response.statusCode().equals(OK)) {
                    return response.bodyToMono(ListLinksResponse.class);
                }
                return response.bodyToMono(ScrapperErrorResponse.class)
                    .flatMap(errorResponse -> {
                        String errorMessage = errorResponse.description();
                        if (response.statusCode().value() == BAD_REQUEST.value()) {
                            return Mono.error(new InvalidDataException(
                                listLinksErrorMessage + errorMessage
                            ));
                        }
                        if (response.statusCode().value() == NOT_FOUND.value()) {
                            return Mono.error(new TgChatNotExistException(
                                listLinksErrorMessage + errorMessage
                            ));
                        }
                        return Mono.error(new RuntimeException(
                            listLinksErrorMessage + errorMessage
                        ));
                    });
            })
            .block();
    }

}
